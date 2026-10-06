package org.example.monitoring.aop;

import org.example.monitoring.catalog.AttributeDefinition;
import org.example.monitoring.catalog.AttributeOptionRepository;
import org.example.monitoring.catalog.DataType;
import org.example.monitoring.catalog.EntityTypeAttribute;
import org.example.monitoring.catalog.EntityTypeAttributeRepository;
import org.example.monitoring.catalog.EntityTypeRepository;
import org.example.monitoring.catalog.OptionValueRepository;
import org.example.monitoring.catalog.StringValueRepository;
import org.example.monitoring.catalog.TrackedEntity;
import org.example.monitoring.catalog.TrackedEntityRepository;
import org.example.monitoring.server.NotFoundException;
import org.example.monitoring.server.ServerAttributes;
import org.example.monitoring.server.ServerMapper;
import org.example.monitoring.server.ServerMappingSource;
import org.example.monitoring.server.ServerResponse;
import org.example.monitoring.server.ServerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringJUnitConfig(classes = FindByIdSelfInvocationTest.AopTestConfig.class)
class FindByIdSelfInvocationTest {

    @Autowired
    private ServerService serverService;

    @Autowired
    private CallMeter callMeter;

    @Autowired
    private TrackedEntityRepository entityRepository;

    @Autowired
    private StringValueRepository stringValueRepository;

    @Autowired
    private OptionValueRepository optionValueRepository;

    @Autowired
    private EntityTypeAttributeRepository linkRepository;

    @Autowired
    private ServerMapper serverMapper;

    @BeforeEach
    void resetMeterAndStubCatalog() {
        callMeter.resetForTests();
        stubCatalogAndEntity(7L);
    }

    @Test
    @WithMockUser(username = "alice")
    void findById_externalCall_incrementsMeter() {
        ServerResponse response = serverService.findById(7L);

        assertThat(response.hostname()).isEqualTo("db-01");
        assertThat(callMeter.getFindByIdEntries()).isEqualTo(1);
    }

    @Test
    @WithMockUser(username = "alice")
    void findByIdUnmetered_selfInvocation_doesNotIncrementMeter() {
        ServerResponse direct = serverService.findById(7L);
        callMeter.resetForTests();

        ServerResponse viaSelf = serverService.findByIdUnmetered(7L);

        assertThat(viaSelf).isEqualTo(direct);
        assertThat(callMeter.getFindByIdEntries()).isZero();
    }

    @Test
    @WithMockUser(username = "alice")
    void findByIdMasked_masksAddressWithoutIncrementingMeter() {
        ServerResponse masked = serverService.findByIdMasked(7L);

        assertThat(masked.ipAddress()).isEqualTo("10.0.0.***");
        assertThat(callMeter.getFindByIdEntries()).isZero();
    }

