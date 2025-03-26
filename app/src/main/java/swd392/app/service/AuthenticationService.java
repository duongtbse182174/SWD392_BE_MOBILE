package swd392.app.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import swd392.app.dto.request.AuthenticationRequest;
import swd392.app.dto.request.IntrospectRequest;
import swd392.app.dto.request.LogoutRequest;
import swd392.app.dto.response.AuthenticationResponse;
import swd392.app.dto.response.IntrospectResponse;
import swd392.app.entity.InvalidatedToken;
import swd392.app.entity.User;
import swd392.app.exception.AppException;
import swd392.app.exception.ErrorCode;
import swd392.app.repository.InvalidatedTokenRepository;
import swd392.app.repository.UserRepository;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.text.ParseException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    InvalidatedTokenRepository invalidatedTokenRepository;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_NOT_EXIST));

        boolean authenticated = passwordEncoder.matches(request.getPassword(),
                user.getPassword());

        if (!authenticated)
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        var token = generateToken(user);
        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .userCode(user.getUserCode())
                .userName(user.getUserName())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    private String generateToken(User user) {
        // Tạo header thủ công với thứ tự alg trước typ
        StringBuilder headerBuilder = new StringBuilder();
        headerBuilder.append("{");
        headerBuilder.append("\"alg\":\"HS512\",");
        headerBuilder.append("\"typ\":\"JWT\"");
        headerBuilder.append("}");

        // Mã hóa header thành Base64 URL-safe
        String headerBase64 = Base64URL.encode(headerBuilder.toString()).toString();

        // Tạo payload với thứ tự claim chính xác
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{");
        jsonBuilder.append("\"jti\":\"").append(UUID.randomUUID().toString()).append("\",");
        jsonBuilder.append("\"userId\":\"").append(user.getUserId().toString()).append("\",");
        jsonBuilder.append("\"userCode\":\"").append(user.getUserCode()).append("\",");
        jsonBuilder.append("\"role\":\"").append(user.getRole().getRoleType()).append("\",");
        jsonBuilder.append("\"username\":\"").append(user.getUserName()).append("\",");
        jsonBuilder.append("\"warehouseCode\":\"").append(user.getWarehouse().getWarehouseCode()).append("\",");
        jsonBuilder.append("\"iat\":").append(new Date().getTime() / 1000).append(",");
        jsonBuilder.append("\"exp\":").append(Instant.now().plus(24, ChronoUnit.HOURS).toEpochMilli() / 1000);
        jsonBuilder.append("}");

        // Mã hóa payload thành Base64 URL-safe
        String payloadBase64 = Base64URL.encode(jsonBuilder.toString()).toString();

        // Tạo chuỗi để ký: headerBase64.payloadBase64
        String signingInput = headerBase64 + "." + payloadBase64;

        // Ký chuỗi bằng HMAC-SHA512
        byte[] signatureBytes;
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(SIGNER_KEY.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            signatureBytes = mac.doFinal(signingInput.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Không thể ký token", e);
            throw new RuntimeException(e);
        }

        // Mã hóa chữ ký thành Base64 URL-safe
        String signatureBase64 = Base64URL.encode(signatureBytes).toString();

        // Tạo token hoàn chỉnh: headerBase64.payloadBase64.signatureBase64
        return headerBase64 + "." + payloadBase64 + "." + signatureBase64;
    }

    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");

        if (user.getRole() != null) {
            stringJoiner.add(user.getRole().getRoleType());
        }

        return stringJoiner.toString();
    }

    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getToken();
        boolean isValid = true;

        try {
            verifyToken(token);
        } catch (AppException e) {
            isValid = false;
        }

        return IntrospectResponse.builder().valid(isValid).build();
    }

    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        try {
            var signToken = verifyToken(request.getToken());

            String jit = signToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

            InvalidatedToken invalidatedToken =
                    InvalidatedToken.builder().id(jit).expiryTime(expiryTime).build();

            invalidatedTokenRepository.save(invalidatedToken);
        } catch (AppException exception) {
            log.info("Token đã hết hạn");
        }
    }

    private SignedJWT verifyToken(String token) throws ParseException, JOSEException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes(StandardCharsets.UTF_8));

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);

        if (!(verified && expiryTime.after(new Date())))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        return signedJWT;
    }
}