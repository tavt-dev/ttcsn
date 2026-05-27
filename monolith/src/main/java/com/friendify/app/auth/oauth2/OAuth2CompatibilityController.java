package com.friendify.app.auth.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class OAuth2CompatibilityController {

    @GetMapping("/api/v1/identity/oauth2/authorization/{registrationId}")
    public String startOAuth2Login(@PathVariable String registrationId) {
        return "redirect:/oauth2/authorization/" + registrationId;
    }

    @GetMapping("/api/v1/identity/login/oauth2/code/{registrationId}")
    public String handleOAuth2Callback(@PathVariable String registrationId, HttpServletRequest request) {
        String queryString = request.getQueryString();
        String target = "/login/oauth2/code/" + registrationId;
        if (queryString == null || queryString.isBlank()) {
            return "redirect:" + target;
        }
        return "redirect:" + target + "?" + queryString;
    }
}
