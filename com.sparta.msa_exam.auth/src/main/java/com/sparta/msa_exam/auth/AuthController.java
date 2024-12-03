package com.sparta.msa_exam.auth;

import com.sparta.msa_exam.auth.core.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/auth/sign-in")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody SignInRequest signInRequest) {

        String token = authService.signIn(signInRequest.getUserId(), signInRequest.getPassword());

        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/auth/sign-up")
    public ResponseEntity<?> signUp(@RequestBody User user) {
        User createdUser = authService.signUp(user);

        return ResponseEntity.ok(createdUser);
    }

    @GetMapping("/validate-user/{userId}")
    public ResponseEntity<Boolean> validateUser(@PathVariable String userId) {
        try {
            boolean isRegistered = authService.isUserRegistered(userId); // 유저 확인 로직

            if (!isRegistered) {
                log.warn("User not found: " + userId); // 유저가 없으면 경고 로그 추가
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(false);
            }

            return ResponseEntity.ok(true);
        } catch (Exception e) {
            log.error("Error validating user: " + userId, e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(false);
        }
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class AuthResponse {
        private String access_token;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class SignInRequest {
        private String userId;
        private String password;
    }
}

