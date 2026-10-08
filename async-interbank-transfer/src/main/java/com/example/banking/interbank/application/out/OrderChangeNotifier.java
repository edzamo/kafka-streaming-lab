package com.example.banking.interbank.application.out;

import reactor.core.publisher.Mono;

public interface OrderChangeNotifier {

    Mono<Void> notifyChanged(long orderId);
}
