package org.example.service;

import org.example.exception.InsufficientFundsException;
import org.example.exception.UserNotFoundException;
import org.example.model.PaymentTransaction;
import org.example.model.TransactionStatus;
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
        User sender = null;
        User recipient = null;
        try {
            sender = userRepository.findById(senderId)
                    .orElseThrow(() -> new UserNotFoundException("Подател с ID: " + senderId + "не беше намерен."));
            recipient = userRepository.findById(recipientId)
                    .orElseThrow(() -> new UserNotFoundException("Получателят с ID: " + recipientId + "не беше намерен."));

            if (sender.getBalance().compareTo(amount) < 0){
                throw new InsufficientFundsException("Недостатъчна наличност. Текущ баланс: " + sender.getBalance() + ", искана сума: " + amount);
            }

            sender.setBalance(sender.getBalance().subtract(amount));
            recipient.setBalance(recipient.getBalance().add(amount));

            userRepository.save(sender);
            userRepository.save(recipient);

            PaymentTransaction transaction = new PaymentTransaction(amount,recipient.getUsername());
            transaction.setStatus(TransactionStatus.SUCCESS);
            transactionRepository.save(transaction);
        }catch (RuntimeException e){
            PaymentTransaction failedTransaction = new PaymentTransaction(amount, recipient != null ? recipient.getUsername() : "UNKNOWN");
            failedTransaction.setStatus(TransactionStatus.FAILED);
            failedTransaction.setErrorMessage(e.getMessage());
            transactionRepository.save(failedTransaction);

            throw e;
        }



    }
}
