package com.omnify.auth.service;

import com.omnify.auth.domain.entity.LoginAttempt;
import com.omnify.auth.domain.entity.RefreshToken;
import com.omnify.auth.domain.entity.User;
import com.omnify.auth.domain.entity.UserStatus;
import com.omnify.auth.domain.entity.VerificationToken;
import com.omnify.auth.notification.UserRegisteredEvent;
import com.omnify.auth.domain.repository.LoginAttemptRepository;
import com.omnify.auth.domain.repository.RefreshTokenRepository;
import com.omnify.auth.domain.repository.UserRepository;
import com.omnify.auth.domain.repository.VerificationTokenRepository;
import com.omnify.auth.dto.request.LoginRequest;
import com.omnify.auth.dto.request.RegisterRequest;
import com.omnify.auth.dto.response.LoginResponse;
import com.omnify.auth.dto.response.RegisterResponse;
import com.omnify.auth.infrastructure.RefreshTokenGenerator;
import com.omnify.auth.infrastructure.VerificationTokenGenerator;
import com.omnify.auth.domain.entity.Company;
import com.omnify.common.exception.BusinessException;
import com.omnify.common.exception.ErrorCode;
import com.omnify.auth.domain.repository.CompanyRepository;
import com.omnify.common.security.JwtTokenProvider;
import com.omnify.common.security.TokenHasher;
import com.omnify.rbac.service.RoleAssignmentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {


    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
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


    private final int maxFailedLoginAttempts;
    private final int lockDurationMinutes;
    private final int refreshTokenTtlDays;

    private static final String OWNER_ROLE = "owner";

    @Value("${app.verification.phone-token-ttl-minutes}")
    private int phoneOtpTtlMinutes;
    @Value("${app.verification.resend-cooldown-seconds}")
    private long resendCooldownSeconds;
    @Value("${app.verification.max-otp-attempts}")
    private int maxOtpAttempts;
    @Value("${app.verification.email-token-ttl-minutes}")
    private int emailTokenTtlMinutes;

    public AuthServiceImpl(UserRepository userRepository,
                           CompanyRepository companyRepository,
                           VerificationTokenRepository verificationTokenRepository,
                           RefreshTokenRepository refreshTokenRepository,
                           LoginAttemptRepository loginAttemptRepository,
                           PasswordEncoder passwordEncoder,
                           VerificationTokenGenerator verificationTokenGenerator,
                           RefreshTokenGenerator refreshTokenGenerator,
                           JwtTokenProvider jwtTokenProvider,
                           RoleAssignmentService roleAssignmentService,
                           ApplicationEventPublisher eventPublisher, LoginSecurityRecorder loginSecurityRecorder, VerificationAttemptRecorder verificationAttemptRecorder, TokenHasher tokenHasher,
                           @Value("${omnify.security.auth.max-failed-login-attempts}") int maxFailedLoginAttempts,
                           @Value("${omnify.security.auth.lock-duration-minutes}") int lockDurationMinutes,
                           @Value("${omnify.security.auth.refresh-token-ttl-days}") int refreshTokenTtlDays) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
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
        this.maxFailedLoginAttempts = maxFailedLoginAttempts;
        this.lockDurationMinutes = lockDurationMinutes;
        this.refreshTokenTtlDays = refreshTokenTtlDays;
    }

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        if (request.getPhone() != null && userRepository.existsByPhone(request.getPhone())) {
            throw new BusinessException(ErrorCode.PHONE_ALREADY_EXISTS);
        }

        Company company = Company.createActive(request.getCompanyName());
        company = companyRepository.save(company);

        String passwordHash = passwordEncoder.encode(request.getPassword());
        User user = User.createPending(
                company.getId(), request.getEmail(), request.getPhone(), passwordHash, request.getFullName());
        user = userRepository.save(user);

        roleAssignmentService.assignRole(user.getId(), OWNER_ROLE, null);

        boolean useEmailChannel = request.getEmail() != null;
        VerificationToken.Type tokenType = useEmailChannel
                ? VerificationToken.Type.EMAIL_VERIFY
                : VerificationToken.Type.PHONE_VERIFY;

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
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                rawToken,
                tokenType
        ));

        return new RegisterResponse(user.getId(), company.getId(), user.getStatus().name(),
                tokenType.name(), OWNER_ROLE);
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request, String ipAddress, String userAgent) {
        String identifier = request.getEmail() != null ? request.getEmail() : request.getPhone();


        User user = userRepository.findByEmailOrPhone(identifier).orElse(null);

        if (user == null) {
            loginSecurityRecorder.recordUnknownIdentifierAttempt(identifier, ipAddress, userAgent);
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        if (user.isLocked()) {
            loginSecurityRecorder.recordLockedAttempt(user.getId(), identifier, ipAddress, userAgent);
            throw new BusinessException(ErrorCode.ACCOUNT_LOCKED);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            loginSecurityRecorder.recordFailedAttempt(
                    user.getId(), identifier, ipAddress, userAgent, maxFailedLoginAttempts, lockDurationMinutes);
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            loginSecurityRecorder.recordLockedAttempt(user.getId(), identifier, ipAddress, userAgent);
            throw new BusinessException(ErrorCode.ACCOUNT_NOT_VERIFIED);
        }

        user.registerSuccessfulLogin();
        loginAttemptRepository.save(LoginAttempt.record(user.getId(), identifier, true, ipAddress, userAgent));

        String role = roleAssignmentService.getPrimaryRoleName(user.getId());
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getCompanyId(), role);

        String rawRefreshToken = refreshTokenGenerator.generate();
        RefreshToken refreshToken = RefreshToken.issue(
                user.getId(),
                refreshTokenGenerator.hash(rawRefreshToken),
                request.getDeviceName(),
                request.getDeviceType(),
                userAgent,
                ipAddress,
                OffsetDateTime.now().plusDays(refreshTokenTtlDays)
        );
        refreshTokenRepository.save(refreshToken);

        return new LoginResponse(accessToken, rawRefreshToken, refreshToken.getId(),
                jwtTokenProvider.getAccessTokenTtlSeconds(), user.getId(), user.getCompanyId(), role);
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
                .findTopByUserIdAndTypeOrderByCreatedAtDesc(user.getId(), VerificationToken.Type.EMAIL_VERIFY)
                .orElseThrow(() -> new BusinessException(ErrorCode.TOKEN_INVALID));

        if (token.isUsed()) {
            throw new BusinessException(ErrorCode.TOKEN_ALREADY_USED);
        }
        if (token.isExpired()) {
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        }
        if (token.getAttemptCount() >= maxOtpAttempts) {
            throw new BusinessException(ErrorCode.OTP_LOCKED);
        }

        String inputHash = tokenHasher.hash(otpCode);

        if (!inputHash.equals(token.getTokenHash())) {
            // ✅ Ghi nhận sai OTP qua transaction ĐỘC LẬP — luôn commit thật xuống DB
            // dù method này sắp throw exception ngay sau đó
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

        Optional<VerificationToken> lastToken = verificationTokenRepository
                .findTopByUserIdAndTypeOrderByCreatedAtDesc(user.getId(), VerificationToken.Type.EMAIL_VERIFY);

        if (lastToken.isPresent()) {
            VerificationToken last = lastToken.get();
            OffsetDateTime nextAllowedAt = last.getCreatedAt().plusSeconds(resendCooldownSeconds);
            if (OffsetDateTime.now().isBefore(nextAllowedAt)) {
                throw new BusinessException(ErrorCode.RESEND_COOLDOWN);
            }
            if (!last.isUsed()) {
                last.markUsed(); // invalidate mã cũ — tránh 2 mã cùng sống, giảm attack surface
                verificationTokenRepository.save(last);
            }
        }

        String rawOtp = verificationTokenGenerator.generateEmailToken();
        VerificationToken newToken = VerificationToken.issue(
                user.getId(),
                verificationTokenGenerator.hash(rawOtp),
                VerificationToken.Type.EMAIL_VERIFY,
                OffsetDateTime.now().plusMinutes(emailTokenTtlMinutes)
        );
        verificationTokenRepository.save(newToken);

        eventPublisher.publishEvent(new UserRegisteredEvent(
                user.getId(), user.getFullName(), user.getEmail(), user.getPhone(),
                rawOtp, VerificationToken.Type.EMAIL_VERIFY
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

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.ACCOUNT_NOT_VERIFIED);
        }

        // Rotation: revoke token cũ NGAY trong transaction này trước khi issue token mới,
        // đảm bảo tại một thời điểm chỉ có đúng 1 refresh token VALID cho phiên này.
        token.touchLastUsed();
        token.revoke();
        refreshTokenRepository.save(token);

        String role = roleAssignmentService.getPrimaryRoleName(user.getId());
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getCompanyId(), role);

        String newRawRefreshToken = refreshTokenGenerator.generate();
        RefreshToken newToken = RefreshToken.issue(
                user.getId(),
                refreshTokenGenerator.hash(newRawRefreshToken),
                token.getDeviceName(),
                token.getDeviceType(),
                userAgent,
                ipAddress,
                OffsetDateTime.now().plusDays(refreshTokenTtlDays)
        );
        refreshTokenRepository.save(newToken);

        return new LoginResponse(accessToken, newRawRefreshToken, newToken.getId(),
                jwtTokenProvider.getAccessTokenTtlSeconds(), user.getId(), user.getCompanyId(), role);
    }
}