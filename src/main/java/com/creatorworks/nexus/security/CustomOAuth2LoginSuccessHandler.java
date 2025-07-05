package com.creatorworks.nexus.security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component // 이 클래스를 Spring Bean으로 등록합니다.
public class CustomOAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        HttpSession session = request.getSession();

        // SocialMemberService에서 저장한 "needsAdditionalInfo" 플래그를 가져옵니다.
        Boolean needsAdditionalInfo = (Boolean) session.getAttribute("needsAdditionalInfo");

        // 기본적으로 성공 시 이동할 URL
        String targetUrl = "/";

        if (needsAdditionalInfo != null && needsAdditionalInfo) {
            // 추가 정보가 필요하다고 판단되면, 타겟 URL을 추가 정보 입력 페이지로 변경합니다.
            targetUrl = "/social/addInfo";
            // 사용했던 세션 플래그는 여기서 지워주는 것이 좋습니다. (컨트롤러에서 처리해도 무방)
            // session.removeAttribute("needsAdditionalInfo");
        }

        // URL 설정 및 리다이렉트
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
