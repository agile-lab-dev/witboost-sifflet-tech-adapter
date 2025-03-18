package com.witboost.provisioning.dq.sifflet.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

public class YamlMapper {
    public static <T> T fromYaml(String yaml, Class<T> target) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        return objectMapper.readValue(yaml, target);
    }

    public static String toYaml(Object obj) throws JsonProcessingException {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        ObjectMapper mapper = builder.factory(YAMLFactory.builder()
                        .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
                        .disable(YAMLGenerator.Feature.USE_NATIVE_TYPE_ID)
                        .build())
                .featuresToEnable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
                .build();
        return mapper.writeValueAsString(obj);
    }
}
