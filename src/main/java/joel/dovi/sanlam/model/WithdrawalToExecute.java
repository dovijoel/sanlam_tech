package joel.dovi.sanlam.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@AllArgsConstructor
public class WithdrawalToExecute {
    private Long accountId;
    private long transferId;
    private int tbAccountId;
    private BigDecimal amount;
}
