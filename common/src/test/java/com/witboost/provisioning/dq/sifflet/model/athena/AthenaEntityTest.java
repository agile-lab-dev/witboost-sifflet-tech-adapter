package com.witboost.provisioning.dq.sifflet.model.athena;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.regions.Region;

class AthenaEntityTest {

    @Test
    void testGettersAndSetters() {
        AthenaEntity entity = new AthenaEntity();

        entity.setCatalog("testCatalog");
        entity.setDatabase("testDatabase");
        entity.setName("testName");
        entity.setRegion(Region.US_EAST_1);
        entity.setWorkGroup("testWorkGroup");
        entity.setS3Bucket("testS3Bucket");

        assertEquals("testCatalog", entity.getCatalog());
        assertEquals("testDatabase", entity.getDatabase());
        assertEquals("testName", entity.getName());
        assertEquals(Region.US_EAST_1, entity.getRegion());
        assertEquals("testWorkGroup", entity.getWorkGroup());
        assertEquals("testS3Bucket", entity.getS3Bucket());
    }

    @Test
    void testToString() {
        AthenaEntity entity =
                new AthenaEntity("catalog1", "database1", "name1", Region.US_EAST_1, "workgroup1", "s3bucket1");

        assertTrue(entity.toString().contains("catalog1"));
        assertTrue(entity.toString().contains("database1"));
        assertTrue(entity.toString().contains("name1"));
        assertTrue(entity.toString().contains("workgroup1"));
        assertTrue(entity.toString().contains("s3bucket1"));
    }
}
