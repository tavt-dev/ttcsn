package com.friendify.app.auth.service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;

import com.friendify.app.auth.entity.InvalidatedToken;
import com.friendify.app.auth.entity.Permission;
import com.friendify.app.auth.entity.User;
import com.friendify.app.auth.repository.InvalidatedTokenRepository;
import com.friendify.app.auth.repository.UserRepository;
import com.friendify.app.shared.exception.AppException;
import com.friendify.app.shared.exception.ErrorCode;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JwtService {

    static final String SCOPE_CLAIM = "scope";
    static final String ROLE_PREFIX = "ROLE_";

    UserRepository userRepository;
    InvalidatedTokenRepository invalidatedTokenRepository;

    @NonFinal
    @Value("${jwt.signer-key}")
    String signerKey;

    @NonFinal
    @Value("${jwt.valid-duration}")
    long validDuration;

    @NonFinal
    @Value("${jwt.issuer}")
    String issuer;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    long refreshableDuration;

    public String generateToken(User user) {
        User userWithRoles = userRepository.findByIdWithRolesAndPermissions(user.getId()).orElse(user);
        initializeLazyCollections(userWithRoles);
        JWSObject jwsObject = createSignedJwsObject(buildJwtClaimsSet(userWithRoles));

        try {
            jwsObject.sign(new MACSigner(signerKey.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException exception) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    public SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        SignedJWT signedJWT = SignedJWT.parse(token);
        JWSVerifier verifier = new MACVerifier(signerKey.getBytes());

        boolean isVerified = signedJWT.verify(verifier);
        Date expiryTime = calculateExpiryTime(signedJWT, isRefresh);
        boolean isExpired = expiryTime == null || !expiryTime.after(new Date());
        String jwtId = signedJWT.getJWTClaimsSet().getJWTID();
        boolean isRevoked = jwtId != null && invalidatedTokenRepository.existsById(jwtId);

        if (!isVerified || isExpired || isRevoked) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return signedJWT;
    }

    public boolean isValidToken(String token) {
        try {
            verifyToken(token, false);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    @Transactional
    public void revokeToken(String token) {
        try {
            SignedJWT signedJWT = verifyToken(token, true);
            String jwtId = signedJWT.getJWTClaimsSet().getJWTID();
            Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            revokeTokenById(jwtId, expiryTime);
        } catch (Exception exception) {
            // Expired or invalid tokens do not need a persisted revocation marker.
        }
    }

    @Transactional
    public void revokeTokenById(String jwtId, Date expiryTime) {
        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .id(jwtId)
                .expiryTime(expiryTime)
                .build();
        invalidatedTokenRepository.save(invalidatedToken);
    }

    private JWTClaimsSet buildJwtClaimsSet(User user) {
        Date now = new Date();
        Date expirationTime = new Date(Instant.now().plus(validDuration, ChronoUnit.SECONDS).toEpochMilli());

        return new JWTClaimsSet.Builder()
                .subject(user.getId())
                .issuer(issuer)
                .issueTime(now)
                .expirationTime(expirationTime)
                .jwtID(UUID.randomUUID().toString())
                .claim(SCOPE_CLAIM, buildScope(user))
                .build();
    }

    private JWSObject createSignedJwsObject(JWTClaimsSet claimsSet) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        return new JWSObject(header, new Payload(claimsSet.toJSONObject()));
    }

    private Date calculateExpiryTime(SignedJWT signedJWT, boolean isRefresh) throws ParseException {
        JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
        if (isRefresh) {
            Date issueTime = claimsSet.getIssueTime();
            if (issueTime == null) {
                return null;
            }
            return new Date(issueTime.toInstant().plus(refreshableDuration, ChronoUnit.SECONDS).toEpochMilli());
        }
        return claimsSet.getExpirationTime();
    }

    private String buildScope(User user) {
        if (CollectionUtils.isEmpty(user.getRoles())) {
            return "";
        }

        return user.getRoles().stream()
                .flatMap(role -> {
                    String roleScope = ROLE_PREFIX + role.getName();
                    if (CollectionUtils.isEmpty(role.getPermissions())) {
                        return java.util.stream.Stream.of(roleScope);
                    }
                    return java.util.stream.Stream.concat(
                            java.util.stream.Stream.of(roleScope),
                            role.getPermissions().stream().map(Permission::getName));
                })
                .collect(Collectors.joining(" "));
    }

    private void initializeLazyCollections(User user) {
        if (user.getRoles() != null) {
            user.getRoles().size();
            user.getRoles().forEach(role -> {
                if (role.getPermissions() != null) {
                    role.getPermissions().size();
                }
            });
        }
    }
}
