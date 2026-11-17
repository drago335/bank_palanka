package org.example.service;

import org.example.exception.InsufficientFundsException;
import org.example.exception.UserNotFoundException;
import org.example.model.PaymentTransaction;
import org.example.model.TransactionStatus;
import org.example.model.User;
import org.example.repository.PaymentTransactionRepository;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentTransactionRepository transactionRepository;

    @Mock
    private TransactionAuditService auditService;

    @InjectMocks
    private PaymentService paymentService;

    private User sender;
    private User receiver;

    @BeforeEach
    void setUp(){
        sender = new User("ivan_ivanov", "ivan@example.com");
        sender.setId(1L);
        sender.setBalance(new BigDecimal("500.00"));

        receiver = new User("maria_petrova", "maria@example.com");
        receiver.setId(2L);
        receiver.setBalance(new BigDecimal("200.00"));
    }

    @Test
    void testSuccessfulTransfer(){
        BigDecimal transferAmount = new BigDecimal("100.00");

        when(userRepository.findByIdWithLock(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findByIdWithLock(2L)).thenReturn(Optional.of(receiver));
        when(transactionRepository.save(any(PaymentTransaction.class)))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        PaymentTransaction result = paymentService.transferMoney(1L,2L, transferAmount);

        assertNotNull(result);
        assertEquals(TransactionStatus.SUCCESS, result.getStatus());
        assertEquals(new BigDecimal("400.00"), sender.getBalance());
        assertEquals(new BigDecimal("300.00"), receiver.getBalance());

        verify(userRepository, times(1)).save(sender);
        verify(userRepository, times(1)).save(receiver);
        verify(transactionRepository, times(1)).save(any(PaymentTransaction.class));
        verifyNoInteractions(auditService);
    }

    @Test
    void testTransferThrowsExceptionWhenInsufficientFunds() {
        BigDecimal transferAmount = new BigDecimal("600.00");

        when(userRepository.findByIdWithLock(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findByIdWithLock(2L)).thenReturn(Optional.of(receiver));

        InsufficientFundsException exception = assertThrows(
                InsufficientFundsException.class,
                () -> paymentService.transferMoney(1L, 2L, transferAmount)
        );
        assertNotNull(exception.getMessage());
        assertEquals(new BigDecimal("500.00"),sender.getBalance());
        assertEquals(new BigDecimal("200.00"), receiver.getBalance());

        verify(auditService, times(1)).logFailedTransaction(eq(1L),eq(2L),eq(transferAmount), anyString());
        verify(userRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void testTransferThrowsExceptionWhenUserNotFound() {
        when(userRepository.findByIdWithLock(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findByIdWithLock(2L)).thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () ->paymentService.transferMoney(1L,2L,new BigDecimal("50.00"))
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void testTransferThrowsExceptionWhenSameAccount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.transferMoney(1L, 1L, new BigDecimal("50.00"))
        );

        verifyNoInteractions(userRepository, transactionRepository, auditService);
    }

    @Test
    void testTransferThrowsExceptionWhenAmountIsNegativeOrZero() {
        assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.transferMoney(1L, 2L, BigDecimal.ZERO)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.transferMoney(1L, 2L, new BigDecimal("-10.00"))
        );

        verifyNoInteractions(userRepository, transactionRepository, auditService);
    }
}
