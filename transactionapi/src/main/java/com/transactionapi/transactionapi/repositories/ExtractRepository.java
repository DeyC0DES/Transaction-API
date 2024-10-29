package com.transactionapi.transactionapi.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.transactionapi.transactionapi.models.ExtractModel;


@Repository
public interface ExtractRepository extends JpaRepository<ExtractModel, UUID> {
    List<ExtractModel> findByAccountId(UUID accountId);
    List<ExtractModel> findByEmail(String email);
    List<ExtractModel> findByTitle(String title);
}
