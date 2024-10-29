package com.transactionapi.transactionapi.models;

import java.io.Serializable;
import java.util.UUID;

import com.transactionapi.transactionapi.enums.RoleEnum;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="tbaccounts")
@Getter
@Setter
public class AccountModel implements Serializable {
    
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private UUID id;
    private String email;
    private String name;
    private String password;
    private double balance;
    private RoleEnum role;

}
