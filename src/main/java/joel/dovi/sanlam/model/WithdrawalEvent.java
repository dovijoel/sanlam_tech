package joel.dovi.sanlam.model;

import java.math.BigDecimal;

public class WithdrawalEvent {
    private BigDecimal amount;
    private Long accountId;
    private ETransactionStatus status;

    public WithdrawalEvent(BigDecimal amount, Long accountId, ETransactionStatus status) {
        this.amount = amount;
        this.accountId = accountId;
        this.status = status;
    }

    public BigDecimal getAmount() { return amount; }
    public Long getAccountId() { return accountId; }
    public ETransactionStatus getStatus() { return status; }

    // Convert to JSON String
    public String toJson() {
        return String.format("{\"amount\":\"%s\",\"accountId\":%d,\"status\":\"%s\"}",
                amount, accountId, status.name());
    }
}
