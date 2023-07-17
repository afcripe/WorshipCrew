package net.dahliasolutions.controllers;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.services.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/order")
@RequiredArgsConstructor
public class OrderAPIController {

    private final OrderService orderService;

    @GetMapping("")
    public String getOrders(){ return null; }
}
