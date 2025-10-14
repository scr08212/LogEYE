package com.nkm.logeye.domain.test;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/test")
public class TestController {
    @GetMapping("/me")
    public ResponseEntity<String> getMyInfo(@AuthenticationPrincipal UserDetails userDetails) {
        // @AuthenticationPrincipal 어노테이션을 사용하면
        // SecurityContextHolder에서 현재 사용자의 정보를 직접 주입받을 수 있다.
        if (userDetails == null) {
            return ResponseEntity.ok("인증 정보가 없습니다.");
        }
        return ResponseEntity.ok("현재 로그인된 사용자: " + userDetails.getUsername());
    }
}
