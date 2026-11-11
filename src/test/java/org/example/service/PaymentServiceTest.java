package org.example.service;

import org.example.exception.InsufficientFundsException;
import org.example.model.PaymentTransaction;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentTransactionRepository transactionRepository;

    @InjectMocks
    private PaymentService paymentService;

    private Long senderId;
    private Long receiverId;
    private User sender;
    private User receiver;

    @BeforeEach
    void setUp(){
        senderId = 1L;
        receiverId = 2L;

        sender = new User();
        sender.setId(senderId);

        receiver = new User();
        receiver.setId(receiverId);
    }


    @Test
    void testSuccessfulTransfer(){
        BigDecimal transferAmount = new BigDecimal("100.00");

        User sender = new User();
        sender.setId(senderId);
        sender.setBalance(new BigDecimal("500.00"));

        User receiver = new User();
        receiver.setId(receiverId);
        receiver.setBalance(new BigDecimal("200.00"));

        when(userRepository.findByIdWithLock(senderId)).thenReturn(Optional.of(sender));
        when(userRepository.findById(receiverId)).thenReturn(Optional.of(receiver));
        lenient().when(transactionRepository.save(any(PaymentTransaction.class)))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));


        when(transactionRepository.save(any(PaymentTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> paymentService.transferMoney(senderId, receiverId, transferAmount));

        assertEquals(new BigDecimal("400.00"), sender.getBalance());
        assertEquals(new BigDecimal("300.00"), receiver.getBalance());

        verify(userRepository, times(1)).save(sender);
        verify(userRepository, times(1)).save(receiver);
        verify(transactionRepository,times(1)).save(any(PaymentTransaction.class));

    }

    @Test
    void testTransferThrowsExceptionWhenInsufficientFunds() {
        BigDecimal transferAmount = new BigDecimal("100.00");

        User sender = new User();
        sender.setId(senderId);
        sender.setBalance(new BigDecimal("50.00"));

        User receiver = new User();
        receiver.setId(receiverId);
        receiver.setBalance(new BigDecimal("200.00"));

        when(userRepository.findByIdWithLock(senderId)).thenReturn(Optional.of(sender));
        when(userRepository.findById(receiverId)).thenReturn(Optional.of(receiver));

        InsufficientFundsException exception = assertThrows(
                InsufficientFundsException.class,
                () -> paymentService.transferMoney(senderId, receiverId, transferAmount)
        );

        assertNotNull(exception.getMessage());
    }
}
