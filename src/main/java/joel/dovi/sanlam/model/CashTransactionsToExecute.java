package joel.dovi.sanlam.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.BigInteger;

@Setter
@Getter
@AllArgsConstructor
public class CashTransactionsToExecute {
    private Long accountId;
    private long transferId;
    private BigDecimal amount;
    private BigInteger tigerbeetleId;
    private ETransactionStatus status;
    private String note;
}
