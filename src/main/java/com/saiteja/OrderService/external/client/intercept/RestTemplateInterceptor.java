package com.saiteja.OrderService.external.client.intercept;

import com.saiteja.OrderService.service.TokenService;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
@Configuration
public class RestTemplateInterceptor implements ClientHttpRequestInterceptor {

    private final TokenService tokenService;

    public RestTemplateInterceptor(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        String token = tokenService.extractToken();

        if(token != null) {
            request.getHeaders().add("Authorization", "Bearer " + token);
        }

        return execution.execute(request, body);
    }
}
