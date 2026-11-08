package org.example.monitoring.lookup;

public interface AlertCountLookup {

    AlertCount countByHostname(String hostname);
}
