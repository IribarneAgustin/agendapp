package com.reservalink.api.controller;

import com.reservalink.api.controller.request.MpWebhookRequest;
import com.reservalink.api.controller.request.MpWebhookType;
import com.reservalink.api.service.booking.BookingService;
import com.reservalink.api.service.payment.PaymentService;
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
public class PaymentController {

    private final PaymentService paymentService;
    private final BookingService bookingService;

    public PaymentController(PaymentService paymentService, BookingService bookingService) {
        this.paymentService = paymentService;
        this.bookingService = bookingService;
    }

    @PostMapping("/mercadopago/webhook")
    public ResponseEntity<Void> handleMpWebhook(@RequestBody MpWebhookRequest payload) {
        log.info("Webhook received: {}", payload);
        try {
            MpWebhookType type = payload.typeEnum();
            String id = payload.data() != null ? payload.data().id() : null;
            if (MpWebhookType.PAYMENT == type) {
                String externalId = paymentService.processPaymentWebhook(id);
                if (externalId != null && !externalId.startsWith("SUBSCRIPTION-")) {
                    bookingService.confirmBooking(externalId);
                }
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
