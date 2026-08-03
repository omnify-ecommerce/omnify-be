package com.omnify.auth.service;

import com.omnify.auth.domain.entity.LoginAttempt;
import com.omnify.auth.domain.entity.RefreshToken;
import com.omnify.auth.domain.entity.User;
import com.omnify.auth.domain.entity.UserStatus;
import com.omnify.auth.domain.entity.VerificationToken;
import com.omnify.auth.domain.event.UserRegisteredEvent;
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
import com.omnify.common.entity.Company;
import com.omnify.common.exception.BusinessException;
import com.omnify.common.exception.ErrorCode;
import com.omnify.common.repository.CompanyRepository;
import com.omnify.common.security.JwtTokenProvider;
import com.omnify.common.security.TokenHasher;
import com.omnify.rbac.service.RoleAssignmentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private static final int EMAIL_TOKEN_TTL_MINUTES = 30;
    private static final int PHONE_OTP_TTL_MINUTES = 5;
    private static final String OWNER_ROLE = "owner";

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
    private final TokenHasher tokenHasher;


    private final int maxFailedLoginAttempts;
    private final int lockDurationMinutes;
    private final int refreshTokenTtlDays;

    @Value("${app.verification.resend-cooldown-seconds}")
    private long resendCooldownSeconds;

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
                           ApplicationEventPublisher eventPublisher, LoginSecurityRecorder loginSecurityRecorder, TokenHasher tokenHasher,
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

        int ttlMinutes = useEmailChannel ? EMAIL_TOKEN_TTL_MINUTES : PHONE_OTP_TTL_MINUTES;

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
    public void verifyEmail(String rawToken) {
        String tokenHash = tokenHasher.hash(rawToken);

        VerificationToken token = verificationTokenRepository
                .findByTokenHashAndType(tokenHash, VerificationToken.Type.EMAIL_VERIFY)
                .orElseThrow(() -> new BusinessException(ErrorCode.TOKEN_INVALID));

        if (token.isUsed()) {
            throw new BusinessException(ErrorCode.TOKEN_ALREADY_USED);
        }
        if (token.isExpired()) {
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        }

        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.isEmailVerified()) {
            throw new BusinessException(ErrorCode.ACCOUNT_ALREADY_VERIFIED);
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

        if (user.getStatus() == UserStatus.ACTIVE) {
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
                last.markUsed(); // invalidate token cũ, tránh nhiều token cùng sống
                verificationTokenRepository.save(last);
            }
        }

        // ✅ FIX bug #5: phải sinh VÀ LƯU token mới, không chỉ sinh raw token để gửi mail
        String rawToken = verificationTokenGenerator.generateEmailToken();
        VerificationToken newToken = VerificationToken.issue(
                user.getId(),
                verificationTokenGenerator.hash(rawToken),
                VerificationToken.Type.EMAIL_VERIFY,
                OffsetDateTime.now().plusMinutes(EMAIL_TOKEN_TTL_MINUTES)
        );
        verificationTokenRepository.save(newToken);

        eventPublisher.publishEvent(new UserRegisteredEvent(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                rawToken,
                VerificationToken.Type.EMAIL_VERIFY
        ));
    }
}