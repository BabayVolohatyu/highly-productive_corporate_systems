package org.example.monitoring.server;

import org.example.monitoring.catalog.AttributeDefinition;
import org.example.monitoring.catalog.AttributeOption;
import org.example.monitoring.catalog.AttributeOptionId;
import org.example.monitoring.catalog.AttributeOptionRepository;
import org.example.monitoring.catalog.DataType;
import org.example.monitoring.catalog.EntityType;
import org.example.monitoring.catalog.EntityTypeAttribute;
import org.example.monitoring.catalog.EntityTypeAttributeRepository;
import org.example.monitoring.catalog.EntityTypeRepository;
import org.example.monitoring.catalog.OptionValue;
import org.example.monitoring.catalog.OptionValueRepository;
import org.example.monitoring.catalog.StringValue;
import org.example.monitoring.catalog.StringValueRepository;
import org.example.monitoring.catalog.TrackedEntity;
import org.example.monitoring.catalog.ValueKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ServerServiceTest {

    @Mock
    private EntityTypeRepository entityTypeRepository;
    @Mock
    private EntityTypeAttributeRepository linkRepository;
    @Mock
    private org.example.monitoring.catalog.TrackedEntityRepository entityRepository;
    @Mock
    private StringValueRepository stringValueRepository;
    @Mock
    private OptionValueRepository optionValueRepository;
    @Mock
    private AttributeOptionRepository optionRepository;
    @Mock
    private ServerMapper serverMapper;

    @InjectMocks
    private ServerService serverService;

    private EntityType serverType;
    private List<EntityTypeAttribute> catalogLinks;
    private AttributeDefinition hostnameAttr;
    private AttributeDefinition ipAttr;
    private AttributeDefinition descriptionAttr;
    private AttributeDefinition environmentAttr;
    private AttributeDefinition statusAttr;

    @BeforeEach
    void setUpCatalog() {
        DataType stringType = dataType(1L, ServerAttributes.STRING);
        DataType optionType = dataType(2L, ServerAttributes.OPTION);

        hostnameAttr = attribute(10L, ServerAttributes.HOSTNAME, stringType);
        ipAttr = attribute(11L, ServerAttributes.IP_ADDRESS, stringType);
        descriptionAttr = attribute(12L, ServerAttributes.DESCRIPTION, stringType);
        environmentAttr = attribute(13L, ServerAttributes.ENVIRONMENT, optionType);
        statusAttr = attribute(14L, ServerAttributes.STATUS, optionType);

        serverType = new EntityType();
        serverType.setCode(ServerAttributes.ENTITY_TYPE);
        serverType.setName("Server");

        catalogLinks = List.of(
                link(hostnameAttr),
                link(ipAttr),
                link(descriptionAttr),
                link(environmentAttr),
                link(statusAttr));

        when(linkRepository.findLinks(ServerAttributes.ENTITY_TYPE)).thenReturn(catalogLinks);

        when(serverMapper.toResponse(any(ServerMappingSource.class))).thenAnswer(invocation -> {
            ServerMappingSource source = invocation.getArgument(0);
            TrackedEntity entity = source.getEntity();
            return new ServerResponse(
                    entity.getId(),
                    source.getHostname(),
                    source.getIpAddress(),
                    source.getEnvironment(),
                    source.getStatus(),
                    source.getDescription(),
                    entity.getCreatedAt(),
                    entity.getUpdatedAt());
        });
    }

    @Test
    void findAll_withEntities_returnsMappedResponses() {
        // Arrange
        TrackedEntity entity = trackedEntity(1L);
        when(entityRepository.findByEntityType_CodeOrderByIdAsc(ServerAttributes.ENTITY_TYPE))
                .thenReturn(List.of(entity));
        stubAssignmentsForEntity(1L, "web-01", "10.0.0.1", "DEV", "UP", "notes");

        // Act
        List<ServerResponse> result = serverService.findAll();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().hostname()).isEqualTo("web-01");
        assertThat(result.getFirst().environment()).isEqualTo("DEV");
    }

    @Test
    void findAll_noEntities_returnsEmptyList() {
        // Arrange
        when(entityRepository.findByEntityType_CodeOrderByIdAsc(ServerAttributes.ENTITY_TYPE))
                .thenReturn(List.of());

        // Act
        List<ServerResponse> result = serverService.findAll();

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void findById_existingServer_returnsMappedResponse() {
        // Arrange
        TrackedEntity entity = trackedEntity(7L);
        when(entityRepository.findByIdAndEntityType_Code(7L, ServerAttributes.ENTITY_TYPE))
                .thenReturn(Optional.of(entity));
        stubAssignmentsForEntity(7L, "db-01", "10.0.0.2", "PROD", "DOWN", null);

        // Act
        ServerResponse result = serverService.findById(7L);

        // Assert
        assertThat(result.id()).isEqualTo(7L);
        assertThat(result.status()).isEqualTo("DOWN");
    }

    @Test
    void findById_missingServer_throwsNotFoundException() {
        // Arrange
        when(entityRepository.findByIdAndEntityType_Code(99L, ServerAttributes.ENTITY_TYPE))
                .thenReturn(Optional.empty());

        // Act / Assert
        assertThatThrownBy(() -> serverService.findById(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Server 99 was not found");
    }

    @Test
    void formOptions_withCatalog_returnsEnvironmentAndStatusChoices() {
        // Arrange
        when(optionRepository.findByAttributeCode(ServerAttributes.ENVIRONMENT))
                .thenReturn(List.of(option(environmentAttr, 1L, "DEV", "Development")));
        when(optionRepository.findByAttributeCode(ServerAttributes.STATUS))
                .thenReturn(List.of(option(statusAttr, 1L, "UP", "Up")));

        // Act
        ServerFormOptions result = serverService.formOptions();

        // Assert
        assertThat(result.environments()).extracting(OptionChoice::code).containsExactly("DEV");
        assertThat(result.statuses()).extracting(OptionChoice::code).containsExactly("UP");
    }

    @Test
    void formOptions_noOptions_returnsEmptyChoiceLists() {
        // Arrange
        when(optionRepository.findByAttributeCode(ServerAttributes.ENVIRONMENT)).thenReturn(List.of());
        when(optionRepository.findByAttributeCode(ServerAttributes.STATUS)).thenReturn(List.of());

        // Act
        ServerFormOptions result = serverService.formOptions();

        // Assert
        assertThat(result.environments()).isEmpty();
        assertThat(result.statuses()).isEmpty();
    }

    @Test
    void countServers_repositoryCount_returnsSameValue() {
        // Arrange
        when(entityRepository.countByEntityType_Code(ServerAttributes.ENTITY_TYPE)).thenReturn(3L);

        // Act
        long count = serverService.countServers();

        // Assert
        assertThat(count).isEqualTo(3L);
    }

    @Test
    void countServers_noServers_returnsZero() {
        // Arrange
        when(entityRepository.countByEntityType_Code(ServerAttributes.ENTITY_TYPE)).thenReturn(0L);

        // Act
        assertThat(serverService.countServers()).isZero();
    }

    @Test
    void create_validRequest_persistsEntityAndAttributeValues() {
        // Arrange
        stubCreatePrerequisites();
        ServerRequest request = new ServerRequest("new-host", "192.168.0.10", "DEV", "UP", "desc");

        // Act
        ServerResponse response = serverService.create(request);

        // Assert
        assertThat(response.hostname()).isEqualTo("new-host");
        ArgumentCaptor<TrackedEntity> entityCaptor = ArgumentCaptor.forClass(TrackedEntity.class);
        verify(entityRepository, atLeastOnce()).save(entityCaptor.capture());
        assertThat(entityCaptor.getValue().getEntityType()).isSameAs(serverType);

        ArgumentCaptor<StringValue> stringCaptor = ArgumentCaptor.forClass(StringValue.class);
        verify(stringValueRepository, atLeastOnce()).save(stringCaptor.capture());
        assertThat(stringCaptor.getAllValues()).anyMatch(row -> "new-host".equals(row.getValue()));
        assertThat(stringCaptor.getAllValues()).anyMatch(row -> "192.168.0.10".equals(row.getValue()));

        ArgumentCaptor<OptionValue> optionCaptor = ArgumentCaptor.forClass(OptionValue.class);
        verify(optionValueRepository, atLeastOnce()).save(optionCaptor.capture());
        assertThat(optionCaptor.getAllValues()).isNotEmpty();
    }

    @Test
    void create_blankHostname_throwsInvalidRequestException() {
        // Arrange
        ServerRequest request = new ServerRequest("  ", "192.168.0.10", "DEV", "UP", "");

        // Act / Assert
        assertThatThrownBy(() -> serverService.create(request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("hostname is required");
    }

    @Test
    void create_duplicateHostname_throwsConflictException() {
        // Arrange
        when(stringValueRepository.existsForType(
                eq(ServerAttributes.ENTITY_TYPE),
                eq(ServerAttributes.HOSTNAME),
                eq("taken"),
                isNull())).thenReturn(true);
        ServerRequest request = new ServerRequest("taken", "192.168.0.10", "DEV", "UP", "");

        // Act / Assert
        assertThatThrownBy(() -> serverService.create(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Hostname already exists");
    }

    @Test
    void create_unknownEnvironmentCode_throwsInvalidRequestException() {
        // Arrange
        when(stringValueRepository.existsForType(any(), any(), any(), isNull())).thenReturn(false);
        when(entityTypeRepository.findByCode(ServerAttributes.ENTITY_TYPE)).thenReturn(Optional.of(serverType));
        when(entityRepository.save(any(TrackedEntity.class))).thenAnswer(this::assignEntityId);
        when(optionRepository.findByAttributeCodeAndCode(ServerAttributes.ENVIRONMENT, "INVALID"))
                .thenReturn(Optional.empty());
        ServerRequest request = new ServerRequest("host", "192.168.0.10", "INVALID", "UP", "");

        // Act / Assert
        assertThatThrownBy(() -> serverService.create(request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("Unknown environment: INVALID");
    }

    @Test
    void create_emptyCatalog_throwsIllegalStateException() {
        // Arrange
        when(linkRepository.findLinks(ServerAttributes.ENTITY_TYPE)).thenReturn(List.of());

        // Act / Assert
        assertThatThrownBy(() -> serverService.create(
                new ServerRequest("host", "1.1.1.1", "DEV", "UP", "")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("SERVER attribute catalog is empty");
    }

    @Test
    void create_blankDescription_skipsOptionalStringSave() {
        // Arrange
        stubCreatePrerequisites();
        when(stringValueRepository.existsById(any(ValueKey.class))).thenReturn(false);

        // Act
        serverService.create(new ServerRequest("new-host", "192.168.0.10", "DEV", "UP", "   "));

        // Assert
        verify(stringValueRepository, never()).deleteById(any());
        ArgumentCaptor<StringValue> stringCaptor = ArgumentCaptor.forClass(StringValue.class);
        verify(stringValueRepository, atLeastOnce()).save(stringCaptor.capture());
        assertThat(stringCaptor.getAllValues()).noneMatch(row ->
                descriptionAttr.getId().equals(row.getAttribute().getId()));
    }

    @Test
    void update_validRequest_savesValuesAndAllowsSameHostname() {
        // Arrange
        TrackedEntity entity = trackedEntity(5L);
        when(entityRepository.findByIdAndEntityType_Code(5L, ServerAttributes.ENTITY_TYPE))
                .thenReturn(Optional.of(entity));
        when(stringValueRepository.existsForType(
                ServerAttributes.ENTITY_TYPE, ServerAttributes.HOSTNAME, "same-host", 5L))
                .thenReturn(false);
        when(entityRepository.save(entity)).thenReturn(entity);
        stubWriteRepositories();
        stubAssignmentsForEntity(5L, "same-host", "10.0.0.9", "STAGE", "MAINTENANCE", "");
        ServerRequest request = new ServerRequest("same-host", "10.0.0.9", "STAGE", "MAINTENANCE", "");

        // Act
        ServerResponse response = serverService.update(5L, request);

        // Assert
        assertThat(response.ipAddress()).isEqualTo("10.0.0.9");
        verify(entityRepository).save(entity);
        ArgumentCaptor<StringValue> stringCaptor = ArgumentCaptor.forClass(StringValue.class);
        verify(stringValueRepository, atLeastOnce()).save(stringCaptor.capture());
        assertThat(stringCaptor.getAllValues()).anyMatch(row -> "10.0.0.9".equals(row.getValue()));
    }

    @Test
    void update_missingServer_throwsNotFoundException() {
        // Arrange
        when(entityRepository.findByIdAndEntityType_Code(404L, ServerAttributes.ENTITY_TYPE))
                .thenReturn(Optional.empty());

        // Act / Assert
        assertThatThrownBy(() -> serverService.update(404L,
                new ServerRequest("h", "1.1.1.1", "DEV", "UP", "")))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void update_duplicateHostnameOnOtherEntity_throwsConflictException() {
        // Arrange
        TrackedEntity entity = trackedEntity(5L);
        when(entityRepository.findByIdAndEntityType_Code(5L, ServerAttributes.ENTITY_TYPE))
                .thenReturn(Optional.of(entity));
        when(stringValueRepository.existsForType(
                ServerAttributes.ENTITY_TYPE, ServerAttributes.HOSTNAME, "other-host", 5L))
                .thenReturn(true);

        // Act / Assert
        assertThatThrownBy(() -> serverService.update(5L,
                new ServerRequest("other-host", "1.1.1.1", "DEV", "UP", "")))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Hostname already exists");
    }

    @Test
    void update_blankIpAddress_throwsInvalidRequestException() {
        // Arrange
        TrackedEntity entity = trackedEntity(5L);
        when(entityRepository.findByIdAndEntityType_Code(5L, ServerAttributes.ENTITY_TYPE))
                .thenReturn(Optional.of(entity));
        when(stringValueRepository.existsForType(any(), any(), any(), eq(5L))).thenReturn(false);

        // Act / Assert
        assertThatThrownBy(() -> serverService.update(5L,
                new ServerRequest("host", "  ", "DEV", "UP", "")))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("ip_address is required");
    }

    @Test
    void delete_existingServer_deletesTrackedEntity() {
        // Arrange
        TrackedEntity entity = trackedEntity(3L);
        when(entityRepository.findByIdAndEntityType_Code(3L, ServerAttributes.ENTITY_TYPE))
                .thenReturn(Optional.of(entity));

        // Act
        serverService.delete(3L);

        // Assert
        verify(entityRepository).delete(entity);
    }

    @Test
    void delete_missingServer_throwsNotFoundException() {
        // Arrange
        when(entityRepository.findByIdAndEntityType_Code(8L, ServerAttributes.ENTITY_TYPE))
                .thenReturn(Optional.empty());

        // Act / Assert
        assertThatThrownBy(() -> serverService.delete(8L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Server 8 was not found");
    }

    private void stubCreatePrerequisites() {
        when(stringValueRepository.existsForType(any(), any(), any(), isNull())).thenReturn(false);
        when(entityTypeRepository.findByCode(ServerAttributes.ENTITY_TYPE)).thenReturn(Optional.of(serverType));
        when(entityRepository.save(any(TrackedEntity.class))).thenAnswer(this::assignEntityId);
        stubWriteRepositories();
        stubAssignmentsForEntity(42L, "new-host", "192.168.0.10", "DEV", "UP", "desc");
    }

    private void stubWriteRepositories() {
        when(stringValueRepository.findById(any(ValueKey.class))).thenReturn(Optional.empty());
        when(optionValueRepository.findById(any(ValueKey.class))).thenReturn(Optional.empty());
        when(optionRepository.findByAttributeCodeAndCode(ServerAttributes.ENVIRONMENT, "DEV"))
                .thenReturn(Optional.of(option(environmentAttr, 1L, "DEV", "Development")));
        when(optionRepository.findByAttributeCodeAndCode(ServerAttributes.ENVIRONMENT, "STAGE"))
                .thenReturn(Optional.of(option(environmentAttr, 2L, "STAGE", "Staging")));
        when(optionRepository.findByAttributeCodeAndCode(ServerAttributes.STATUS, "UP"))
                .thenReturn(Optional.of(option(statusAttr, 1L, "UP", "Up")));
        when(optionRepository.findByAttributeCodeAndCode(ServerAttributes.STATUS, "MAINTENANCE"))
                .thenReturn(Optional.of(option(statusAttr, 4L, "MAINTENANCE", "Maintenance")));
    }

    private TrackedEntity assignEntityId(org.mockito.invocation.InvocationOnMock invocation) {
        TrackedEntity entity = invocation.getArgument(0);
        ReflectionTestUtils.setField(entity, "id", 42L);
        return entity;
    }

    private void stubAssignmentsForEntity(long entityId,
                                          String hostname,
                                          String ip,
                                          String environment,
                                          String status,
                                          String description) {
        StringValueRepository.StringAssignment hostnameRow = assignment(entityId, ServerAttributes.HOSTNAME, hostname);
        StringValueRepository.StringAssignment ipRow = assignment(entityId, ServerAttributes.IP_ADDRESS, ip);
        List<StringValueRepository.StringAssignment> stringRows = List.of(hostnameRow, ipRow);
        if (description != null) {
            stringRows = List.of(hostnameRow, ipRow, assignment(entityId, ServerAttributes.DESCRIPTION, description));
        }
        when(stringValueRepository.findAssignments(List.of(entityId))).thenReturn(stringRows);
        when(optionValueRepository.findAssignments(List.of(entityId))).thenReturn(List.of(
                optionAssignment(entityId, ServerAttributes.ENVIRONMENT, environment),
                optionAssignment(entityId, ServerAttributes.STATUS, status)));
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

    private static AttributeOption option(AttributeDefinition attribute, long optionId, String code, String label) {
        AttributeOption option = new AttributeOption();
        option.setId(new AttributeOptionId(attribute.getId(), optionId));
        option.setAttribute(attribute);
        option.setCode(code);
        option.setLabel(label);
        return option;
    }
}
