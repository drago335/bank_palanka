package org.example;

import org.example.model.PaymentTransaction;
import org.example.model.User;
import org.example.repository.PaymentTransactionRepository;
import org.example.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DatabaseTestRunner implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PaymentTransactionRepository transactionRepository;

    public DatabaseTestRunner(UserRepository userRepository,PaymentTransactionRepository transactionRepository){
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public void run(String... args) throws Exception{
        System.out.println("--- ТЕСТ НА БАЗАТА ДАННИ ЗАПОЧВА ---");
        User user = new User("ivan_ivanov", "ivan@example.com");
        userRepository.save(user);
        System.out.println("Успешно записен потребител в базата: " + user.getUsername());

        PaymentTransaction transaction = new PaymentTransaction(new BigDecimal(160.60), "Electriicity Bill");
        transactionRepository.save(transaction);
        System.out.println("Успешно записана транзакция на стойност: " + transaction.getAmount() + "лв.");
        System.out.println("--- Database work perfect! ---");
    }


}
