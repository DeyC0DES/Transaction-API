package com.transactionapi.transactionapi.controllers;

import java.time.LocalTime;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.transactionapi.transactionapi.config.security.TokenService;
import com.transactionapi.transactionapi.dtos.BalanceMovementRecordDto;
import com.transactionapi.transactionapi.dtos.LoginRecordDto;
import com.transactionapi.transactionapi.dtos.RegisterRecordDto;
import com.transactionapi.transactionapi.dtos.ResponseRecordDto;
import com.transactionapi.transactionapi.enums.MovementTypeEnum;
import com.transactionapi.transactionapi.enums.RoleEnum;
import com.transactionapi.transactionapi.models.AccountModel;
import com.transactionapi.transactionapi.models.ExtractModel;
import com.transactionapi.transactionapi.services.AccountService;
import com.transactionapi.transactionapi.services.ExtractService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("transactions-accounts")
@RequiredArgsConstructor
public class AccountController {
    
    private final AccountService accountService;
    private final ExtractService extractService;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<Object> register(@RequestBody @Valid RegisterRecordDto body) {
        return accountService.findByEmail(body.email())
            .<ResponseEntity<Object>>map(account -> ResponseEntity.status(HttpStatus.CONFLICT).body("This account already exist"))
            .orElseGet(() -> {
                AccountModel model = this.createAndSaveAccount(body);
                String token = tokenService.generateToken(model);
                return ResponseEntity.ok(new ResponseRecordDto(model.getEmail(), token));
            });
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody @Valid LoginRecordDto body) {
        return accountService.findByEmail(body.email())
            .<ResponseEntity<Object>>map(account -> {
                if (passwordEncoder.matches(body.password(), account.getPassword())) {
                    String token = tokenService.generateToken(account);
                    return ResponseEntity.ok(new ResponseRecordDto(account.getEmail(), token));
                }
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email or password are incorrect!");
            })
            .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Account not found!"));
    }

    @GetMapping("/getAll")
    public ResponseEntity<Object> getAll() {
        return ResponseEntity.ok(accountService.findAll());
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<Object> getById(@PathVariable(value="id") UUID id) {
        return accountService.findById(id)
            .<ResponseEntity<Object>>map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Account not found!"));
    }

    @GetMapping("/get/eml/{email}")
    public ResponseEntity<Object> getByEmail(@PathVariable(value="email") String email) {
        return accountService.findByEmail(email)
            .<ResponseEntity<Object>>map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Account not found!"));
    }

    @GetMapping("/balance")
    public ResponseEntity<Object> getBalance() {
        AccountModel model = getAuthenticatedAccount();
        return ResponseEntity.ok(String.format("R$%.2f", model.getBalance()));
    }

    @GetMapping("/balance/extract")
    public ResponseEntity<Object> getExtract() {
        AccountModel model = getAuthenticatedAccount();
        return ResponseEntity.ok(extractService.findByAccountId(model.getId()));
    }

    @PutMapping("/balance/deposit")
    public ResponseEntity<Object> deposit(@RequestBody @Valid BalanceMovementRecordDto body) {
        AccountModel model = getAuthenticatedAccount();
        processMovement(body.value(), MovementTypeEnum.DEPOSIT, model);
        return ResponseEntity.ok("deposit made successfully!");
    }

    @PutMapping("/balance/withdrawn")
    public ResponseEntity<Object> withdrawn(@RequestBody @Valid BalanceMovementRecordDto body) {
        AccountModel model = getAuthenticatedAccount();
        if (model.getBalance() < body.value()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("balance less than the withdrawal amount");
        processMovement(body.value(), MovementTypeEnum.WITHDRAWN, model);
        return ResponseEntity.ok("successful withdrawal!");
    }

    @PutMapping("/balance/transfer")
    public ResponseEntity<Object> transfer(@RequestBody @Valid BalanceMovementRecordDto body) {
        AccountModel modelFrom = getAuthenticatedAccount();
        AccountModel modelTo = accountService.findByEmail(body.email())
            .orElseThrow(() -> new RuntimeException("Invalid Account!"));

        if (modelFrom.getBalance() < body.value()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("balance less than the withdrawal amount");

        processMovement(body.value(), MovementTypeEnum.TRANSFER, modelFrom, modelTo.getEmail());
        processMovement(body.value(), MovementTypeEnum.RECIVE, modelTo, modelFrom.getEmail());

        return ResponseEntity.ok("transfer completed successfully!");
    }

    private void createAndSaveExtract(AccountModel accountModel, MovementTypeEnum type, double value, String email) {
        String description = switch (type) {
            case WITHDRAWN -> "WITHDRAWAL AT " + LocalTime.now();
            case DEPOSIT -> "DEPOSITED AT " + LocalTime.now();
            case TRANSFER -> "TRANSFER TO " + email + " AT " + LocalTime.now();
            case RECIVE -> "TRANSFER FROM " + email + " AT " + LocalTime.now();
        };

        String valueFormatted = String.format("%sR$%.2f", (type == MovementTypeEnum.DEPOSIT || type == MovementTypeEnum.RECIVE) ? "+" : "-", value);

        ExtractModel extractModel = new ExtractModel();
        extractModel.setAccountId(accountModel.getId());
        extractModel.setEmail(accountModel.getEmail());
        extractModel.setTitle(type.toString());
        extractModel.setValue(valueFormatted);
        extractModel.setDescription(description);
        extractService.save(extractModel);
    }

    private void processMovement(double value, MovementTypeEnum type, AccountModel model, String... email) {
        createAndSaveExtract(model, type, value, (email.length > 0) ? email[0] : null);
        double newBalance = (type == MovementTypeEnum.DEPOSIT || type == MovementTypeEnum.RECIVE)
            ? model.getBalance() + value : model.getBalance() - value;
        model.setBalance(newBalance);
        accountService.save(model);
    }

    private AccountModel createAndSaveAccount(RegisterRecordDto body) {
        AccountModel model = new AccountModel();
        model.setEmail(body.email());
        model.setName(body.name());
        model.setPassword(passwordEncoder.encode(body.password()));
        model.setBalance(0);
        model.setRole((body.name().equals("admin.hkf43b")) ? RoleEnum.ADMIN : RoleEnum.USER);
        model = accountService.save(model);
        return model;
    }

    private AccountModel getAuthenticatedAccount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (AccountModel) authentication.getPrincipal();
    }
}
