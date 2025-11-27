package com.reservalink.api.controller;

import com.reservalink.api.client.MercadoPagoClient;
import com.reservalink.api.service.booking.BookingService;
import com.reservalink.api.service.payment.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/payment")
public class PaymentController {

    private final MercadoPagoClient mpClient;
    private final PaymentService paymentService;
    private final BookingService bookingService;
    private static final String MERCADO_PAGO_WEBHOOK_TOPIC_PAYMENT = "payment";
    private static final String MERCADO_PAGO_WEBHOOK_TOPIC_SUBSCRIPTION = "preapproval";


    public PaymentController(MercadoPagoClient mpClient, PaymentService paymentService, BookingService bookingService) {
        this.mpClient = mpClient;
        this.paymentService = paymentService;
        this.bookingService = bookingService;
    }

   /* @PostMapping("/mercadopago/webhook")
    public ResponseEntity<Void> handleMercadoPagoWebhook(@RequestBody Map<String, Object> payload) {

        String topic = (String) payload.get("type");
        String resourceId = null;

        if (payload.containsKey("data")) {
            Map<String, String> dataMap = (Map<String, String>) payload.get("data");
            resourceId = dataMap.get("id");
        }

        if (topic == null || resourceId == null) {
            log.error("Webhook with Payload uncompleted: {}", payload);
            return ResponseEntity.ok().build(); //to avoid retries
        }

        log.info("Webhook received. Type: {}, ID: {}", topic, resourceId);

        if (MERCADO_PAGO_WEBHOOK_TOPIC_PAYMENT.equals(topic)) {
            try {
                 Long paymentId = Long.parseLong(resourceId);
                 Payment paymentDetails = mpClient.getPaymentDetails(paymentId);
                 if (paymentDetails != null) {
                     paymentService.processPaymentWebhook(paymentDetails);
                 }
                 else {
                     log.error("Unexpected error: could not fetch details for paymentId: {}.", paymentId);
                }
                log.info("Processing payment ID: {}", paymentId);

            } catch (Exception e) {
                log.error("Invalid ID format received: {}", resourceId, e);
                return ResponseEntity.ok().build(); //to avoid retries
            }
        } else {
            log.info("Topic '{}' couldn't be processed.", topic);
        }

        // Siempre devolver 200 OK
        return ResponseEntity.ok().build();
    }


    @PostMapping("/create-preapproval")
    public ResponseEntity<String> createPreapproval(@Valid @RequestBody SubscriptionRequest request) {
        String initPoint = mpClient.createPreapproval(request);
        return ResponseEntity.ok(initPoint);
    }*/

    @PostMapping("/mercadopago/webhook/booking")
    public ResponseEntity<Void> handleBookingWebhook(@RequestBody Map<String, Object> payload) {
        log.info("Booking Payment received: {}", payload);
        try {
            Map<String, Object> data = (Map<String, Object>) payload.get("data");
            String paymentId = (String) data.get("id");
            String externalId = paymentService.processBookingWebhook(paymentId);
            if (externalId != null) {
                bookingService.confirmBooking(externalId);
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error processing Mercado Pago webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


}
