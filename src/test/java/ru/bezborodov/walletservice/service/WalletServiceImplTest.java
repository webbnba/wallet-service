package ru.bezborodov.walletservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.bezborodov.walletservice.entity.OperationType;
import ru.bezborodov.walletservice.entity.Wallet;
import ru.bezborodov.walletservice.exception.InsufficientFundsException;
import ru.bezborodov.walletservice.exception.WalletNotFoundException;
import ru.bezborodov.walletservice.repository.WalletRepository;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WalletServiceImplTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private ReactiveRedisTemplate<String, Wallet> reactiveRedisTemplate;

    @Mock
    private ReactiveValueOperations<String, Wallet> reactiveValueOperations;

    @InjectMocks
    private WalletServiceImpl walletService;

    @BeforeEach
    void setUp() {
        when(reactiveRedisTemplate.opsForValue()).thenReturn(reactiveValueOperations);
    }

    @Test
    void performOperation_ShouldDepositSuccessfully() {
        //given
        UUID walletId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(100.00);
        Wallet wallet = new Wallet(walletId, BigDecimal.valueOf(500.00));
        Wallet updatedWallet = new Wallet(walletId, BigDecimal.valueOf(600.00));
        //when
        when(walletRepository.findById(walletId)).thenReturn(Mono.just(wallet));
        when(walletRepository.updateBalance(walletId, updatedWallet.getBalance())).thenReturn(Mono.empty());
        when(reactiveRedisTemplate.opsForValue().set("wallet:" + walletId, updatedWallet)).thenReturn(Mono.just(true));
        //then
        StepVerifier.create(walletService.performOperation(walletId, OperationType.DEPOSIT, amount))
                .verifyComplete();

        verify(walletRepository).findById(walletId);
        verify(walletRepository).updateBalance(walletId, updatedWallet.getBalance());
        verify(reactiveRedisTemplate.opsForValue()).set("wallet:" + walletId, updatedWallet);
    }

    @Test
    void performOperation_ShouldWithdrawSuccessfully() {
        //given
        UUID walletId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(100.00);
        Wallet wallet = new Wallet(walletId, BigDecimal.valueOf(500.00));
        Wallet updatedWallet = new Wallet(walletId, BigDecimal.valueOf(400.00));
        //when
        when(walletRepository.findById(walletId)).thenReturn(Mono.just(wallet));
        when(walletRepository.updateBalance(walletId, updatedWallet.getBalance())).thenReturn(Mono.empty());
        when(reactiveRedisTemplate.opsForValue().set("wallet:" + walletId, updatedWallet)).thenReturn(Mono.just(true));
        //then
        StepVerifier.create(walletService.performOperation(walletId, OperationType.WITHDRAW, amount))
                .verifyComplete();

        verify(walletRepository).findById(walletId);
        verify(walletRepository).updateBalance(walletId, updatedWallet.getBalance());
        verify(reactiveRedisTemplate.opsForValue()).set("wallet:" + walletId, updatedWallet);
    }

    @Test
    void performOperation_ShouldThrowInsufficientFundsException() {
        //given
        UUID walletId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(1000.00);
        Wallet wallet = new Wallet(walletId, BigDecimal.valueOf(500.00));
        //when
        when(walletRepository.findById(walletId)).thenReturn(Mono.just(wallet));
        //then
        StepVerifier.create(walletService.performOperation(walletId, OperationType.WITHDRAW, amount))
                .expectErrorMatches(throwable -> throwable instanceof InsufficientFundsException &&
                        throwable.getMessage().equals("Insufficient funds in wallet: " + walletId))
                .verify();

        verify(walletRepository).findById(walletId);
        verify(walletRepository, never()).updateBalance(any(UUID.class), any(BigDecimal.class));
        verify(reactiveRedisTemplate.opsForValue(), never()).set(anyString(), any(Wallet.class));
    }

    @Test
    void performOperation_ShouldThrowWalletNotFoundException() {
        //given
        UUID walletId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(100.00);
        //when
        when(walletRepository.findById(walletId)).thenReturn(Mono.empty());
        //then
        StepVerifier.create(walletService.performOperation(walletId, OperationType.DEPOSIT, amount))
                .expectErrorMatches(throwable -> throwable instanceof WalletNotFoundException &&
                        throwable.getMessage().equals("Wallet not found: " + walletId))
                .verify();

        verify(walletRepository).findById(walletId);
        verify(walletRepository, never()).updateBalance(any(UUID.class), any(BigDecimal.class));
        verify(reactiveRedisTemplate.opsForValue(), never()).set(anyString(), any(Wallet.class));
    }

    @Test
    void findWalletBalance_ShouldReturnWalletFromCache() {
        //given
        UUID walletId = UUID.randomUUID();
        Wallet wallet = new Wallet(walletId, BigDecimal.valueOf(500.00));
        String cacheKey = "wallet:" + walletId;
        //when
        when(reactiveRedisTemplate.opsForValue().get(cacheKey)).thenReturn(Mono.just(wallet));
        when(walletRepository.findById(walletId)).thenReturn(Mono.empty());
        //then
        StepVerifier.create(walletService.findWalletBalance(walletId))
                .expectNext(wallet)
                .verifyComplete();

        verify(reactiveRedisTemplate.opsForValue()).get(cacheKey);
    }

    @Test
    void findWalletBalance_ShouldReturnWalletFromRepository() {
        //given
        UUID walletId = UUID.randomUUID();
        Wallet wallet = new Wallet(walletId, BigDecimal.valueOf(500.00));
        String cacheKey = "wallet:" + walletId;
        //when
        when(reactiveRedisTemplate.opsForValue().get(cacheKey)).thenReturn(Mono.empty());
        when(walletRepository.findById(walletId)).thenReturn(Mono.just(wallet));
        when(reactiveRedisTemplate.opsForValue().set(cacheKey, wallet)).thenReturn(Mono.just(true));
        //then
        StepVerifier.create(walletService.findWalletBalance(walletId))
                .expectNext(wallet)
                .verifyComplete();

        verify(reactiveRedisTemplate.opsForValue()).get(cacheKey);
        verify(walletRepository).findById(walletId);
        verify(reactiveRedisTemplate.opsForValue()).set(cacheKey, wallet);
    }

    @Test
    void findWalletBalance_ShouldThrowWalletNotFoundException() {
        //given
        UUID walletId = UUID.randomUUID();
        String cacheKey = "wallet:" + walletId;
        //when
        when(reactiveRedisTemplate.opsForValue().get(cacheKey)).thenReturn(Mono.empty());
        when(walletRepository.findById(walletId)).thenReturn(Mono.empty());
        //then
        StepVerifier.create(walletService.findWalletBalance(walletId))
                .expectErrorMatches(throwable -> throwable instanceof WalletNotFoundException &&
                        throwable.getMessage().equals("Wallet not found: " + walletId))
                .verify();

        verify(reactiveRedisTemplate.opsForValue()).get(cacheKey);
        verify(walletRepository).findById(walletId);
    }
}