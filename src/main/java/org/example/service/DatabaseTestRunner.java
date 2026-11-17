package org.example.service;

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

    public DatabaseTestRunner(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        if(userRepository.count() == 0){
            User user1 = new User("ivan_ivanov","ivan@example.com");
            user1.setBalance(new BigDecimal("1000.00"));

            User user2 = new User("maria_petrova","maria@example.com");
            user2.setBalance(new BigDecimal("500.00"));

            userRepository.save(user1);
            userRepository.save(user2);

            System.out.println("--- Created test users: ivan_ivanov(1000.00) and maria_petrova(500.00) ---");
        }
    }


}
