package org.example.monitoring.server;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ServerMapper {

    @Mapping(target = "id", source = "entity.id")
    @Mapping(target = "createdAt", source = "entity.createdAt")
    @Mapping(target = "updatedAt", source = "entity.updatedAt")
    ServerResponse toResponse(ServerMappingSource source);
}
