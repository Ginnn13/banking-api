package com.nogicode.banking_service.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "acounts")

public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "account_name",nullable = false,unique = true)
    private String accountNumber;

    @Column(name = "balance",nullable = false)
    private BigDecimal balance;

    @Column(name = "owner_name",nullable = false)
    private String ownerName;

    @Column(name = "owner_Email",nullable = false)
    private String ownerEmail;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "update_at")
    private LocalDateTime updatedAt;

}
