package joel.dovi.sanlam.model;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
@Builder
public class Transaction {
    @Id
    public Long id;

    @ManyToOne // specifically optional is true for unknown accounts
    @JoinColumn(
            name = "account_id"
    )
    @Nullable
    public Account account;
    public BigDecimal transactionValue;
    public Instant transactionTimestamp;

    @Enumerated(EnumType.ORDINAL)
    public ETransactionStatus successful;

    @Setter
    public String note;
}

