package com.saiteja.OrderService.service;

import com.saiteja.OrderService.model.OrderRequest;
import com.saiteja.OrderService.model.OrderResponse;

public interface OrderService {

    long placeOrder(OrderRequest orderRequest);

    OrderResponse getOrderDetails(long orderId);
}
