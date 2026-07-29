package com.ronext.optimizer.adapter.in.http;

/**
 * Test seam around the current Google Workflows call. This is legacy-only and
 * is not a target orchestration port.
 */
interface LegacyWorkflowExecutor {
    void createExecution(String workflowName, String argument);
}
