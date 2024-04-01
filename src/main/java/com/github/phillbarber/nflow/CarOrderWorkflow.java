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

    private static final WorkflowState CHECK_ORDER_IS_VALID = new State("checkOrderIsValid", "Check Order Is Valid");
    private static final WorkflowState GET_CUSTOMER_DETAILS = new State("getCustomerDetails", "Check Order Is Valid");
    private static final WorkflowState GET_BASE_PRICE = new State("getBasePrice", "Get Base Price");
    private static final WorkflowState SAVE_NEW_ORDER = new State("saveNewOrder", "Save New Order");
    private static final WorkflowState DONE = new State("done", WorkflowStateType.end, "Order Request Finished");

    private static final WorkflowState ERROR = new State("error", manual, "Manual processing of failed applications");

    public CarOrderWorkflow() {
        super("carOrderWorkflow", ORDER_RECEIVED, ERROR,
                new WorkflowSettings.Builder().setMinErrorTransitionDelay(ZERO).setMaxErrorTransitionDelay(ZERO)
                        .setShortTransitionDelay(ZERO).setMaxRetries(3).build());
        setDescription("Mock workflow that makes credit decision, creates loan, deposits the money and updates credit application");
        permit(ORDER_RECEIVED, CHECK_ORDER_IS_VALID);
        permit(CHECK_ORDER_IS_VALID, GET_CUSTOMER_DETAILS);
        permit(CHECK_ORDER_IS_VALID, DONE);
        permit(GET_CUSTOMER_DETAILS, GET_BASE_PRICE);
        permit(GET_BASE_PRICE, SAVE_NEW_ORDER);
        permit(SAVE_NEW_ORDER, DONE);
    }

    public NextAction orderReceived(@SuppressWarnings("unused") StateExecution execution,
                                              @StateVar(value = "requestData", readOnly = true) Map request,
                                              @StateVar(instantiateIfNotExists = true, value = "info") CreditApplicationWorkflow.WorkflowInfo info) {
        logger.info("orderReceived");
        return moveToState(DONE, "Done");
    }

    public NextAction checkOrderIsValid(@SuppressWarnings("unused") StateExecution execution,
                                    @StateVar(value = "requestData", readOnly = true) Map request,
                                    @StateVar(instantiateIfNotExists = true, value = "info") CreditApplicationWorkflow.WorkflowInfo info) {
        logger.info("orderReceived");
        return moveToState(GET_CUSTOMER_DETAILS, "Done");
    }

    public NextAction getCustomerDetails(@SuppressWarnings("unused") StateExecution execution,
                                        @StateVar(value = "requestData", readOnly = true) Map request,
                                        @StateVar(instantiateIfNotExists = true, value = "info") CreditApplicationWorkflow.WorkflowInfo info) {
        logger.info("orderReceived");
        return moveToState(GET_BASE_PRICE, "Done");
    }

    public NextAction getBasePrice(@SuppressWarnings("unused") StateExecution execution,
                                         @StateVar(value = "requestData", readOnly = true) Map request,
                                         @StateVar(instantiateIfNotExists = true, value = "info") CreditApplicationWorkflow.WorkflowInfo info) {
        logger.info("orderReceived");
        return moveToState(SAVE_NEW_ORDER, "Done");
    }

    public NextAction saveNewOrder(@SuppressWarnings("unused") StateExecution execution,
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
