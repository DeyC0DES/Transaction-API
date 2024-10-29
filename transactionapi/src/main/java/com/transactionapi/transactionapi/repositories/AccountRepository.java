package com.transactionapi.transactionapi.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.transactionapi.transactionapi.enums.RoleEnum;
import com.transactionapi.transactionapi.models.AccountModel;

@Repository
public interface AccountRepository extends JpaRepository<AccountModel, UUID> {
    Optional<AccountModel> findByEmail(String email);
    List<AccountModel> findByRole(RoleEnum role);
}
