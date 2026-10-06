package org.example.monitoring.server;

import java.util.List;

public record ServerFormOptions(List<OptionChoice> environments, List<OptionChoice> statuses) {
}
