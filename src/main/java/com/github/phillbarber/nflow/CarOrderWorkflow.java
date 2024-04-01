package com.github.phillbarber.nflow;

import io.nflow.engine.workflow.curated.State;
import io.nflow.engine.workflow.definition.*;
import org.slf4j.Logger;

import java.util.Map;

import static io.nflow.engine.workflow.definition.NextAction.moveToState;
import static io.nflow.engine.workflow.definition.WorkflowStateType.manual;
import static org.joda.time.Duration.ZERO;
import static org.slf4j.LoggerFactory.getLogger;

public class CarOrderWorkflow extends WorkflowDefinition {

    private static final Logger logger = getLogger(CarOrderWorkflow.class);
    private static final WorkflowState ORDER_RECEIVED = new State("orderReceived", WorkflowStateType.start, "Receive Order Request");
    private static final WorkflowState DONE = new State("done", WorkflowStateType.end, "Order Request Finished");

    private static final WorkflowState ERROR = new State("error", manual, "Manual processing of failed applications");

    public CarOrderWorkflow() {
        super("carOrderWorkflow", ORDER_RECEIVED, ERROR,
                new WorkflowSettings.Builder().setMinErrorTransitionDelay(ZERO).setMaxErrorTransitionDelay(ZERO)
                        .setShortTransitionDelay(ZERO).setMaxRetries(3).build());
        setDescription("Mock workflow that makes credit decision, creates loan, deposits the money and updates credit application");
        permit(ORDER_RECEIVED, DONE);
    }

    public NextAction orderReceived(@SuppressWarnings("unused") StateExecution execution,
                                              @StateVar(value = "requestData", readOnly = true) Map request,
                                              @StateVar(instantiateIfNotExists = true, value = "info") CreditApplicationWorkflow.WorkflowInfo info) {
        logger.info("orderReceived");
        return moveToState(DONE, "Done");
    }

    public void done(@SuppressWarnings("unused") StateExecution execution,
                                    @StateVar(value = "requestData", readOnly = true) Map request,
                                    @StateVar(instantiateIfNotExists = true, value = "info") CreditApplicationWorkflow.WorkflowInfo info) {
        logger.info("done");
    }

    public void error(@SuppressWarnings("unused") StateExecution execution,
                     @StateVar(value = "requestData", readOnly = true) Map request,
                     @StateVar(instantiateIfNotExists = true, value = "info") CreditApplicationWorkflow.WorkflowInfo info) {
        logger.info("error");
    }


}
