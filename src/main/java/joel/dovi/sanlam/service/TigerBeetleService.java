package joel.dovi.sanlam.service;

import com.tigerbeetle.*;
import joel.dovi.sanlam.model.WithdrawalToExecute;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class TigerBeetleService {
    private String[] replicaAddresses;
    private byte[] clusterId;
    private int cohAccoountId;
    public TigerBeetleService(
            @Value("${tigerbeetle.replica_address}") String replicaAddress,
            @Value("${tigerbeetle.cluster_id}") int clusterId,
            @Value("${tigerbeetle.coh_account_id}") int cohAccountId
    ) {
        this.clusterId = UInt128.asBytes(clusterId);
        this.replicaAddresses = new String[] {replicaAddress == null ? "3000" : replicaAddress};
        this.cohAccoountId = cohAccountId;
    }

    private Client getTigerBeetleClient() {
        return new Client(clusterId, replicaAddresses);
    }

    public AccountBatch getTbAccounts(List<Integer> ids) {
        try (var client = getTigerBeetleClient()) {
            IdBatch idBatch = new IdBatch(ids.size());
            ids.forEach(idBatch::add);
            return client.lookupAccounts(idBatch);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return null;
    }

    public CreateTransferResultBatch executeWithdrawals(List<WithdrawalToExecute> withdrawals) throws ConcurrencyExceededException {
        Client client = getTigerBeetleClient();
        TransferBatch transferBatch = new TransferBatch(withdrawals.size());
        withdrawals.forEach(withdrawal -> {
            transferBatch.add();
            transferBatch.setId(UInt128.id());
            transferBatch.setAmount(withdrawal.getAmount().multiply(BigDecimal.valueOf(100)).toBigInteger());
            transferBatch.setDebitAccountId(withdrawal.getTbAccountId());
            transferBatch.setCreditAccountId(this.cohAccoountId); // cash on hand account
            transferBatch.setLedger(0);
            transferBatch.setUserData64(withdrawal.getTransferId());
        });
        CreateTransferResultBatch transferResults = client.createTransfers(transferBatch);
        client.close();
        return transferResults;
    }

}
