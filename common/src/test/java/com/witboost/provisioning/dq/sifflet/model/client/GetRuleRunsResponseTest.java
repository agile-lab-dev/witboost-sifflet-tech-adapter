package com.witboost.provisioning.dq.sifflet.model.client;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class GetRuleRunsResponseTest {

    @Test
    void testGetRuleRunsResponse() {
        GetRuleRunsResponse response = new GetRuleRunsResponse();
        GetRuleRunsResponse.DataItem dataItem = new GetRuleRunsResponse.DataItem();

        dataItem.setId("item1");
        dataItem.setCreatedDate(123456789L);
        dataItem.setCreatedBy("user1");
        dataItem.setStartDate(111111111L);
        dataItem.setEndDate(222222222L);
        dataItem.setResult("Valid");
        dataItem.setStatus(GetRuleRunsResponse.Status.SUCCESS);
        dataItem.setType("testType");

        GetRuleRunsResponse.DebugSql debugSql = new GetRuleRunsResponse.DebugSql();
        debugSql.setQuery("SELECT * FROM table");
        debugSql.setPositionalParameters(List.of("param1", "param2"));
        dataItem.setDebugSql(debugSql);

        dataItem.setRuleId("ruleId1");
        dataItem.setDebuggable(true);
        dataItem.setHasGroupBy(false);
        dataItem.setHasGraph(true);
        dataItem.setCanShowFailingRows(false);

        response.setData(List.of(dataItem));
        response.setTotalElements(1);

        assertEquals(List.of(dataItem), response.getData());
        assertEquals(1, response.getTotalElements());
        assertEquals("item1", dataItem.getId());
        assertEquals(123456789L, dataItem.getCreatedDate());
        assertEquals("user1", dataItem.getCreatedBy());
        assertEquals(111111111L, dataItem.getStartDate());
        assertEquals(222222222L, dataItem.getEndDate());
        assertEquals("Valid", dataItem.getResult());
        assertEquals(GetRuleRunsResponse.Status.SUCCESS, dataItem.getStatus());
        assertEquals("testType", dataItem.getType());
        assertEquals(debugSql, dataItem.getDebugSql());
        assertEquals("ruleId1", dataItem.getRuleId());
        assertTrue(dataItem.isDebuggable());
        assertFalse(dataItem.isHasGroupBy());
        assertTrue(dataItem.isHasGraph());
        assertFalse(dataItem.isCanShowFailingRows());

        assertEquals("SELECT * FROM table", debugSql.getQuery());
        assertEquals(List.of("param1", "param2"), debugSql.getPositionalParameters());
    }

    @Test
    void testStatusEnum() {
        assertEquals(GetRuleRunsResponse.Status.PENDING, GetRuleRunsResponse.Status.valueOf("PENDING"));
        assertEquals(GetRuleRunsResponse.Status.RUNNING, GetRuleRunsResponse.Status.valueOf("RUNNING"));
        assertEquals(GetRuleRunsResponse.Status.SUCCESS, GetRuleRunsResponse.Status.valueOf("SUCCESS"));
        assertEquals(
                GetRuleRunsResponse.Status.REQUIRES_YOUR_ATTENTION,
                GetRuleRunsResponse.Status.valueOf("REQUIRES_YOUR_ATTENTION"));
        assertEquals(GetRuleRunsResponse.Status.TECHNICAL_ERROR, GetRuleRunsResponse.Status.valueOf("TECHNICAL_ERROR"));
        assertEquals(GetRuleRunsResponse.Status.FAILED, GetRuleRunsResponse.Status.valueOf("FAILED"));
    }

    @Test
    void testCalculateStatusPercentages() {
        GetRuleRunsResponse.DataItem item1 = new GetRuleRunsResponse.DataItem();
        item1.setStatus(GetRuleRunsResponse.Status.SUCCESS);

        GetRuleRunsResponse.DataItem item2 = new GetRuleRunsResponse.DataItem();
        item2.setStatus(GetRuleRunsResponse.Status.FAILED);

        GetRuleRunsResponse.DataItem item3 = new GetRuleRunsResponse.DataItem();
        item3.setStatus(GetRuleRunsResponse.Status.REQUIRES_YOUR_ATTENTION);

        GetRuleRunsResponse.DataItem item4 = new GetRuleRunsResponse.DataItem();
        item4.setStatus(GetRuleRunsResponse.Status.SUCCESS);

        List<GetRuleRunsResponse.DataItem> items = List.of(item1, item2, item3, item4);

        Map<GetRuleRunsResponse.Status, Double> percentages = GetRuleRunsResponse.calculateStatusPercentages(items);

        assertEquals(50.0, percentages.get(GetRuleRunsResponse.Status.SUCCESS));
        assertEquals(25.0, percentages.get(GetRuleRunsResponse.Status.FAILED));
        assertEquals(25.0, percentages.get(GetRuleRunsResponse.Status.REQUIRES_YOUR_ATTENTION));
        assertEquals(0.0, percentages.get(GetRuleRunsResponse.Status.TECHNICAL_ERROR));
    }

    @Test
    void testCalculateStatusPercentagesWithEmptyList() {
        Map<GetRuleRunsResponse.Status, Double> percentages = GetRuleRunsResponse.calculateStatusPercentages(List.of());

        assertEquals(0.0, percentages.get(GetRuleRunsResponse.Status.SUCCESS));
        assertEquals(0.0, percentages.get(GetRuleRunsResponse.Status.FAILED));
        assertEquals(0.0, percentages.get(GetRuleRunsResponse.Status.REQUIRES_YOUR_ATTENTION));
        assertEquals(0.0, percentages.get(GetRuleRunsResponse.Status.TECHNICAL_ERROR));
    }

    @Test
    void testCalculateStatusPercentagesWithNullList() {
        Map<GetRuleRunsResponse.Status, Double> percentages = GetRuleRunsResponse.calculateStatusPercentages(null);

        assertEquals(0.0, percentages.get(GetRuleRunsResponse.Status.SUCCESS));
        assertEquals(0.0, percentages.get(GetRuleRunsResponse.Status.FAILED));
        assertEquals(0.0, percentages.get(GetRuleRunsResponse.Status.REQUIRES_YOUR_ATTENTION));
        assertEquals(0.0, percentages.get(GetRuleRunsResponse.Status.TECHNICAL_ERROR));
    }
}
