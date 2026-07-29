package com.ronext.optimizer.adapter.in.http;

import com.google.cloud.workflows.executions.v1.CreateExecutionRequest;
import com.google.cloud.workflows.executions.v1.Execution;
import com.google.cloud.workflows.executions.v1.ExecutionsClient;

/** Legacy-only mapping of the characterized workflow call. */
final class GcpLegacyWorkflowExecutor implements LegacyWorkflowExecutor {
    private final ExecutionsClient executions;

    GcpLegacyWorkflowExecutor(ExecutionsClient executions) {
        this.executions = executions;
    }

    @Override
    public void createExecution(String workflowName, String argument) {
        executions.createExecution(CreateExecutionRequest.newBuilder()
                .setParent(workflowName)
                .setExecution(Execution.newBuilder().setArgument(argument).build())
                .build());
    }
}
