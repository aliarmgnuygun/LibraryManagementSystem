package com.getir.aau.librarymanagementsystem.config;

import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.media.StringSchema;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;


@Configuration
public class SpringDocConfig {

    @Bean
    public OpenApiCustomizer customizePageable() {
        return openApi -> openApi.getPaths().values().forEach(pathItem -> pathItem.readOperations().forEach(operation -> {
            List<Parameter> parameters = operation.getParameters();

            if (parameters != null) {
                parameters.removeIf(p -> p.getName().equalsIgnoreCase("pageable"));
            } else {
                parameters = new java.util.ArrayList<>();
                operation.setParameters(parameters);
            }

            parameters.add(createParameter(
                    "page",
                    "Page number to retrieve (starts from 0). Example: 0 for first page.",
                    "0"
            ));

            parameters.add(createParameter(
                    "size",
                    "Number of records per page. Example: 10 returns 10 books per page.",
                    "10"
            ));

            parameters.add(createParameter(
                    "sort",
                    "Sorting criteria in the format: property,(asc|desc). Example: title,asc or genre,desc.",
                    "title,asc"
            ));
        }));
    }

    private Parameter createParameter(String name, String description, String defaultValue) {
        return new Parameter()
                .name(name)
                .description(description)
                .required(false)
                .in("query")
                .schema(new StringSchema()._default(defaultValue));
    }
}