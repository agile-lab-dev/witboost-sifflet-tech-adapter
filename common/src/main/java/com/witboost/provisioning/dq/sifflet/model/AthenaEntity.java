package com.witboost.provisioning.dq.sifflet.model;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import software.amazon.awssdk.regions.Region;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class AthenaEntity {
    @NotBlank
    private String catalog;

    @NotBlank
    private String database;

    @NotBlank
    private String name;

    @NotBlank
    private Region region;

    @NotBlank
    private String workGroup;

    @NotBlank
    private String s3Bucket;
}
