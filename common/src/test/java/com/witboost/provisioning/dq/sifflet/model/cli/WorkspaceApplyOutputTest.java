package com.witboost.provisioning.dq.sifflet.model.cli;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class WorkspaceApplyOutputTest {

    @Test
    void testWorkspaceApplyOutputCreation() {
        WorkspaceApplyChanges.WorkspaceChange change = new WorkspaceApplyChanges.WorkspaceChange("modify", null, null);
        WorkspaceApplyChanges.Log log = new WorkspaceApplyChanges.Log("INFO", "Modification applied");
        WorkspaceApplyChanges changes =
                new WorkspaceApplyChanges("Workspace", "WS-124", change, "success", "done", List.of(log));

        WorkspaceApplyOutput output = new WorkspaceApplyOutput(List.of(changes), List.of("Summary 1"));

        assertNotNull(output);
        assertNotNull(output.changes());
        assertEquals(1, output.changes().size());
        assertEquals(changes, output.changes().get(0));

        assertNotNull(output.change_summary());
        assertEquals(1, output.change_summary().size());
        assertEquals("Summary 1", output.change_summary().get(0));
    }

    @Test
    void testEqualityInWorkspaceApplyOutput() {
        WorkspaceApplyChanges change1 = new WorkspaceApplyChanges(null, null, null, "success", null, null);
        WorkspaceApplyChanges change2 = new WorkspaceApplyChanges(null, null, null, "success", null, null);

        WorkspaceApplyOutput output1 = new WorkspaceApplyOutput(List.of(change1), List.of());
        WorkspaceApplyOutput output2 = new WorkspaceApplyOutput(List.of(change2), List.of());

        assertEquals(output1, output2);
    }

    @Test
    void testWorkspaceApplyOutputToString() {
        WorkspaceApplyOutput output = new WorkspaceApplyOutput(List.of(), List.of("Change 1", "Change 2"));
        assertTrue(output.toString().contains("Change 1"));
        assertTrue(output.toString().contains("Change 2"));
    }
}
