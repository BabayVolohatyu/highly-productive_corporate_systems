package org.example.monitoring.lookup;

public interface AlertStatusLookup {

    AlertStatus findByHostname(String hostname);
}
