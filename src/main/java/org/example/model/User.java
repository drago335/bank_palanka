package org.example.model;

import jakarta.persistence.*;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    public User(){
    }

    public User(String username,String email){
        this.username = username;
        this.email = email;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId(){
        return id;
    }
    public void  setId(Long id){
        this.id = id;
    }
    public String getUsername(){
        return username;
    }
    public void setUsername(String username){
        this.username=username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
