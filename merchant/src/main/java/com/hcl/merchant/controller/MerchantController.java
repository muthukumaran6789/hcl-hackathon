package com.hcl.merchant.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/merchant")
public class MerchantController {
    @GetMapping
    public String Merchant() {
        return "Hello, Merchant!";
    }
}
