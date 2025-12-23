package org.albarakadigital.service;

import org.albarakadigital.entity.Account;
import org.albarakadigital.entity.Operation;
import org.albarakadigital.entity.enums.OperationStatus;
import org.albarakadigital.entity.enums.OperationType;
import org.albarakadigital.repository.AccountRepository;
import org.albarakadigital.repository.OperationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OperationServiceTest {

    @Mock
    private OperationRepository operationRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private OperationService operationService;

    private Account account;
    private Account destAccount;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        account = new Account();
        account.setId(1L);
        account.setBalance(20000.0);

        destAccount = new Account();
        destAccount.setId(2L);
        destAccount.setBalance(0.0);
        destAccount.setAccountNumber("ACC12345678");
    }

    @Test
    void createDeposit_BelowThreshold_ExecutesImmediately() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(operationRepository.save(any(Operation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Operation op = operationService.createDeposit(1L, 5000.0);

        assertEquals(OperationStatus.EXECUTED, op.getStatus());
        assertEquals(OperationType.DEPOSIT, op.getType());
        assertEquals(25000.0, account.getBalance());
        verify(accountRepository).save(account);
    }

    @Test
    void createDeposit_AboveThreshold_SetsPending() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(operationRepository.save(any(Operation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Operation op = operationService.createDeposit(1L, 15000.0);

        assertEquals(OperationStatus.PENDING, op.getStatus());
        assertEquals(20000.0, account.getBalance());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void createWithdrawal_BelowThreshold_ExecutesImmediately() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(operationRepository.save(any(Operation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Operation op = operationService.createWithdrawal(1L, 5000.0);

        assertEquals(OperationStatus.EXECUTED, op.getStatus());
        assertEquals(OperationType.WITHDRAWAL, op.getType());
        assertEquals(15000.0, account.getBalance());
        verify(accountRepository).save(account);
    }

    @Test
    void createWithdrawal_InsufficientBalance_ThrowsException() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        assertThrows(RuntimeException.class, () -> operationService.createWithdrawal(1L, 30000.0));
    }

    @Test
    void createTransfer_BelowThreshold_ExecutesImmediately() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.findByAccountNumber("ACC12345678")).thenReturn(Optional.of(destAccount));
        when(operationRepository.save(any(Operation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Operation op = operationService.createTransfer(1L, "ACC12345678", 5000.0);

        assertEquals(OperationStatus.EXECUTED, op.getStatus());
        assertEquals(OperationType.TRANSFER, op.getType());
        assertEquals(15000.0, account.getBalance());
        assertEquals(5000.0, destAccount.getBalance());
        verify(accountRepository, times(2)).save(any());
    }

    @Test
    void createTransfer_AboveThreshold_SetsPending() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.findByAccountNumber("ACC12345678")).thenReturn(Optional.of(destAccount));
        when(operationRepository.save(any(Operation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Operation op = operationService.createTransfer(1L, "ACC12345678", 15000.0);

        assertEquals(OperationStatus.PENDING, op.getStatus());
        assertEquals(20000.0, account.getBalance());
        assertEquals(0.0, destAccount.getBalance());
        verify(accountRepository, never()).save(any());
    }
}