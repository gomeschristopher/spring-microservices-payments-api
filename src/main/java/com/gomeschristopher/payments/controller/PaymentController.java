package com.gomeschristopher.payments.controller;

import com.gomeschristopher.payments.dto.PaymentDTO;
import com.gomeschristopher.payments.service.PaymentService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private PaymentService service;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @GetMapping
    public Page<PaymentDTO> list(@PageableDefault Pageable pageable) {
        return service.getAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<PaymentDTO> create(@RequestBody PaymentDTO dto) {
        PaymentDTO createdPayment = service.create(dto);


        //Message message = new Message(("Payment created with ID: " + createdPayment.id()).getBytes());
        rabbitTemplate.convertAndSend("pagamentos.ex", "", createdPayment);

        return ResponseEntity.ok(createdPayment);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentDTO> update(@PathVariable Long id, @RequestBody PaymentDTO dto) {
        PaymentDTO updatedPayment = service.update(id, dto);
        return ResponseEntity.ok(updatedPayment);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/confirm")
    @CircuitBreaker(name = "updateOrder", fallbackMethod = "confirmPaymentFallback")
    public void confirmPayment(@PathVariable Long id) {
        service.confirmPayment(id);
    }

    public void confirmPaymentFallback(Long id, Throwable t) {
        System.out.println("Falha ao confirmar pagamento para o pedido ID: " + id + ". Motivo: " + t.getMessage());
    }
}
