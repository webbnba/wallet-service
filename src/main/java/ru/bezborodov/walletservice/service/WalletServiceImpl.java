package ru.bezborodov.walletservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.bezborodov.walletservice.entity.OperationType;
import ru.bezborodov.walletservice.entity.Wallet;
import ru.bezborodov.walletservice.exception.InsufficientFundsException;
import ru.bezborodov.walletservice.exception.WalletNotFoundException;
import ru.bezborodov.walletservice.repository.WalletRepository;

import java.math.BigDecimal;
import java.util.UUID;
@Slf4j
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {
    private final WalletRepository walletRepository;
    private final ReactiveRedisTemplate<String, Wallet> reactiveRedisTemplate;

    private static final String WALLET_CACHE_PREFIX = "wallet:";
    @Override
    @Transactional
    public Mono<Void> performOperation(UUID id, OperationType type, BigDecimal amount) {
        return this.walletRepository.findById(id)
                .switchIfEmpty(Mono.error(new WalletNotFoundException("Wallet not found: " + id)))
                .flatMap(wallet -> {
                    if (type == OperationType.DEPOSIT) {
                        wallet.setBalance(wallet.getBalance().add(amount));
                    } else if (type == OperationType.WITHDRAW) {
                        if (wallet.getBalance().compareTo(amount) < 0) {
                            return Mono.error(
                                    new InsufficientFundsException("Insufficient funds in wallet: " + id));
                        }
                        wallet.setBalance(wallet.getBalance().subtract(amount));
                    }
                    return this.walletRepository.updateBalance(wallet.getId(), wallet.getBalance())
                            .then(updateCache(wallet));
                })
                .then();
    }

    @Override
    public Mono<Wallet> findWalletBalance(UUID id) {
        String cacheKey = WALLET_CACHE_PREFIX + id;
        return reactiveRedisTemplate.opsForValue()
                .get(cacheKey)
                .doOnNext(wallet -> log.info("Found wallet in cache: {}", wallet))
                .switchIfEmpty(this.walletRepository.findById(id)
                        .switchIfEmpty(Mono.error(new WalletNotFoundException("Wallet not found: " + id)))
                        .doOnNext(wallet -> log.info("Found wallet in database: {}", wallet))
                        .flatMap(wallet -> updateCache(wallet).thenReturn(wallet)));
    }

    private Mono<Boolean> updateCache(Wallet wallet) {
        String cacheKey = WALLET_CACHE_PREFIX + wallet.getId();
        return reactiveRedisTemplate.opsForValue().set(cacheKey, wallet);
    }
}
