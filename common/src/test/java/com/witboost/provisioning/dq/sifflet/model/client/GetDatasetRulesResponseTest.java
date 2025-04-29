package com.witboost.provisioning.dq.sifflet.model.client;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class GetDatasetRulesResponseTest {

    @Test
    void testGetDatasetRulesResponse() {
        GetDatasetRulesResponse response = new GetDatasetRulesResponse();
        GetDatasetRulesResponse.CatalogFilter filter = new GetDatasetRulesResponse.CatalogFilter();
        GetDatasetRulesResponse.SearchRules searchRules = new GetDatasetRulesResponse.SearchRules();

        response.setCatalogFilters(List.of(filter));
        response.setSearchRules(searchRules);

        assertEquals(List.of(filter), response.getCatalogFilters());
        assertEquals(searchRules, response.getSearchRules());
    }

    @Test
    void testCatalogFilter() {
        GetDatasetRulesResponse.CatalogFilter filter = new GetDatasetRulesResponse.CatalogFilter();
        filter.setName("FilterName");
        filter.setQuery("Query1");
        filter.setId("FilterId1");

        GetDatasetRulesResponse.CatalogChild child = new GetDatasetRulesResponse.CatalogChild();
        filter.setChildren(List.of(child));

        assertEquals("FilterName", filter.getName());
        assertEquals("Query1", filter.getQuery());
        assertEquals("FilterId1", filter.getId());
        assertEquals(List.of(child), filter.getChildren());
    }

    @Test
    void testCatalogChild() {
        GetDatasetRulesResponse.CatalogChild child = new GetDatasetRulesResponse.CatalogChild();
        child.setName("Child1");
        child.setResults(5);
        child.setId("ChildId1");

        GetDatasetRulesResponse.CatalogChild grandChild = new GetDatasetRulesResponse.CatalogChild();
        child.setChildren(List.of(grandChild));

        assertEquals("Child1", child.getName());
        assertEquals(5, child.getResults());
        assertEquals("ChildId1", child.getId());
        assertEquals(List.of(grandChild), child.getChildren());
    }

    @Test
    void testSearchRules() {
        GetDatasetRulesResponse.SearchRules searchRules = new GetDatasetRulesResponse.SearchRules();
        GetDatasetRulesResponse.RuleData ruleData = new GetDatasetRulesResponse.RuleData();

        searchRules.setData(List.of(ruleData));
        searchRules.setTotalElements(10);

        assertEquals(List.of(ruleData), searchRules.getData());
        assertEquals(10, searchRules.getTotalElements());
    }

    @Test
    void testRuleData() {
        GetDatasetRulesResponse.RuleData ruleData = new GetDatasetRulesResponse.RuleData();
        GetDatasetRulesResponse.Provider provider = new GetDatasetRulesResponse.Provider();
        GetDatasetRulesResponse.CreatedBy createdBy = new GetDatasetRulesResponse.CreatedBy();
        GetDatasetRulesResponse.RunStatus runStatus = new GetDatasetRulesResponse.RunStatus();
        GetDatasetRulesResponse.RuleStatus ruleStatus = new GetDatasetRulesResponse.RuleStatus();
        GetDatasetRulesResponse.Dataset dataset = new GetDatasetRulesResponse.Dataset();

        ruleData.setId("RuleId1");
        ruleData.setName("RuleName1");
        ruleData.setSourcePlatform("Source1");
        ruleData.setProvider(provider);
        ruleData.setCreatedBy(createdBy);
        ruleData.setLastRunId("RunId1");
        ruleData.setLastRunStatus(runStatus);
        ruleData.setRuleStatus(ruleStatus);
        ruleData.setLastWeekStatuses(List.of(runStatus));
        ruleData.setReadOnly(true);
        ruleData.setCanManuallyRun(false);
        ruleData.setSupportAsCodeYAMLConversion(true);
        ruleData.setSchedule("EveryDay");
        ruleData.setRuleLabel("Label1");
        ruleData.setSelectable(false);
        ruleData.setMultiDimensional(true);
        ruleData.setCriticality(3);
        ruleData.setRuleType("Type1");
        ruleData.setDatasets(List.of(dataset));
        ruleData.setDatasetFieldNames(List.of("Field1", "Field2"));
        ruleData.setTags(List.of("Tag1", "Tag2"));
        ruleData.setTerms(List.of("Term1", "Term2"));
        ruleData.setSlackChannels(List.of("Channel1"));
        ruleData.setMails(List.of(new GetDatasetRulesResponse.Mail()));
        ruleData.setMsTeams(List.of("Team1"));
        ruleData.setHasAiRecommendations(false);

        assertEquals("RuleId1", ruleData.getId());
        assertEquals("RuleName1", ruleData.getName());
        assertEquals("Source1", ruleData.getSourcePlatform());
        assertEquals(provider, ruleData.getProvider());
        assertEquals(createdBy, ruleData.getCreatedBy());
        assertEquals("RunId1", ruleData.getLastRunId());
        assertEquals(runStatus, ruleData.getLastRunStatus());
        assertEquals(ruleStatus, ruleData.getRuleStatus());
        assertEquals(List.of(runStatus), ruleData.getLastWeekStatuses());
        assertTrue(ruleData.isReadOnly());
        assertFalse(ruleData.isCanManuallyRun());
        assertTrue(ruleData.isSupportAsCodeYAMLConversion());
        assertEquals("EveryDay", ruleData.getSchedule());
        assertEquals("Label1", ruleData.getRuleLabel());
        assertFalse(ruleData.isSelectable());
        assertTrue(ruleData.isMultiDimensional());
        assertEquals(3, ruleData.getCriticality());
        assertEquals("Type1", ruleData.getRuleType());
        assertEquals(List.of(dataset), ruleData.getDatasets());
        assertEquals(List.of("Field1", "Field2"), ruleData.getDatasetFieldNames());
        assertEquals(List.of("Tag1", "Tag2"), ruleData.getTags());
        assertEquals(List.of("Term1", "Term2"), ruleData.getTerms());
        assertEquals(List.of("Channel1"), ruleData.getSlackChannels());
        assertEquals(1, ruleData.getMails().size());
        assertEquals(List.of("Team1"), ruleData.getMsTeams());
        assertFalse(ruleData.isHasAiRecommendations());
    }

    @Test
    void testProvider() {
        GetDatasetRulesResponse.Provider provider = new GetDatasetRulesResponse.Provider();
        provider.setCreatedBy("User1");
        provider.setType("Type1");

        assertEquals("User1", provider.getCreatedBy());
        assertEquals("Type1", provider.getType());
    }

    @Test
    void testCreatedBy() {
        GetDatasetRulesResponse.CreatedBy createdBy = new GetDatasetRulesResponse.CreatedBy();
        createdBy.setName("Name1");
        createdBy.setLogin("Login1");

        assertEquals("Name1", createdBy.getName());
        assertEquals("Login1", createdBy.getLogin());
    }

    @Test
    void testRunStatus() {
        GetDatasetRulesResponse.RunStatus runStatus = new GetDatasetRulesResponse.RunStatus();
        runStatus.setTimestamp(123456789L);
        runStatus.setStatus("Success");
        runStatus.setResult("Result1");

        assertEquals(123456789L, runStatus.getTimestamp());
        assertEquals("Success", runStatus.getStatus());
        assertEquals("Result1", runStatus.getResult());
    }

    @Test
    void testRuleStatus() {
        GetDatasetRulesResponse.RuleStatus ruleStatus = new GetDatasetRulesResponse.RuleStatus();
        ruleStatus.setRuleStatus("Active");
        ruleStatus.setLatestRunDate(987654321L);

        assertEquals("Active", ruleStatus.getRuleStatus());
        assertEquals(987654321L, ruleStatus.getLatestRunDate());
    }

    @Test
    void testDataset() {
        GetDatasetRulesResponse.Dataset dataset = new GetDatasetRulesResponse.Dataset();
        dataset.setName("DatasetName1");
        dataset.setId("DatasetId1");
        dataset.setUrn("Urn1");
        dataset.setDatasourceName("Datasource1");
        dataset.setDatasourceType("DatasourceType1");
        dataset.setUri("Uri1");
        dataset.setQualifiedName("QualifiedName1");

        assertEquals("DatasetName1", dataset.getName());
        assertEquals("DatasetId1", dataset.getId());
        assertEquals("Urn1", dataset.getUrn());
        assertEquals("Datasource1", dataset.getDatasourceName());
        assertEquals("DatasourceType1", dataset.getDatasourceType());
        assertEquals("Uri1", dataset.getUri());
        assertEquals("QualifiedName1", dataset.getQualifiedName());
    }

    @Test
    void testMail() {
        GetDatasetRulesResponse.Mail mail = new GetDatasetRulesResponse.Mail();
        mail.setId("MailId1");
        mail.setCreatedDate(123456789L);
        mail.setLastModifiedDate(987654321L);
        mail.setCreatedBy("Creator1");
        mail.setModifiedBy("Modifier1");
        mail.setName("MailName1");
        mail.setExternalHook("Hook1");
        mail.setType("MailType1");

        assertEquals("MailId1", mail.getId());
        assertEquals(123456789L, mail.getCreatedDate());
        assertEquals(987654321L, mail.getLastModifiedDate());
        assertEquals("Creator1", mail.getCreatedBy());
        assertEquals("Modifier1", mail.getModifiedBy());
        assertEquals("MailName1", mail.getName());
        assertEquals("Hook1", mail.getExternalHook());
        assertEquals("MailType1", mail.getType());
    }

    @Test
    void testRuleDataToString() {
        GetDatasetRulesResponse.RuleData ruleData = new GetDatasetRulesResponse.RuleData();
        ruleData.setId("rule1");
        ruleData.setName("Test Rule");
        ruleData.setSourcePlatform("Platform1");
        ruleData.setReadOnly(true);
        ruleData.setCriticality(2);

        String toStringOutput = ruleData.toString();

        assertTrue(toStringOutput.contains("id=rule1"));
        assertTrue(toStringOutput.contains("name=Test Rule"));
        assertTrue(toStringOutput.contains("sourcePlatform=Platform1"));
        assertTrue(toStringOutput.contains("readOnly=true"));
        assertTrue(toStringOutput.contains("criticality=2"));
    }
}
