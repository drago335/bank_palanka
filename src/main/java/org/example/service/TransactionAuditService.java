package org.example.service;

import org.example.model.PaymentTransaction;
import org.example.model.TransactionStatus;
import org.example.repository.PaymentTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class TransactionAuditService {
    private final PaymentTransactionRepository transactionRepository;

    public TransactionAuditService(PaymentTransactionRepository transactionRepository){
        this.transactionRepository = transactionRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logFailedTransaction(Long senderId, Long recipientId, BigDecimal amount, String errorMessage){
        PaymentTransaction failedTransaction = new PaymentTransaction(
                senderId,
                recipientId,
                amount,
                TransactionStatus.FAILED
        );
        failedTransaction.setErrorMessage(errorMessage);
        transactionRepository.save(failedTransaction);
    }
}
