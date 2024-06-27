package joel.dovi.sanlam.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
@Builder
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne // specifically optional is true for unknown accounts
    @JoinColumn(
            name = "account_id"
    )
    @Nullable
    @JsonIgnore
    public Account account;
    public BigDecimal transactionValue;
    public Instant transactionTimestamp;
    @Setter
    public BigInteger tigerBeetleId;

    @Enumerated(EnumType.ORDINAL)
    @Setter
    public ETransactionStatus status;

    @Setter
    public String note;
}

