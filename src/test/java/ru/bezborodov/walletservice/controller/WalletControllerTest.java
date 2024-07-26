package ru.bezborodov.walletservice.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.bezborodov.walletservice.controller.payload.RequestPayload;
import ru.bezborodov.walletservice.entity.OperationType;
import ru.bezborodov.walletservice.entity.Wallet;
import ru.bezborodov.walletservice.exception.InsufficientFundsException;
import ru.bezborodov.walletservice.exception.WalletNotFoundException;
import ru.bezborodov.walletservice.service.WalletService;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletControllerTest {

    @Mock
    WalletService walletService;

    @InjectMocks
    WalletController walletController;

    @Test
    void performOperation_ShouldReturnNoContent() {
        //given
        UUID walletId = UUID.randomUUID();
        OperationType operationType = OperationType.DEPOSIT;
        BigDecimal amount = BigDecimal.valueOf(100.00);
        RequestPayload requestPayload = new RequestPayload(walletId, operationType, amount);

        //when
        when(walletService.performOperation(walletId, operationType, amount))
                .thenReturn(Mono.empty());

        StepVerifier.create(walletController.performOperation(Mono.just(requestPayload)))
                //then
                .expectNext(ResponseEntity.noContent().build())
                .verifyComplete();
        verify(walletService).performOperation(walletId, operationType, amount);
        verifyNoMoreInteractions(walletService);
    }

    @Test
    void getAmount_ShouldReturnWallet() {
        //given
        UUID walletId = UUID.randomUUID();
        Wallet wallet = new Wallet(walletId, BigDecimal.valueOf(500.00));
        //when
        when(walletService.findWalletBalance(walletId))
                .thenReturn(Mono.just(wallet));

        StepVerifier.create(walletController.getAmount(walletId))
                //then
                .expectNext(ResponseEntity.ok(wallet))
                .verifyComplete();

        verify(walletService).findWalletBalance(walletId);
        verifyNoMoreInteractions(walletService);
    }

    @Test
    void getAmount_ShouldReturnNotFound() {
        //given
        UUID walletId = UUID.randomUUID();
        //when
        when(walletService.findWalletBalance(walletId))
                .thenReturn(Mono.error(new WalletNotFoundException("Wallet not found")));

        StepVerifier.create(walletController.getAmount(walletId))
         //then
                .expectErrorMatches(throwable -> throwable instanceof WalletNotFoundException &&
                        throwable.getMessage().equals("Wallet not found"))
                .verify();

        verify(walletService).findWalletBalance(walletId);
        verifyNoMoreInteractions(walletService);
    }

    @Test
    void performOperation_ShouldReturnInsufficientFunds() {
        //given
        UUID walletId = UUID.randomUUID();
        OperationType operationType = OperationType.WITHDRAW;
        BigDecimal amount = BigDecimal.valueOf(100.00);
        RequestPayload requestPayload = new RequestPayload(walletId, operationType, amount);
        //when
        when(walletService.performOperation(walletId, operationType, amount))
                .thenReturn(Mono.error(new InsufficientFundsException("Insufficient funds in wallet: " + walletId)));

        StepVerifier.create(walletController.performOperation(Mono.just(requestPayload)))
        //then
                .expectErrorMatches(throwable -> throwable instanceof InsufficientFundsException &&
                        throwable.getMessage().equals("Insufficient funds in wallet: " + walletId))
                .verify();

        verify(walletService).performOperation(walletId, operationType, amount);
        verifyNoMoreInteractions(walletService);
    }
}