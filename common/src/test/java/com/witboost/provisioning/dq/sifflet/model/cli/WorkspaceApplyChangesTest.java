package com.witboost.provisioning.dq.sifflet.model.cli;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class WorkspaceApplyChangesTest {

    @Test
    void testWorkspaceApplyChangesCreation() {
        WorkspaceApplyChanges.WorkspaceChange change = new WorkspaceApplyChanges.WorkspaceChange("update", null, null);

        WorkspaceApplyChanges.Log log = new WorkspaceApplyChanges.Log("INFO", "Change applied successfully");

        WorkspaceApplyChanges changes =
                new WorkspaceApplyChanges("Workspace", "WS-123", change, "success", "completed", List.of(log));

        assertNotNull(changes);
        assertEquals("Workspace", changes.kind());
        assertEquals("WS-123", changes.id());
        assertEquals("success", changes.status());
        assertEquals("completed", changes.subStatus());
        assertNotNull(changes.logs());
        assertEquals(1, changes.logs().size());
        assertEquals("INFO", changes.logs().get(0).level());
    }

    @Test
    void testWorkspaceApplyChangeEquality() {
        WorkspaceApplyChanges.WorkspaceChange change1 = new WorkspaceApplyChanges.WorkspaceChange("create", null, null);
        WorkspaceApplyChanges.WorkspaceChange change2 = new WorkspaceApplyChanges.WorkspaceChange("create", null, null);

        assertEquals(change1, change2);

        WorkspaceApplyChanges.Log log1 = new WorkspaceApplyChanges.Log("INFO", "Log1");
        WorkspaceApplyChanges.Log log2 = new WorkspaceApplyChanges.Log("ERROR", "Log2");

        assertNotEquals(log1, log2);
    }

    @Test
    void testLogToString() {
        WorkspaceApplyChanges.Log log = new WorkspaceApplyChanges.Log("WARN", "Test warning");
        assertTrue(log.toString().contains("WARN"));
        assertTrue(log.toString().contains("Test warning"));
    }
}
