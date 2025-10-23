package com.gomeschristopher.payments.service;

import com.gomeschristopher.payments.dto.PaymentDTO;
import com.gomeschristopher.payments.http.OrderClient;
import com.gomeschristopher.payments.model.Payment;
import com.gomeschristopher.payments.model.Status;
import com.gomeschristopher.payments.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository repository;

    @Autowired
    private OrderClient order;

    public Page<PaymentDTO> getAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(PaymentDTO::new);
    }

    public PaymentDTO findById(Long id) {
        var payment = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        return new PaymentDTO(payment);
    }

    public PaymentDTO create(PaymentDTO dto) {
        Payment payment = new Payment();
        payment.setStatus(Status.CREATED);
        return getPaymentDTO(dto, payment);
    }

    public PaymentDTO update(Long id, PaymentDTO dto) {
        Payment payment = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        payment.setStatus(dto.status());
        return getPaymentDTO(dto, payment);
    }

    private PaymentDTO getPaymentDTO(PaymentDTO dto, Payment payment) {
        payment.setValue(dto.value());
        payment.setName(dto.name());
        payment.setNumber(dto.number());
        payment.setExpiration(dto.expiration());
        payment.setCode(dto.code());
        payment.setOrderId(dto.orderId());
        payment.setPaymentMethodId(dto.paymentMethodId());
        Payment updatedPayment = repository.save(payment);
        return new PaymentDTO(updatedPayment);
    }

    public void delete(Long id) {
        Payment payment = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        repository.delete(payment);
    }

    public void confirmPayment(Long id){
        Optional<Payment> payment = repository.findById(id);

        if (!payment.isPresent()) {
            throw new EntityNotFoundException();
        }

        payment.get().setStatus(Status.CONFIRMED);
        repository.save(payment.get());
        order.updatePayment(payment.get().getOrderId());
    }
}
