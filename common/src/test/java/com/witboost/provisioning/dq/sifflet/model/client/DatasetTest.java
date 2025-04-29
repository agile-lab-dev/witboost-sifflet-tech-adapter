package com.witboost.provisioning.dq.sifflet.model.client;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class DatasetTest {

    @Test
    void testDataset() {
        Dataset dataset = new Dataset();
        dataset.setId("dataset1");
        dataset.setUri("uri1");
        dataset.setUrn("urn1");
        dataset.setName("name1");
        dataset.setTechnology("tech1");
        dataset.setType("type1");
        dataset.setDescription("description1");
        dataset.setExternalDescriptions(List.of("desc1", "desc2"));
        dataset.setHealthStatus("healthy");
        dataset.setTags(List.of("tag1", "tag2"));
        dataset.setTerms(List.of("term1", "term2"));
        dataset.setOwners(List.of("owner1", "owner2"));
        dataset.setUsage("usage1");
        dataset.setIngestionMethod("method1");
        dataset.setTransformationRun("run1");

        assertEquals("dataset1", dataset.getId());
        assertEquals("uri1", dataset.getUri());
        assertEquals("urn1", dataset.getUrn());
        assertEquals("name1", dataset.getName());
        assertEquals("tech1", dataset.getTechnology());
        assertEquals("type1", dataset.getType());
        assertEquals("description1", dataset.getDescription());
        assertEquals(List.of("desc1", "desc2"), dataset.getExternalDescriptions());
        assertEquals("healthy", dataset.getHealthStatus());
        assertEquals(List.of("tag1", "tag2"), dataset.getTags());
        assertEquals(List.of("term1", "term2"), dataset.getTerms());
        assertEquals(List.of("owner1", "owner2"), dataset.getOwners());
        assertEquals("usage1", dataset.getUsage());
        assertEquals("method1", dataset.getIngestionMethod());
        assertEquals("run1", dataset.getTransformationRun());
    }
}
