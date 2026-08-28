package com.omnify.auth.service.impl;

import com.omnify.auth.domain.entity.LoginAttempt;
import com.omnify.auth.domain.entity.RefreshToken;
import com.omnify.auth.domain.entity.VerificationToken;
import com.omnify.auth.domain.repository.LoginAttemptRepository;
import com.omnify.auth.domain.repository.RefreshTokenRepository;
import com.omnify.auth.domain.repository.VerificationTokenRepository;
import com.omnify.auth.dto.request.LoginRequest;
import com.omnify.auth.dto.request.RegisterRequest;
import com.omnify.auth.dto.response.LoginResponse;
import com.omnify.auth.dto.response.RegisterResponse;
import com.omnify.auth.infrastructure.*;
import com.omnify.auth.notification.DuplicateRegistrationEvent;
import com.omnify.auth.notification.UserRegisteredEvent;
import com.omnify.auth.service.AuthService;
import com.omnify.auth.service.recorder.LoginSecurityRecorder;
import com.omnify.auth.service.recorder.VerificationAttemptRecorder;
import com.omnify.common.exception.BusinessException;
import com.omnify.common.exception.ErrorCode;
import com.omnify.rbac.service.RoleAssignmentService;
import com.omnify.security.JwtTokenProvider;
import com.omnify.security.TokenHasher;
import com.omnify.user.domain.entity.User;
import com.omnify.user.domain.entity.UserProfile;
import com.omnify.user.domain.entity.UserStatus;
import com.omnify.user.domain.repository.UserRepository;
import com.omnify.user.domain.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private static final String OWNER_ROLE = "owner";
    private static final String CAPTCHA_SCOPE_REGISTER = "register";
    private static final String CAPTCHA_SCOPE_LOGIN = "login";
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final LoginAttemptRepository loginAttemptRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenGenerator verificationTokenGenerator;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final JwtTokenProvider jwtTokenProvider;
    private final RoleAssignmentService roleAssignmentService;
    private final ApplicationEventPublisher eventPublisher;
    private final LoginSecurityRecorder loginSecurityRecorder;
    private final VerificationAttemptRecorder verificationAttemptRecorder;
    private final TokenHasher tokenHasher;
    private final DeviceInfoParser deviceInfoParser;
    private final CaptchaVerifier captchaVerifier;
    private final CaptchaGate captchaGate;
    private final int refreshTokenTtlDays;
    @Value("${omnify.verification.phone-token-ttl-minutes}")
    private int phoneOtpTtlMinutes;
    @Value("${omnify.verification.resend-cooldown-seconds}")
    private long resendCooldownSeconds;
    @Value("${omnify.verification.max-otp-attempts}")
    private int maxOtpAttempts;
    @Value("${omnify.verification.email-token-ttl-minutes}")
    private int emailTokenTtlMinutes;

    public AuthServiceImpl(
        UserRepository userRepository, UserProfileRepository userProfileRepository,
        VerificationTokenRepository verificationTokenRepository,
        RefreshTokenRepository refreshTokenRepository,
        LoginAttemptRepository loginAttemptRepository,
        PasswordEncoder passwordEncoder,
        VerificationTokenGenerator verificationTokenGenerator,
        RefreshTokenGenerator refreshTokenGenerator,
        JwtTokenProvider jwtTokenProvider,
        RoleAssignmentService roleAssignmentService,
        ApplicationEventPublisher eventPublisher,
        LoginSecurityRecorder loginSecurityRecorder,
        VerificationAttemptRecorder verificationAttemptRecorder,
        TokenHasher tokenHasher,
        DeviceInfoParser deviceInfoParser,
        CaptchaVerifier captchaVerifier, CaptchaGate captchaGate,
        @Value("${omnify.security.auth.refresh-token-ttl-days}") int refreshTokenTtlDays
    ) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.loginAttemptRepository = loginAttemptRepository;
        this.passwordEncoder = passwordEncoder;
        this.verificationTokenGenerator = verificationTokenGenerator;
        this.refreshTokenGenerator = refreshTokenGenerator;
        this.jwtTokenProvider = jwtTokenProvider;
        this.roleAssignmentService = roleAssignmentService;
        this.eventPublisher = eventPublisher;
        this.loginSecurityRecorder = loginSecurityRecorder;
        this.verificationAttemptRecorder = verificationAttemptRecorder;
        this.tokenHasher = tokenHasher;
        this.deviceInfoParser = deviceInfoParser;
        this.captchaVerifier = captchaVerifier;
        this.captchaGate = captchaGate;
        this.refreshTokenTtlDays = refreshTokenTtlDays;
    }

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request, String ipAddress) {
        if (captchaGate.isCaptchaRequired(CAPTCHA_SCOPE_REGISTER, ipAddress)){
            if (request.getCaptchaToken() == null){
                throw new BusinessException(ErrorCode.CAPTCHA_REQUIRED);
            }
            if (!captchaVerifier.verify(request.getCaptchaToken())){
                throw new BusinessException(ErrorCode.CAPTCHA_FAILED);
            }
        }

        Optional<User> existingByEmail = request.getEmail() != null
            ? userRepository.findByEmail(request.getEmail())
            : Optional.empty();

        Optional<User> existingByPhone = request.getPhone() != null
            ? userRepository.findByPhone(request.getPhone())
            : Optional.empty();

        if (existingByEmail.isPresent()) {
            if (existingByEmail.get().getStatus() == UserStatus.PENDING) {
                captchaGate.recordFailure(CAPTCHA_SCOPE_REGISTER, ipAddress);
                throw new BusinessException(ErrorCode.ACCOUNT_PENDING_VERIFICATION);
            }
            eventPublisher.publishEvent(new DuplicateRegistrationEvent(request.getEmail()));
            captchaGate.recordFailure(CAPTCHA_SCOPE_REGISTER, ipAddress);
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        if (existingByPhone.isPresent()) {
            if (existingByPhone.get().getStatus() == UserStatus.PENDING) {
                captchaGate.recordFailure(CAPTCHA_SCOPE_REGISTER, ipAddress);
                throw new BusinessException(ErrorCode.ACCOUNT_PENDING_VERIFICATION);
            }
            captchaGate.recordFailure(CAPTCHA_SCOPE_REGISTER, ipAddress);
            throw new BusinessException(ErrorCode.PHONE_ALREADY_EXISTS);
        }

        String passwordHash = passwordEncoder.encode(request.getPassword());
        User user = User.builder()
            .email(request.getEmail())
            .phone(request.getPhone())
            .passwordHash(passwordHash)
            .build();
        user = userRepository.save(user);

        UserProfile profile= UserProfile.builder()
            .userId(user.getId())
            .user(user)
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .build();
        userProfileRepository.save(profile);

        roleAssignmentService.assignRole(user.getId(), OWNER_ROLE, null);

        boolean useEmailChannel = request.getEmail() != null;
        VerificationToken.Type tokenType = useEmailChannel
            ? VerificationToken.Type.EMAIL_VERIFICATION
            : VerificationToken.Type.PHONE_VERIFICATION;

        String rawToken = useEmailChannel
            ? verificationTokenGenerator.generateEmailToken()
            : verificationTokenGenerator.generatePhoneOtp();

        int ttlMinutes = useEmailChannel ? emailTokenTtlMinutes : phoneOtpTtlMinutes;

        VerificationToken verificationToken = VerificationToken.issue(
            user.getId(),
            verificationTokenGenerator.hash(rawToken),
            tokenType,
            OffsetDateTime.now().plusMinutes(ttlMinutes)
        );
        verificationTokenRepository.save(verificationToken);

        eventPublisher.publishEvent(new UserRegisteredEvent(
            user.getId(),
            profile.getFirstName() + " " + profile.getLastName(),
            user.getEmail(),
            user.getPhone(),
            rawToken,
            tokenType
        ));
        captchaGate.resetAttempts(CAPTCHA_SCOPE_REGISTER, ipAddress);

        return RegisterResponse.builder()
            .userId(user.getId())
            .status(user.getStatus().name())
            .verificationChannel(tokenType.name())
            .assignedRole(OWNER_ROLE)
            .build();
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request, String ipAddress, String userAgent) {
        if (captchaGate.isCaptchaRequired(CAPTCHA_SCOPE_LOGIN, ipAddress)){
            if (request.getCaptchaToken() == null){
                throw new BusinessException(ErrorCode.CAPTCHA_REQUIRED);
            }
            if (!captchaVerifier.verify(request.getCaptchaToken())){
                throw new BusinessException(ErrorCode.CAPTCHA_FAILED);
            }
        }

        String identifier = request.getEmail() != null ? request.getEmail() : request.getPhone();

        User user = userRepository.findByEmailOrPhone(identifier).orElse(null);

        if (user == null) {
            loginSecurityRecorder.recordUnknownIdentifierAttempt(identifier, ipAddress, userAgent);
            captchaGate.recordFailure(CAPTCHA_SCOPE_LOGIN, ipAddress);
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        if (user.isLocked()) {
            loginSecurityRecorder.recordLockedAttempt(user.getId(), identifier, ipAddress, userAgent);
            captchaGate.recordFailure(CAPTCHA_SCOPE_LOGIN,ipAddress);
            throw new BusinessException(ErrorCode.ACCOUNT_LOCKED);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            loginSecurityRecorder.recordFailedAttempt(user.getId(), identifier, ipAddress, userAgent);
            captchaGate.recordFailure(CAPTCHA_SCOPE_LOGIN,ipAddress);
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        if (user.getStatus() == UserStatus.PENDING) {
            LoginAttempt.FailureReason reason = user.isEmailVerified()
                ? LoginAttempt.FailureReason.PHONE_NOT_VERIFIED
                : LoginAttempt.FailureReason.EMAIL_NOT_VERIFIED;
            loginSecurityRecorder.recordUnverifiedAttempt(user.getId(), identifier, ipAddress, userAgent, reason);
            captchaGate.recordFailure(CAPTCHA_SCOPE_LOGIN,ipAddress);
            throw new BusinessException(ErrorCode.ACCOUNT_NOT_VERIFIED);
        }

        user.registerSuccessfulLogin();
        loginAttemptRepository.save(LoginAttempt.success(user.getId(), identifier, ipAddress, userAgent));
        captchaGate.resetAttempts(CAPTCHA_SCOPE_LOGIN, ipAddress);

        String role = roleAssignmentService.getPrimaryRoleName(user.getId());
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), role);

        String rawRefreshToken = refreshTokenGenerator.generate();
        RefreshToken refreshToken = RefreshToken.issue(
            user.getId(),
            refreshTokenGenerator.hash(rawRefreshToken),
            deviceInfoParser.parseDeviceName(userAgent),
            userAgent,
            ipAddress,
            OffsetDateTime.now().plusDays(refreshTokenTtlDays)
        );

        refreshTokenRepository.save(refreshToken);

        return LoginResponse.builder()
            .accessToken(accessToken)
            .refreshToken(rawRefreshToken)
            .sessionId(refreshToken.getId())
            .expiresIn(jwtTokenProvider.getAccessTokenTtlSeconds())
            .userId(user.getId())
            .role(role)
            .build();
    }

    @Override
    @Transactional
    public void verifyEmail(String email, String otpCode) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.isEmailVerified()) {
            throw new BusinessException(ErrorCode.ACCOUNT_ALREADY_VERIFIED);
        }

        VerificationToken token = verificationTokenRepository
            .findTopByUserIdAndTypeOrderByCreatedAtDesc(user.getId(), VerificationToken.Type.EMAIL_VERIFICATION)
            .orElseThrow(() -> new BusinessException(ErrorCode.OTP_INVALID));

        if (token.isExpired()) {
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        }
        if (token.getAttemptCount() >= maxOtpAttempts) {
            throw new BusinessException(ErrorCode.OTP_LOCKED);
        }

        String inputHash = tokenHasher.hash(otpCode);

        if (!inputHash.equals(token.getTokenHash())) {
            int attemptsSoFar = verificationAttemptRecorder.recordFailedAttempt(token.getId());
            if (attemptsSoFar >= maxOtpAttempts) {
                throw new BusinessException(ErrorCode.OTP_LOCKED);
            }
            throw new BusinessException(ErrorCode.OTP_INVALID);
        }

        user.markEmailVerified();
        token.markUsed();

        userRepository.save(user);
        verificationTokenRepository.save(token);
    }

    @Override
    @Transactional
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.isEmailVerified()) {
            throw new BusinessException(ErrorCode.ACCOUNT_ALREADY_VERIFIED);
        }

        UserProfile profile = userProfileRepository.findByUserId(user.getId())
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Optional<VerificationToken> lastToken = verificationTokenRepository
            .findTopByUserIdAndTypeOrderByCreatedAtDesc(user.getId(), VerificationToken.Type.EMAIL_VERIFICATION);

        if (lastToken.isPresent()) {
            VerificationToken last = lastToken.get();
            OffsetDateTime nextAllowedAt = last.getCreatedAt().plusSeconds(resendCooldownSeconds);
            if (OffsetDateTime.now().isBefore(nextAllowedAt)) {
                throw new BusinessException(ErrorCode.RESEND_COOLDOWN);
            }
            if (!last.isUsed()) {
                last.markUsed();
                verificationTokenRepository.save(last);
            }
        }

        String rawOtp = verificationTokenGenerator.generateEmailToken();
        VerificationToken newToken = VerificationToken.issue(
            user.getId(),
            verificationTokenGenerator.hash(rawOtp),
            VerificationToken.Type.EMAIL_VERIFICATION,
            OffsetDateTime.now().plusMinutes(emailTokenTtlMinutes)
        );
        verificationTokenRepository.save(newToken);

        eventPublisher.publishEvent(new UserRegisteredEvent(
            user.getId(), profile.getFirstName() + " " + profile.getLastName(), user.getEmail(), user.getPhone(),
            rawOtp, VerificationToken.Type.EMAIL_VERIFICATION
        ));
    }

    @Override
    @Transactional
    public LoginResponse refreshToken(String rawRefreshToken, String ipAddress, String userAgent) {
        String tokenHash = refreshTokenGenerator.hash(rawRefreshToken);

        RefreshToken token = refreshTokenRepository.findByTokenHash(tokenHash)
            .orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID));

        if (token.getStatus() == RefreshToken.Status.REVOKED) {
            loginSecurityRecorder.revokeAllSessionsOnReuseDetected(token.getUserId());
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_REUSE_DETECTED);
        }

        if (token.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        User user = userRepository.findById(token.getUserId())
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.getStatus() == UserStatus.PENDING) {
            throw new BusinessException(ErrorCode.ACCOUNT_NOT_VERIFIED);
        }

        token.touchLastUsed();
        token.revoke();
        refreshTokenRepository.save(token);

        String role = roleAssignmentService.getPrimaryRoleName(user.getId());
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), role);

        String newRawRefreshToken = refreshTokenGenerator.generate();
        RefreshToken newToken = RefreshToken.issue(
            user.getId(),
            refreshTokenGenerator.hash(newRawRefreshToken),
            token.getDeviceName(),
            userAgent,
            ipAddress,
            OffsetDateTime.now().plusDays(refreshTokenTtlDays)
        );
        refreshTokenRepository.save(newToken);

        return LoginResponse.builder()
            .accessToken(accessToken)
            .refreshToken(newRawRefreshToken)
            .sessionId(newToken.getId())
            .expiresIn(jwtTokenProvider.getAccessTokenTtlSeconds())
            .userId(user.getId())
            .role(role)
            .build();
    }

    @Override
    @Transactional
    public void logout(String rawRefreshToken, UUID userId) {
        String tokenHash = refreshTokenGenerator.hash(rawRefreshToken);
        RefreshToken token = refreshTokenRepository.findByTokenHash(tokenHash)
            .orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID));
        if (!token.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID);
        }
        //
        if (token.getStatus() == RefreshToken.Status.VALID) {
            token.revoke();
            refreshTokenRepository.save(token);
        }
    }

    @Override
    @Transactional
    public void verifyPhone(String phone, String otpCode) {
        User user = userRepository.findByPhone(phone)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.isPhoneVerified()) {
            throw new BusinessException(ErrorCode.ACCOUNT_ALREADY_VERIFIED);
        }

        VerificationToken token = verificationTokenRepository
            .findTopByUserIdAndTypeOrderByCreatedAtDesc(user.getId(), VerificationToken.Type.PHONE_VERIFICATION)
            .orElseThrow(() -> new BusinessException(ErrorCode.OTP_INVALID));

        if (token.isExpired()) {
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        }

        if (token.getAttemptCount() >= maxOtpAttempts) {
            throw new BusinessException(ErrorCode.OTP_LOCKED);
        }

        String inputHash = tokenHasher.hash(otpCode);

        if (!inputHash.equals(token.getTokenHash())) {
            int attemptsSoFar = verificationAttemptRecorder.recordFailedAttempt(token.getId());
            if (attemptsSoFar >= maxOtpAttempts) {
                throw new BusinessException(ErrorCode.OTP_LOCKED);
            }
            throw new BusinessException(ErrorCode.OTP_INVALID);
        }

        user.markPhoneVerified();
        token.markUsed();

        userRepository.save(user);
        verificationTokenRepository.save(token);
    }

    @Override
    @Transactional
    public void resendVerificationPhone(String phone) {
        User user = userRepository.findByPhone(phone)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.isPhoneVerified()) {
            throw new BusinessException(ErrorCode.ACCOUNT_ALREADY_VERIFIED);
        }

        UserProfile profile = userProfileRepository.findByUserId(user.getId())
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Optional<VerificationToken> lastToken = verificationTokenRepository
            .findTopByUserIdAndTypeOrderByCreatedAtDesc(user.getId(), VerificationToken.Type.PHONE_VERIFICATION);

        if (lastToken.isPresent()) {
            VerificationToken last = lastToken.get();
            OffsetDateTime nextAllowedAt = last.getCreatedAt().plusSeconds(resendCooldownSeconds);
            if (OffsetDateTime.now().isBefore(nextAllowedAt)) {
                throw new BusinessException(ErrorCode.RESEND_COOLDOWN);
            }
            if (!last.isUsed()) {
                last.markUsed();
                verificationTokenRepository.save(last);
            }
        }

        String rawOtp = verificationTokenGenerator.generatePhoneOtp();
        VerificationToken newToken = VerificationToken.issue(
            user.getId(),
            verificationTokenGenerator.hash(rawOtp),
            VerificationToken.Type.PHONE_VERIFICATION,
            OffsetDateTime.now().plusMinutes(phoneOtpTtlMinutes)
        );
        verificationTokenRepository.save(newToken);

        eventPublisher.publishEvent(new UserRegisteredEvent(
            user.getId(),
            profile.getFirstName() + " " + profile.getLastName(),
            user.getEmail(),
            user.getPhone(),
            rawOtp,
            VerificationToken.Type.PHONE_VERIFICATION

        ));
    }
}