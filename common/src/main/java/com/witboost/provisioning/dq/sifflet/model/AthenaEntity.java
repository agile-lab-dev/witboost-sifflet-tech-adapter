package com.witboost.provisioning.dq.sifflet.model;

import com.witboost.provisioning.model.DataContract;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import software.amazon.awssdk.regions.Region;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AthenaEntity {
    @NotBlank
    private String catalog;

    @NotBlank
    private String database;

    @NotBlank
    private String name;

    @NotBlank
    private DataContract dataContract;

    @NotBlank
    private Region region;

    @NotBlank
    private String workGroup;

    @NotBlank
    private String s3Bucket;
}
