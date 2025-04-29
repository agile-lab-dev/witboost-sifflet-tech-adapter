package com.witboost.provisioning.dq.sifflet.model.cli;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class DatasetTest {

    @Test
    void testDatasetCreation() {
        Dataset dataset = new Dataset("s3://example-bucket/file.csv");

        assertNotNull(dataset);
        assertEquals("s3://example-bucket/file.csv", dataset.uri());
    }

    @Test
    void testDatasetEquality() {
        Dataset dataset1 = new Dataset("s3://path/to/data.csv");
        Dataset dataset2 = new Dataset("s3://path/to/data.csv");
        Dataset dataset3 = new Dataset("s3://path/to/other.csv");

        assertEquals(dataset1, dataset2);
        assertNotEquals(dataset1, dataset3);
    }

    @Test
    void testDatasetToString() {
        Dataset dataset = new Dataset("s3://path/to/data.csv");
        assertTrue(dataset.toString().contains("s3://path/to/data.csv"));
    }
}
