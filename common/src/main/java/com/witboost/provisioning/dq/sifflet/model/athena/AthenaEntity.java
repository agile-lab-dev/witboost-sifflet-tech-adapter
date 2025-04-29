package com.witboost.provisioning.dq.sifflet.model.athena;

import jakarta.validation.constraints.NotBlank;
import java.util.Objects;
import lombok.*;
import software.amazon.awssdk.regions.Region;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AthenaEntity that = (AthenaEntity) o;
        return Objects.equals(catalog, that.catalog)
                && Objects.equals(database, that.database)
                && Objects.equals(name, that.name)
                && Objects.equals(region, that.region)
                && Objects.equals(workGroup, that.workGroup)
                && Objects.equals(s3Bucket, that.s3Bucket);
    }

    @Override
    public int hashCode() {
        return Objects.hash(catalog, database, name, region, workGroup, s3Bucket);
    }
}
