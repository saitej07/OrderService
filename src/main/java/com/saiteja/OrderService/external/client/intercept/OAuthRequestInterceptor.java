package com.saiteja.OrderService.external.client.intercept;

import com.saiteja.OrderService.service.TokenService;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OAuthRequestInterceptor implements RequestInterceptor {

    private final TokenService tokenService;

    public OAuthRequestInterceptor(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public void apply(RequestTemplate template) {
        String token = tokenService.extractToken();

        if(token != null) {
            template.header("Authorization", "Bearer " + token);
        }
    }
}
