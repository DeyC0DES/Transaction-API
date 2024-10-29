package com.transactionapi.transactionapi.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.transactionapi.transactionapi.enums.RoleEnum;
import com.transactionapi.transactionapi.models.AccountModel;
import com.transactionapi.transactionapi.repositories.AccountRepository;

import jakarta.transaction.Transactional;

@Service
public class AccountService {
    
    @Autowired
    private AccountRepository repository;

    @Transactional
    public AccountModel save(AccountModel model) {
        repository.save(model);
        return model;
    }

    public Optional<AccountModel> findById(UUID id) {
        return repository.findById(id);
    }

    public Optional<AccountModel> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    public List<AccountModel> findByrole(RoleEnum role) {
        return repository.findByRole(role);
    }

    public List<AccountModel> findAll() {
        return repository.findAll();
    }

    public void delete(AccountModel model) {
        repository.delete(model);
    }

}
