package com.friendify.app.chat.configuration;

import java.util.Arrays;
import java.util.List;

import com.friendify.app.auth.configuration.CustomJwtDecoder;
import com.friendify.app.shared.exception.AppException;
import com.friendify.app.shared.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    CustomJwtDecoder jwtDecoder;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();
        if (StompCommand.CONNECT.equals(command)) {
            authenticateFromHeader(accessor, true);
        } else if (StompCommand.SEND.equals(command) || StompCommand.SUBSCRIBE.equals(command)) {
            Object userPrincipal = accessor.getUser();
            if (userPrincipal instanceof Authentication authentication) {
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                authenticateFromHeader(accessor, false);
            }
        }
        return message;
    }

    private void authenticateFromHeader(StompHeaderAccessor accessor, boolean required) {
        List<String> authHeaders = accessor.getNativeHeader("Authorization");
        if (authHeaders == null || authHeaders.isEmpty()) {
            if (required) {
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }
            return;
        }

        String authHeader = authHeaders.get(0);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }

        String token = authHeader.substring(7);
        try {
            Jwt jwt = jwtDecoder.decode(token);
            Authentication authentication = new JwtAuthenticationToken(jwt, authorities(jwt));
            accessor.setUser(authentication);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (JwtException exception) {
            throw new AppException(mapJwtExceptionToErrorCode(exception));
        } catch (RuntimeException exception) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
    }

    private List<SimpleGrantedAuthority> authorities(Jwt jwt) {
        Object scopeClaim = jwt.getClaims().get("scope");
        if (scopeClaim instanceof String scope && !scope.isEmpty()) {
            return Arrays.stream(scope.split("\\s+"))
                    .filter(value -> !value.isEmpty())
                    .map(SimpleGrantedAuthority::new)
                    .toList();
        }
        if (scopeClaim instanceof List<?> scopes) {
            return scopes.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .map(SimpleGrantedAuthority::new)
                    .toList();
        }
        return List.of();
    }

    private ErrorCode mapJwtExceptionToErrorCode(JwtException exception) {
        String message = exception.getMessage();
        if (message != null) {
            if (message.contains("missing expiration") || message.contains("Token missing expiration")) {
                return ErrorCode.TOKEN_MISSING_EXPIRATION;
            }
            if (message.contains("expired") || message.contains("Token expired")) {
                return ErrorCode.TOKEN_EXPIRED;
            }
        }
        return ErrorCode.INVALID_TOKEN;
    }
}
