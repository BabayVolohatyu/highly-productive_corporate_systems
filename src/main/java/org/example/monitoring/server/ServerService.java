package org.example.monitoring.server;

import org.example.monitoring.catalog.AttributeDefinition;
import org.example.monitoring.catalog.AttributeOption;
import org.example.monitoring.catalog.AttributeOptionRepository;
import org.example.monitoring.catalog.EntityType;
import org.example.monitoring.catalog.EntityTypeAttribute;
import org.example.monitoring.catalog.EntityTypeAttributeRepository;
import org.example.monitoring.catalog.EntityTypeRepository;
import org.example.monitoring.catalog.OptionValue;
import org.example.monitoring.catalog.OptionValueRepository;
import org.example.monitoring.catalog.StringValue;
import org.example.monitoring.catalog.StringValueRepository;
import org.example.monitoring.catalog.TrackedEntity;
import org.example.monitoring.catalog.TrackedEntityRepository;
import org.example.monitoring.catalog.ValueKey;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ServerService {

    private final EntityTypeRepository entityTypeRepository;
    private final EntityTypeAttributeRepository linkRepository;
    private final TrackedEntityRepository entityRepository;
    private final StringValueRepository stringValueRepository;
    private final OptionValueRepository optionValueRepository;
    private final AttributeOptionRepository optionRepository;
    private final ServerMapper serverMapper;

    public ServerService(EntityTypeRepository entityTypeRepository,
                         EntityTypeAttributeRepository linkRepository,
                         TrackedEntityRepository entityRepository,
                         StringValueRepository stringValueRepository,
                         OptionValueRepository optionValueRepository,
                         AttributeOptionRepository optionRepository,
                         ServerMapper serverMapper) {
        this.entityTypeRepository = entityTypeRepository;
        this.linkRepository = linkRepository;
        this.entityRepository = entityRepository;
        this.stringValueRepository = stringValueRepository;
        this.optionValueRepository = optionValueRepository;
        this.optionRepository = optionRepository;
        this.serverMapper = serverMapper;
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public List<ServerResponse> findAll() {
        return toResponses(entityRepository.findByEntityType_CodeOrderByIdAsc(ServerAttributes.ENTITY_TYPE));
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public ServerResponse findById(Long id) {
        return toResponses(List.of(loadServer(id))).getFirst();
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public ServerFormOptions formOptions() {
        return new ServerFormOptions(
                choices(ServerAttributes.ENVIRONMENT),
                choices(ServerAttributes.STATUS));
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public long countServers() {
        return entityRepository.countByEntityType_Code(ServerAttributes.ENTITY_TYPE);
    }

    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    @Transactional
    public ServerResponse create(ServerRequest request) {
        List<EntityTypeAttribute> links = links();
        String hostname = requireText(request.hostname(), ServerAttributes.HOSTNAME);
        ensureUniqueHostname(hostname, null);
        EntityType serverType = entityTypeRepository.findByCode(ServerAttributes.ENTITY_TYPE)
                .orElseThrow(() -> new IllegalStateException("Entity type SERVER is not seeded"));
        Instant now = Instant.now();
        TrackedEntity entity = new TrackedEntity();
        entity.setEntityType(serverType);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity = entityRepository.save(entity);
        writeValues(entity, request, links);
        return toResponses(List.of(entity)).getFirst();
    }

    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    @Transactional
    public ServerResponse update(Long id, ServerRequest request) {
        TrackedEntity entity = loadServer(id);
        String hostname = requireText(request.hostname(), ServerAttributes.HOSTNAME);
        ensureUniqueHostname(hostname, id);
        entity.setUpdatedAt(Instant.now());
        writeValues(entity, request, links());
        return toResponses(List.of(entityRepository.save(entity))).getFirst();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void delete(Long id) {
        TrackedEntity entity = loadServer(id);
        entityRepository.delete(entity);
    }

    private List<EntityTypeAttribute> links() {
        List<EntityTypeAttribute> links = linkRepository.findLinks(ServerAttributes.ENTITY_TYPE);
        if (links.isEmpty()) {
            throw new IllegalStateException("SERVER attribute catalog is empty");
        }
        return links;
    }

    private TrackedEntity loadServer(Long id) {
        return entityRepository.findByIdAndEntityType_Code(id, ServerAttributes.ENTITY_TYPE)
                .orElseThrow(() -> new NotFoundException("Server " + id + " was not found"));
    }

    private void ensureUniqueHostname(String hostname, Long excludeEntityId) {
        if (stringValueRepository.existsForType(
                ServerAttributes.ENTITY_TYPE, ServerAttributes.HOSTNAME, hostname, excludeEntityId)) {
            // lab2-broken (AC4 demo): conflict handling disabled so unit tests fail on lab2-broken only
            // throw new ConflictException("Hostname already exists");
        }
    }

    private void writeValues(TrackedEntity entity, ServerRequest request, List<EntityTypeAttribute> links) {
        Map<String, EntityTypeAttribute> byCode = new HashMap<>();
        for (EntityTypeAttribute link : links) {
            byCode.put(link.getAttribute().getCode(), link);
        }
        writeString(entity, requiredLink(byCode, ServerAttributes.HOSTNAME), requireText(request.hostname(), ServerAttributes.HOSTNAME));
        writeString(entity, requiredLink(byCode, ServerAttributes.IP_ADDRESS), requireText(request.ipAddress(), ServerAttributes.IP_ADDRESS));
        writeOptionalString(entity, requiredLink(byCode, ServerAttributes.DESCRIPTION), request.description());
        writeOption(entity, requiredLink(byCode, ServerAttributes.ENVIRONMENT), requireText(request.environment(), ServerAttributes.ENVIRONMENT));
        writeOption(entity, requiredLink(byCode, ServerAttributes.STATUS), requireText(request.status(), ServerAttributes.STATUS));
    }

    private EntityTypeAttribute requiredLink(Map<String, EntityTypeAttribute> byCode, String code) {
        EntityTypeAttribute link = byCode.get(code);
        if (link == null) {
            throw new IllegalStateException("Catalog is missing attribute " + code);
        }
        return link;
    }

    private void writeString(TrackedEntity entity, EntityTypeAttribute link, String text) {
        expectType(link, ServerAttributes.STRING);
        ValueKey key = new ValueKey(entity.getId(), link.getAttribute().getId());
        StringValue row = stringValueRepository.findById(key).orElseGet(StringValue::new);
        row.setId(key);
        row.setEntity(entity);
        row.setAttribute(link.getAttribute());
        row.setValue(text);
        stringValueRepository.save(row);
    }

    private void writeOptionalString(TrackedEntity entity, EntityTypeAttribute link, String text) {
        expectType(link, ServerAttributes.STRING);
        ValueKey key = new ValueKey(entity.getId(), link.getAttribute().getId());
        if (text == null || text.isBlank()) {
            if (stringValueRepository.existsById(key)) {
                stringValueRepository.deleteById(key);
            }
            return;
        }
        writeString(entity, link, text.trim());
    }

    private void writeOption(TrackedEntity entity, EntityTypeAttribute link, String code) {
        expectType(link, ServerAttributes.OPTION);
        AttributeDefinition attribute = link.getAttribute();
        AttributeOption option = optionRepository.findByAttributeCodeAndCode(attribute.getCode(), code)
                .orElseThrow(() -> new InvalidRequestException("Unknown " + attribute.getCode() + ": " + code));
        ValueKey key = new ValueKey(entity.getId(), attribute.getId());
        OptionValue row = optionValueRepository.findById(key).orElseGet(OptionValue::new);
        row.setId(key);
        row.setEntity(entity);
        row.setAttribute(attribute);
        row.setOptionId(option.getId().getOptionId());
        optionValueRepository.save(row);
    }

    private void expectType(EntityTypeAttribute link, String dataTypeCode) {
        if (!dataTypeCode.equals(link.getAttribute().getDataType().getCode())) {
            throw new IllegalStateException(link.getAttribute().getCode() + " is not a " + dataTypeCode + " attribute");
        }
    }

    private String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new InvalidRequestException(field + " is required");
        }
        return value.trim();
    }

    private List<OptionChoice> choices(String attributeCode) {
        return optionRepository.findByAttributeCode(attributeCode).stream()
                .map(option -> new OptionChoice(option.getCode(), option.getLabel()))
                .toList();
    }

    private List<ServerResponse> toResponses(List<TrackedEntity> entities) {
        if (entities.isEmpty()) {
            return List.of();
        }
        List<Long> ids = entities.stream().map(TrackedEntity::getId).toList();
        Map<Long, Map<String, String>> values = new HashMap<>();
        for (StringValueRepository.StringAssignment assignment : stringValueRepository.findAssignments(ids)) {
            values.computeIfAbsent(assignment.getEntityId(), ignored -> new HashMap<>())
                    .put(assignment.getAttributeCode(), assignment.getValue());
        }
        for (OptionValueRepository.OptionAssignment assignment : optionValueRepository.findAssignments(ids)) {
            values.computeIfAbsent(assignment.getEntityId(), ignored -> new HashMap<>())
                    .put(assignment.getAttributeCode(), assignment.getOptionCode());
        }
        return entities.stream().map(entity -> {
            Map<String, String> fields = values.getOrDefault(entity.getId(), Map.of());
            return serverMapper.toResponse(new ServerMappingSource(
                    entity,
                    fields.get(ServerAttributes.HOSTNAME),
                    fields.get(ServerAttributes.IP_ADDRESS),
                    fields.get(ServerAttributes.ENVIRONMENT),
                    fields.get(ServerAttributes.STATUS),
                    fields.get(ServerAttributes.DESCRIPTION)));
        }).toList();
    }
}
