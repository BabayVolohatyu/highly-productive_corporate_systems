package org.example.monitoring.asyncwork;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class ReachabilityProbe {

    private final BlockingWork blockingWork;

    public ReachabilityProbe(BlockingWork blockingWork) {
        this.blockingWork = blockingWork;
    }

    @Async("monitoringExecutor")
    public CompletableFuture<ProbeSnapshot> probe(String hostname) {
        blockingWork.block();
        return CompletableFuture.completedFuture(
                new ProbeSnapshot(hostname, true, Thread.currentThread().getName()));
    }
}
