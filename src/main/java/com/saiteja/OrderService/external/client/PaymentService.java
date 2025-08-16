package com.saiteja.OrderService.external.client;

import com.saiteja.OrderService.exception.CustomException;
import com.saiteja.OrderService.external.client.request.PaymentRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "PAYMENT-SERVICE/payment")
//@FeignClient(name = "payment", url = "${microservices.payment}")
@CircuitBreaker(name = "external", fallbackMethod = "fallback")
public interface PaymentService {

    @PostMapping
    public ResponseEntity<Long> doPayment(@RequestBody PaymentRequest paymentRequest);

    default void fallback(Exception e) {
        throw new CustomException("Payment Service is not available", "UNAVAILABLE", 500);
    }
}
