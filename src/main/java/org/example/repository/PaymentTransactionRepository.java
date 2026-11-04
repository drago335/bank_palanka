package org.example.repository;

import org.example.model.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

}
