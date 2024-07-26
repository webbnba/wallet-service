package ru.bezborodov.walletservice.service;

import reactor.core.publisher.Mono;
import ru.bezborodov.walletservice.entity.OperationType;
import ru.bezborodov.walletservice.entity.Wallet;

import java.math.BigDecimal;
import java.util.UUID;

public interface WalletService {

 Mono<Void> performOperation(UUID id, OperationType type, BigDecimal amount);

 Mono<Wallet> findWalletBalance(UUID id);
}
