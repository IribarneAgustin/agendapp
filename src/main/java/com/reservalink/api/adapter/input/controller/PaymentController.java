package com.reservalink.api.adapter.input.controller;

import com.reservalink.api.adapter.input.controller.request.MpWebhookRequest;
import com.reservalink.api.adapter.input.controller.request.MpWebhookType;
import com.reservalink.api.application.service.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/mercadopago/webhook")
    public ResponseEntity<Void> handleMpWebhook(@RequestBody MpWebhookRequest payload) {
        log.info("Webhook received: {}", payload);
        try {
            MpWebhookType type = payload.typeEnum();
            String id = payload.data() != null ? payload.data().id() : null;
            if (MpWebhookType.PAYMENT == type) {
                paymentService.processPaymentWebhook(id);
            } else {
                log.warn("Unhandled webhook type: {}", payload.type());
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error processing MP webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


}
