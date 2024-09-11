package com.nogicode.banking_service.service;

import com.nogicode.banking_service.exception.BadRequestException;
import com.nogicode.banking_service.exception.ResourceNotFoundException;
import com.nogicode.banking_service.mapper.TransactionMapper;
import com.nogicode.banking_service.model.dto.TransactionReportDTO;
import com.nogicode.banking_service.model.dto.TransactionRequestDTO;
import com.nogicode.banking_service.model.dto.TransactionResponseDTO;
import com.nogicode.banking_service.model.entity.Account;
import com.nogicode.banking_service.model.entity.Transaction;
import com.nogicode.banking_service.repository.AccountRepository;
import com.nogicode.banking_service.repository.TransactionRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final TransactionMapper transactionMapper;

    @Transactional(readOnly = true)
    public List<TransactionResponseDTO> getAllTransactions(String accountNumber) {
        List<Transaction> transactions = transactionRepository.findBySourceOrTargetAccountNumber(accountNumber);
        return transactionMapper.convertToListDTO(transactions);
    }

    @Transactional
    public TransactionResponseDTO createTransaction(TransactionRequestDTO transactionRequestDTO) {
        Account sourceAccount = accountRepository.findByAccountNumber(transactionRequestDTO.getSourceAccountNumber())
                .orElseThrow(()-> new ResourceNotFoundException("La cuenta de origen no existe"));

        Account targetAccount = accountRepository.findByAccountNumber(transactionRequestDTO.getSourceAccountNumber())
                .orElseThrow(()-> new ResourceNotFoundException("La cuenta de destino no existe"));

        BigDecimal amount = transactionRequestDTO.getAmount();
        BigDecimal sourceAccountBalance = sourceAccount.getBalance();

        if (sourceAccountBalance.compareTo(amount) < 0){
            throw new BadRequestException("Saldo insuficiente en la cuenta de origen");
        }

        Transaction transaction = transactionMapper.convertToEntity(transactionRequestDTO);
        transaction.setTransactionDate(LocalDate.now());
        transaction.setSourceAccount(sourceAccount);
        transaction.setTargetAccount(targetAccount);
        transaction = transactionRepository.save(transaction);

        BigDecimal newSourceBalance = sourceAccountBalance.subtract(amount);
        BigDecimal targetAccountBalance = targetAccount.getBalance().add(amount);

        sourceAccount.setBalance(newSourceBalance);
        targetAccount.setBalance(targetAccountBalance);

        accountRepository.save(sourceAccount);
        accountRepository.save(targetAccount);

        return transactionMapper.convertToDTO(transaction);
    }

    @Transactional(readOnly = true)
    public List<TransactionReportDTO> getTransactionsByAccountReport(String startDateStr,
                                                                     String endDateStr,
                                                                     String accountNumber) {
        LocalDate startDate = LocalDate.parse(startDateStr);
        LocalDate endDate = LocalDate.parse(endDateStr);

        List<Object[]> transactionCounts = transactionRepository.
                getTransactionCountByDateRangeAndAccountNumber(startDate, endDate, accountNumber);
        return transactionCounts.stream()
                .map(transactionMapper::convertTransactionReportToDTO)
                .toList();
    }
}
