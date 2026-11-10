package org.example.controller;

import org.example.model.PaymentTransaction;
import org.example.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService){
        this.paymentService = paymentService;
    }

    @PostMapping("/transfer")
    public ResponseEntity<PaymentTransaction> transferMoney(
            @RequestParam Long senderId,
            @RequestParam Long recipientId,
            @RequestParam BigDecimal amount){
        PaymentTransaction transaction =  paymentService.transferMoney(senderId, recipientId, amount);
        return ResponseEntity.ok(transaction);
    }

}
