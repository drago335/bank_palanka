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
    private final TransactionAuditService auditService;

    public PaymentService(UserRepository userRepository,PaymentTransactionRepository transactionRepository,TransactionAuditService auditService){
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.auditService = auditService;
    }

    @Transactional
    public PaymentTransaction transferMoney(Long senderId, Long recipientId, BigDecimal amount){
        if(senderId == null || recipientId == null){
            throw new IllegalArgumentException("Идентификаторитена подателя и получателя са задължителни.");
        }
        if(senderId.equals(recipientId)){
            throw new IllegalArgumentException("Не можете да завършите превод към собствения си акаунт.");
        }
        if(amount == null || amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("Сумата за превод трябва да бъде по-голяма от нула!");
        }

        Long firstLockId = Math.min(senderId, recipientId);
        Long secondLockId =Math.max(senderId, recipientId);

        User firstUser = userRepository.findByIdWithLock(firstLockId)
                .orElseThrow(() -> new UserNotFoundException("Потребител с ID: " + firstLockId + "не беше намерен."));
        User secondUser = userRepository.findByIdWithLock(secondLockId)
                .orElseThrow(() -> new UserNotFoundException("Потребител с ID: " + secondLockId + "не беше намерен."));

        User sender = senderId.equals(firstLockId) ? firstUser : secondUser;
        User recipient = recipientId.equals(firstLockId) ? firstUser : secondUser;

        if(sender. getBalance().compareTo(amount) < 0){
            String errorMsg = "Недостатъчна наличност. Текущ баланс: " + sender.getBalance() + ", искана сума: " + amount;
            auditService.logFailedTransaction(senderId, recipientId, amount, errorMsg);
            throw new InsufficientFundsException(errorMsg);
        }

        sender.setBalance(sender.getBalance().subtract(amount));
        recipient.setBalance(recipient.getBalance().add(amount));

        userRepository.save(sender);
        userRepository.save(recipient);

        PaymentTransaction transaction = new PaymentTransaction(senderId,recipientId,amount,TransactionStatus.SUCCESS);

        return transactionRepository.save(transaction);
    }
}
