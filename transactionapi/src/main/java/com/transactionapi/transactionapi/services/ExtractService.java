package com.transactionapi.transactionapi.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.transactionapi.transactionapi.models.ExtractModel;
import com.transactionapi.transactionapi.repositories.ExtractRepository;

import jakarta.transaction.Transactional;

@Service
public class ExtractService {
    
    @Autowired
    private ExtractRepository repository;

    @Transactional
    public ExtractModel save(ExtractModel model) {
        repository.save(model);
        return model;
    }

    public Optional<ExtractModel> findById(UUID id) {
        return repository.findById(id);
    }

    public List<ExtractModel> findByAccountId(UUID id) {
        return repository.findByAccountId(id);
    }

    public List<ExtractModel> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    public List<ExtractModel> findByTitle(String title) {
        return repository.findByTitle(title);
    }

    public void delete(ExtractModel model) {
        repository.delete(model);
    }

}
