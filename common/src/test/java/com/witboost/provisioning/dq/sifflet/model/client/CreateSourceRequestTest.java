package com.witboost.provisioning.dq.sifflet.model.client;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateSourceRequestTest {

    @Test
    void testCreateSourceRequestSettersAndGetters() {
        String name = "Test Source";
        String description = "A test description";
        String type = "CustomType";
        List<String> tags = Arrays.asList("tag1", "tag2");
        String cronExpression = "0 0 12 * * ?";

        CreateSourceRequest.Params params = new CreateSourceRequest.Params(
                "ParamType",
                "TestDatasource",
                "us-west-2",
                "s3://test-bucket/output/",
                "workgroup1",
                "database1",
                "arn:aws:iam::123456789012:role/test-role",
                "vpc-url");

        CreateSourceRequest createSourceRequest = new CreateSourceRequest();
        createSourceRequest.setName(name);
        createSourceRequest.setDescription(description);
        createSourceRequest.setType(type);
        createSourceRequest.setTags(tags);
        createSourceRequest.setCronExpression(cronExpression);
        createSourceRequest.setParams(params);

        assertEquals("Test Source", createSourceRequest.getName());
        assertEquals("A test description", createSourceRequest.getDescription());
        assertEquals("CustomType", createSourceRequest.getType());
        assertEquals(tags, createSourceRequest.getTags());
        assertEquals("0 0 12 * * ?", createSourceRequest.getCronExpression());
        assertEquals(params, createSourceRequest.getParams());
    }

    @Test
    void testParamsSettersAndGetters() {
        String type = "CustomParamType";
        String datasource = "TestDatasource";
        String region = "eu-central-1";
        String s3OutputLocation = "s3://example-bucket/output/";
        String workgroup = "wg-test";
        String database = "db-test";
        String roleArn = "arn:aws:iam::example-role";
        String vpcUrl = "test-vpc-url";

        CreateSourceRequest.Params params = new CreateSourceRequest.Params();
        params.setType(type);
        params.setDatasource(datasource);
        params.setRegion(region);
        params.setS3OutputLocation(s3OutputLocation);
        params.setWorkgroup(workgroup);
        params.setDatabase(database);
        params.setRoleArn(roleArn);
        params.setVpcUrl(vpcUrl);

        assertEquals("CustomParamType", params.getType());
        assertEquals("TestDatasource", params.getDatasource());
        assertEquals("eu-central-1", params.getRegion());
        assertEquals("s3://example-bucket/output/", params.getS3OutputLocation());
        assertEquals("wg-test", params.getWorkgroup());
        assertEquals("db-test", params.getDatabase());
        assertEquals("arn:aws:iam::example-role", params.getRoleArn());
        assertEquals("test-vpc-url", params.getVpcUrl());
    }

    @Test
    void testNoArgsConstructor() {
        CreateSourceRequest createSourceRequest = new CreateSourceRequest();

        assertNull(createSourceRequest.getName());
        assertNull(createSourceRequest.getDescription());
        assertNull(createSourceRequest.getType());
        assertNull(createSourceRequest.getTags());
        assertNull(createSourceRequest.getCronExpression());
        assertNull(createSourceRequest.getParams());
    }

    @Test
    void testAllArgsConstructor() {
        String name = "Test Source";
        String description = "A test description";
        String type = "CustomType";
        List<String> tags = Arrays.asList("tag1", "tag2");
        String cronExpression = "0 0 12 * * ?";

        CreateSourceRequest.Params params = new CreateSourceRequest.Params(
                "ParamType",
                "TestDatasource",
                "us-west-2",
                "s3://test-bucket/output/",
                "workgroup1",
                "database1",
                "arn:aws:iam::123456789012:role/test-role",
                "vpc-url");

        CreateSourceRequest createSourceRequest =
                new CreateSourceRequest(name, description, type, params, tags, cronExpression);

        assertEquals("Test Source", createSourceRequest.getName());
        assertEquals("A test description", createSourceRequest.getDescription());
        assertEquals("CustomType", createSourceRequest.getType());
        assertEquals(tags, createSourceRequest.getTags());
        assertEquals("0 0 12 * * ?", createSourceRequest.getCronExpression());
        assertEquals(params, createSourceRequest.getParams());
    }
}
