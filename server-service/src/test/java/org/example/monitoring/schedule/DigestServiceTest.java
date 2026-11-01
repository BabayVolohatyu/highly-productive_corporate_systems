package org.example.monitoring.schedule;

import org.example.monitoring.catalog.TrackedEntityRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DigestServiceTest {

    @Mock
    private TrackedEntityRepository entities;
    @Mock
    private CatalogDigestRepository digests;

    @InjectMocks
    private DigestService digestService;

    @Test
    void publish_secondRunForSameKey_changesNothing() {
        when(digests.existsByDigestKey("hourly")).thenReturn(false, true);
        when(entities.countByEntityType_Code("SERVER")).thenReturn(6L);

        DigestResult first = digestService.publish("hourly");
        DigestResult second = digestService.publish("hourly");

        assertThat(first.created()).isTrue();
        assertThat(first.serverCount()).isEqualTo(6L);
        assertThat(second.created()).isFalse();
        ArgumentCaptor<CatalogDigest> saved = ArgumentCaptor.forClass(CatalogDigest.class);
        verify(digests).save(saved.capture());
        assertThat(saved.getValue().getDigestKey()).isEqualTo("hourly");
        assertThat(saved.getValue().getServerCount()).isEqualTo(6L);
        verify(entities, times(1)).countByEntityType_Code("SERVER");
        verify(digests, times(1)).save(saved.getValue());
    }
}
