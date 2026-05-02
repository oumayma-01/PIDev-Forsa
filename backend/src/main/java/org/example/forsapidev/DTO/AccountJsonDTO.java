package org.example.forsapidev.DTO;

import org.example.forsapidev.entities.WalletManagement.Account;
import org.example.forsapidev.entities.WalletManagement.Transaction;
import org.example.forsapidev.entities.WalletManagement.Wallet;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * JSON-safe view of an account (no JPA cycles: wallet ↔ transaction ↔ wallet).
 */
public class AccountJsonDTO {

    public Long id;
    public String type;
    public String status;
    public String accountHolderName;
    public WalletJsonDTO wallet;

    public static AccountJsonDTO from(Account account) {
        if (account == null) {
            return null;
        }
        AccountJsonDTO dto = new AccountJsonDTO();
        dto.id = account.getId();
        dto.type = account.getType() != null ? account.getType().name() : null;
        dto.status = account.getStatus() != null ? account.getStatus().name() : null;
        dto.accountHolderName = account.getAccountHolderName();
        dto.wallet = WalletJsonDTO.from(account.getWallet());
        return dto;
    }

    public static List<AccountJsonDTO> fromList(List<Account> accounts) {
        if (accounts == null) {
            return Collections.emptyList();
        }
        List<AccountJsonDTO> out = new ArrayList<>(accounts.size());
        for (Account a : accounts) {
            out.add(from(a));
        }
        return out;
    }

    public static class WalletJsonDTO {
        public Long id;
        public Long ownerId;
        public BigDecimal balance;
        public List<TransactionJsonDTO> transactions;

        public static WalletJsonDTO from(Wallet w) {
            if (w == null) {
                return null;
            }
            WalletJsonDTO dto = new WalletJsonDTO();
            dto.id = w.getId();
            dto.ownerId = w.getOwnerId();
            dto.balance = w.getBalance();
            dto.transactions = new ArrayList<>();
            if (w.getTransactions() != null) {
                for (Transaction t : w.getTransactions()) {
                    dto.transactions.add(TransactionJsonDTO.from(t));
                }
            }
            return dto;
        }
    }

    public static class TransactionJsonDTO {
        public Long id;
        public BigDecimal amount;
        public LocalDateTime date;
        public String type;
        public String status;

        public static TransactionJsonDTO from(Transaction t) {
            if (t == null) {
                return null;
            }
            TransactionJsonDTO dto = new TransactionJsonDTO();
            dto.id = t.getId();
            dto.amount = t.getAmount();
            dto.date = t.getDate();
            dto.type = t.getType() != null ? t.getType().name() : null;
            dto.status = t.getStatus() != null ? t.getStatus().name() : null;
            return dto;
        }
    }
}
