package joel.dovi.sanlam.controller;

import jakarta.persistence.EntityNotFoundException;
import joel.dovi.sanlam.model.Account;
import joel.dovi.sanlam.model.Transaction;
import joel.dovi.sanlam.model.WithdrawalEvent;
import joel.dovi.sanlam.service.AccountService;
import joel.dovi.sanlam.service.SnsService;
import joel.dovi.sanlam.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@RestController
@RequestMapping("/bank")
public class BankAccountController {
    @Autowired
    AccountService accountService;
    @Autowired
    TransactionService transactionService;
    @Autowired
    SnsService snsService;

    @PostMapping("/withdraw")
    public ResponseEntity<Transaction> withdraw(@RequestParam("accountId") Long accountId,
                                   @RequestParam("amount") BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            return withdrawOrDeposit(accountId, amount.multiply(BigDecimal.valueOf(-1)));
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Withdrawal cannot be negative");
        }
    }

    @GetMapping("/transaction")
    public ResponseEntity<Transaction> withdraw(@RequestParam("transactionId") Long transactionId) {
        return ResponseEntity.ok(transactionService.getTransaction(transactionId));
    }

    @PostMapping("/deposit")
    public ResponseEntity<Transaction> deposit(@RequestParam("accountId") Long accountId,
                                                @RequestParam("amount") BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            return withdrawOrDeposit(accountId, amount);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Deposit cannot be negative");
        }
    }

    private ResponseEntity<Transaction> withdrawOrDeposit(Long accountId, BigDecimal amount) {
        Transaction transaction;
        try {
            transaction = accountService.processCashTransaction(accountId, amount);
        } catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Account with ID " + accountId + " not found.");
        }

        return ResponseEntity.ok(transaction);
    }

    @GetMapping("account")
    public ResponseEntity<Account> getAccount(@RequestParam("accountId") Long accountId) {
        return ResponseEntity.ok(accountService.getAccount(accountId));
    }
}
