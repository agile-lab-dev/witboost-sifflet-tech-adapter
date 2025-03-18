package com.witboost.provisioning.dq.sifflet.model.cli;

import java.util.List;

public record WorkspaceApplyOutput(List<WorkspaceApplyChanges> changes, List<String> change_summary) {}
