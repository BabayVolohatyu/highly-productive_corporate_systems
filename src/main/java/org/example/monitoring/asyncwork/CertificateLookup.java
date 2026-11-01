package org.example.monitoring.asyncwork;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class CertificateLookup {

    private final BlockingWork blockingWork;

    public CertificateLookup(BlockingWork blockingWork) {
        this.blockingWork = blockingWork;
    }

    @Async("monitoringExecutor")
    public CompletableFuture<CertificateSnapshot> lookup(String hostname) {
        blockingWork.block();
        return CompletableFuture.completedFuture(
                new CertificateSnapshot(hostname, "OK", Thread.currentThread().getName()));
    }
}
