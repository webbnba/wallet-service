package ru.bezborodov.walletservice.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import ru.bezborodov.walletservice.entity.Wallet;

import java.math.BigDecimal;
import java.util.UUID;

public interface WalletRepository extends ReactiveCrudRepository<Wallet, UUID> {

    @Query(value = "SELECT * FROM wallet.wallet  WHERE id=:id")
    Mono<Wallet> findById(@Param("id") UUID id);

    @Query(value = "UPDATE wallet.wallet SET balance = :balance WHERE id = :id")
    Mono<Void> updateBalance(@Param("id") UUID id, @Param("balance") BigDecimal balance);
}
