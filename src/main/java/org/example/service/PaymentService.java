package org.example.service;

import org.example.model.PaymentTransaction;
import org.example.model.User;
import org.example.repository.PaymentTransactionRepository;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class PaymentService {

    private final UserRepository userRepository;
    private final PaymentTransactionRepository transactionRepository;

    public PaymentService(UserRepository userRepository,PaymentTransactionRepository transactionRepository){
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public void transferMoney(Long senderId, Long recipientId, BigDecimal amount){
        if(amount == null || amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("Сумата за превод трябва да бъде по-голяма от нула!");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Подателят не е намерен с ID: " + senderId));

        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new RuntimeException("Получтелят не е намерен с ID: " + recipientId));

        if(sender.getBalance().compareTo(amount) < 0){
            throw new RuntimeException("Недостатъчна наличност по сметката на подателя!");
        }

        sender.setBalance(sender.getBalance().subtract(amount));
        recipient.setBalance(recipient.getBalance().add(amount));

        userRepository.save(sender);
        userRepository.save(recipient);

        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setAmount(amount);
        transaction.setRecipient(recipient.getUsername());
        transaction.setTimestamp(LocalDateTime.now());

        transactionRepository.save(transaction);

    }
}
