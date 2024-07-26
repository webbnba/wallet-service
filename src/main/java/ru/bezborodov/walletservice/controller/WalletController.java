package ru.bezborodov.walletservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.bezborodov.walletservice.controller.payload.RequestPayload;
import ru.bezborodov.walletservice.entity.Wallet;
import ru.bezborodov.walletservice.service.WalletService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/wallets")
public class WalletController {
    private final WalletService walletService;

    @PutMapping
    public Mono<ResponseEntity<Void>> performOperation(@Valid @RequestBody Mono<RequestPayload> payloadMono) {
        return payloadMono.flatMap(payload -> this.walletService.performOperation(payload.id(),
                        payload.type(), payload.amount()))
                .then(Mono.just(ResponseEntity.noContent().build()));
    }

    @GetMapping("/{walletId}")
    public Mono<ResponseEntity<Wallet>> getAmount(@PathVariable UUID walletId) {
        return this.walletService.findWalletBalance(walletId)
                .map(ResponseEntity::ok);

    }
}