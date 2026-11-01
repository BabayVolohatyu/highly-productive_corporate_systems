package org.example.monitoring.asyncwork;

import org.springframework.stereotype.Component;

@Component
public class SlowMailGateway implements MailGateway {

    private final BlockingWork blockingWork;

    public SlowMailGateway(BlockingWork blockingWork) {
        this.blockingWork = blockingWork;
    }

    @Override
    public void send(String hostname) {
        blockingWork.block();
    }
}
