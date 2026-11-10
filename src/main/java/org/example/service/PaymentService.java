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
    public PaymentTransaction transferMoney(Long senderId, Long recipientId, BigDecimal amount){
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумата за превод трябва да бъде по-голяма от нула!");
        }

        User sender = userRepository.findByIdWithLock(senderId)
                .orElseThrow(() -> new UserNotFoundException("Подател с ID: " + senderId + " не беше намерен."));
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new UserNotFoundException("Получателят с ID: " + recipientId + " не беше намерен."));

        if (sender.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Недостатъчна наличност. Текущ баланс: " + sender.getBalance() + ", искана сума: " + amount);
        }

        sender.setBalance(sender.getBalance().subtract(amount));
        recipient.setBalance(recipient.getBalance().add(amount));

        userRepository.save(sender);
        userRepository.save(recipient);

        try {
            // Създаваме успешна транзакция с новия конструктор
            PaymentTransaction transaction = new PaymentTransaction(senderId, recipientId, amount);
            transaction.setStatus(TransactionStatus.SUCCESS);

            // Записваме я и я ВРЪЩАМЕ към контролера
            return transactionRepository.save(transaction);

        } catch (RuntimeException e) {
            PaymentTransaction failedTransaction = new PaymentTransaction(senderId, recipientId, amount);
            failedTransaction.setStatus(TransactionStatus.FAILED);
            failedTransaction.setErrorMessage(e.getMessage());
            transactionRepository.save(failedTransaction);
            throw e;
        }



    }
}
