package com.saiteja.OrderService.service;

import com.saiteja.OrderService.entity.Order;
import com.saiteja.OrderService.exception.CustomException;
import com.saiteja.OrderService.external.client.PaymentService;
import com.saiteja.OrderService.external.client.ProductService;
import com.saiteja.OrderService.external.client.request.PaymentRequest;
import com.saiteja.OrderService.external.client.response.PaymentResponse;
import com.saiteja.OrderService.model.OrderRequest;
import com.saiteja.OrderService.model.OrderResponse;
import com.saiteja.OrderService.model.ProductResponse;
import com.saiteja.OrderService.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private RestTemplate restTemplate;

//    @Value("${microservices.product}")
//    private String productServiceUrl;
//
//    @Value("${microservices.payment}")
//    private String paymentServiceUrl;


    @Override
    public long placeOrder(OrderRequest orderRequest) {
        log.info("Order placed request: {}", orderRequest);

        productService.reduceQuantity(orderRequest.getProductId(), orderRequest.getQuantity());

        log.info("Creating Order with Status CREATED");

        Order order = Order.builder()
                .productId(orderRequest.getProductId())
                .quantity(orderRequest.getQuantity())
                .amount(orderRequest.getTotalAmount())
                .orderDate(Instant.now())
                .orderStatus("CREATED")
                .build();
        order = orderRepository.save(order);

        log.info("Calling Payment service to complete the payment");

        PaymentRequest paymentRequest = PaymentRequest.builder()
                .orderId(order.getId())
                .paymentMode(orderRequest.getPaymentMode())
                .amount(orderRequest.getTotalAmount())
                .build();

        String orderStatus = null;
        try {
            paymentService.doPayment(paymentRequest);
            log.info("Payment done Successfully.. Changing the order status to placed");
            orderStatus = "PLACED";
        } catch (Exception e) {
            log.error("Error occurred in payment.. Changing order status to PAYMENT_FAILED");
            orderStatus = "PAYMENT_FAILED";
        }

        order.setOrderStatus(orderStatus);
        orderRepository.save(order);

        log.info("Order Placed successfully with orderId: {}", order.getId());
        return order.getId();
    }

    @Override
    public OrderResponse getOrderDetails(long orderId) {
        log.info("Get Order details for order Id: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException("Order not found for the order Id: " + orderId, "NOT_FOUND", 404));

        log.info("Invoking order service to fetch the product for Id: {}", order.getProductId());

        ProductResponse productResponse = restTemplate.getForObject("http://PRODUCT-SERVICE/api/v1/product/" + order.getProductId(), ProductResponse.class);

        //ProductResponse productResponse = restTemplate.getForObject(productServiceUrl + order.getProductId(), ProductResponse.class);

        log.info("Getting payment information from the payment service");
        PaymentResponse paymentResponse = restTemplate.getForObject("http://PAYMENT-SERVICE/api/v1/payment/" + order.getId(), PaymentResponse.class);
        //PaymentResponse paymentResponse = restTemplate.getForObject(paymentServiceUrl + order.getId(), PaymentResponse.class);


        OrderResponse.ProductDetails productDetails = OrderResponse.ProductDetails.builder()
                .productName(productResponse.getProductName())
                .productId(productResponse.getProductId())
                .quantity(productResponse.getQuantity())
                .price(productResponse.getPrice())
                .build();

        OrderResponse.PaymentDetails paymentDetails = OrderResponse.PaymentDetails.builder()
                .paymentId(paymentResponse.getPaymentId())
                .paymentStatus(paymentResponse.getStatus())
                .paymentDate(paymentResponse.getPaymentDate())
                .paymentMode(paymentResponse.getPaymentMode())
                .build();

        OrderResponse orderResponse = OrderResponse.builder()
                .orderId(order.getId())
                .orderDate(order.getOrderDate())
                .orderStatus(order.getOrderStatus())
                .amount(order.getAmount())
                .productDetails(productDetails)
                .paymentDetails(paymentDetails)
                .build();

        return orderResponse;
    }
}
