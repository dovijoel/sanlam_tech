package joel.dovi.sanlam.controller;

import jakarta.persistence.EntityNotFoundException;
import joel.dovi.sanlam.model.Transaction;
import joel.dovi.sanlam.model.WithdrawalEvent;
import joel.dovi.sanlam.service.AccountService;
import joel.dovi.sanlam.service.SnsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@RestController
@RequestMapping("/bank")
public class BankAccountController {
    @Autowired
    AccountService accountService;
    @Autowired
    SnsService snsService;

    @PostMapping("/withdraw")
    public ResponseEntity<WithdrawalEvent> withdraw(@RequestParam("accountId") Long accountId,
                                   @RequestParam("amount") BigDecimal amount) {
        Transaction transaction;
        try {
            transaction = accountService.processWithdrawal(accountId, amount);
        } catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Account with ID " + accountId + " not found.");
        }

        // After a successful withdrawal, publish a withdrawal event to SNS
        WithdrawalEvent event = new WithdrawalEvent(amount, accountId, transaction.getStatus());

        snsService.publish(event.toJson(), "withdrawal_events");

        return ResponseEntity.ok(event);
    }
}