    @Test
    @WithMockUser(username = "alice")
    void findById_missingServer_throwsNotFound() {
        when(entityRepository.findByIdAndEntityType_Code(99L, ServerAttributes.ENTITY_TYPE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> serverService.findById(99L))
                .isInstanceOf(NotFoundException.class);
    }

    private void stubCatalogAndEntity(long entityId) {
        DataType stringType = dataType(1L, ServerAttributes.STRING);
        DataType optionType = dataType(2L, ServerAttributes.OPTION);
        AttributeDefinition hostnameAttr = attribute(10L, ServerAttributes.HOSTNAME, stringType);
        AttributeDefinition ipAttr = attribute(11L, ServerAttributes.IP_ADDRESS, stringType);
        AttributeDefinition descriptionAttr = attribute(12L, ServerAttributes.DESCRIPTION, stringType);
        AttributeDefinition environmentAttr = attribute(13L, ServerAttributes.ENVIRONMENT, optionType);
        AttributeDefinition statusAttr = attribute(14L, ServerAttributes.STATUS, optionType);
        List<EntityTypeAttribute> catalogLinks = List.of(
                link(hostnameAttr),
                link(ipAttr),
                link(descriptionAttr),
                link(environmentAttr),
                link(statusAttr));
        when(linkRepository.findLinks(ServerAttributes.ENTITY_TYPE)).thenReturn(catalogLinks);

        TrackedEntity entity = trackedEntity(entityId);
        when(entityRepository.findByIdAndEntityType_Code(entityId, ServerAttributes.ENTITY_TYPE))
                .thenReturn(Optional.of(entity));
        when(stringValueRepository.findAssignments(List.of(entityId))).thenReturn(List.of(
                assignment(entityId, ServerAttributes.HOSTNAME, "db-01"),
                assignment(entityId, ServerAttributes.IP_ADDRESS, "10.0.0.2"),
                assignment(entityId, ServerAttributes.DESCRIPTION, "notes")));
        when(optionValueRepository.findAssignments(List.of(entityId))).thenReturn(List.of(
                optionAssignment(entityId, ServerAttributes.ENVIRONMENT, "PROD"),
                optionAssignment(entityId, ServerAttributes.STATUS, "DOWN")));

        when(serverMapper.toResponse(any(ServerMappingSource.class))).thenAnswer(invocation -> {
            ServerMappingSource source = invocation.getArgument(0);
            TrackedEntity tracked = source.getEntity();
            return new ServerResponse(
                    tracked.getId(),
                    source.getHostname(),
                    source.getIpAddress(),
                    source.getEnvironment(),
                    source.getStatus(),
                    source.getDescription(),
                    tracked.getCreatedAt(),
                    tracked.getUpdatedAt());
        });
    }

    @Configuration
    @EnableAspectJAutoProxy
    @EnableMethodSecurity
    static class AopTestConfig {

        @Bean
        CallMeter callMeter() {
            return new CallMeter();
        }

        @Bean
        OperationTimings operationTimings() {
            return new OperationTimings();
        }

        @Bean
        Clock clock() {
            return Clock.systemUTC();
        }

        @Bean
        CallCountAspect callCountAspect(CallMeter callMeter) {
            return new CallCountAspect(callMeter);
        }

        @Bean
        BudgetAspect budgetAspect(Clock clock, OperationTimings operationTimings) {
            return new BudgetAspect(clock, operationTimings);
        }

        @Bean
        AddressMaskAspect addressMaskAspect() {
            return new AddressMaskAspect();
        }

        @Bean
        EntityTypeRepository entityTypeRepository() {
            return mock(EntityTypeRepository.class);
        }

        @Bean
        EntityTypeAttributeRepository linkRepository() {
            return mock(EntityTypeAttributeRepository.class);
        }

        @Bean
        TrackedEntityRepository entityRepository() {
            return mock(TrackedEntityRepository.class);
        }

        @Bean
        StringValueRepository stringValueRepository() {
            return mock(StringValueRepository.class);
        }

        @Bean
        OptionValueRepository optionValueRepository() {
            return mock(OptionValueRepository.class);
        }

        @Bean
        AttributeOptionRepository optionRepository() {
            return mock(AttributeOptionRepository.class);
        }

        @Bean
        ServerMapper serverMapper() {
            return mock(ServerMapper.class);
        }

        @Bean
        ServerService serverService(EntityTypeRepository entityTypeRepository,
                                    EntityTypeAttributeRepository linkRepository,
                                    TrackedEntityRepository entityRepository,
                                    StringValueRepository stringValueRepository,
                                    OptionValueRepository optionValueRepository,
                                    AttributeOptionRepository optionRepository,
                                    ServerMapper serverMapper) {
            return new ServerService(
                    entityTypeRepository,
                    linkRepository,
                    entityRepository,
                    stringValueRepository,
                    optionValueRepository,
                    optionRepository,
                    serverMapper);
        }
    }

    private static StringValueRepository.StringAssignment assignment(long entityId, String code, String value) {
        return new StringValueRepository.StringAssignment() {
            @Override
            public Long getEntityId() {
                return entityId;
            }

            @Override
            public String getAttributeCode() {
                return code;
            }

            @Override
            public String getValue() {
                return value;
            }
        };
    }

    private static OptionValueRepository.OptionAssignment optionAssignment(long entityId, String code, String optionCode) {
        return new OptionValueRepository.OptionAssignment() {
            @Override
            public Long getEntityId() {
                return entityId;
            }

            @Override
            public String getAttributeCode() {
                return code;
            }

            @Override
            public String getOptionCode() {
                return optionCode;
            }
        };
    }

    private static TrackedEntity trackedEntity(long id) {
        TrackedEntity entity = new TrackedEntity();
        ReflectionTestUtils.setField(entity, "id", id);
        entity.setCreatedAt(Instant.parse("2024-01-01T00:00:00Z"));
        entity.setUpdatedAt(Instant.parse("2024-01-02T00:00:00Z"));
        return entity;
    }

    private static EntityTypeAttribute link(AttributeDefinition attribute) {
        EntityTypeAttribute link = new EntityTypeAttribute();
        link.setAttribute(attribute);
        return link;
    }

    private static AttributeDefinition attribute(long id, String code, DataType dataType) {
        AttributeDefinition attribute = new AttributeDefinition();
        ReflectionTestUtils.setField(attribute, "id", id);
        attribute.setCode(code);
        attribute.setName(code);
        attribute.setDataType(dataType);
        return attribute;
    }

    private static DataType dataType(long id, String code) {
        DataType dataType = new DataType();
        ReflectionTestUtils.setField(dataType, "id", id);
        dataType.setCode(code);
        return dataType;
    }
}
