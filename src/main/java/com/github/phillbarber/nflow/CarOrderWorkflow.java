package com.github.phillbarber.nflow;

import com.github.phillbarber.nflow.domain.Customer;
import com.github.phillbarber.nflow.domain.Order;
import com.github.phillbarber.nflow.domain.OrderRequest;
import com.github.phillbarber.nflow.remoteservices.*;
import io.nflow.engine.workflow.curated.State;
import io.nflow.engine.workflow.definition.*;
import org.slf4j.Logger;

import java.util.UUID;

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
    private static final WorkflowState GET_DISCOUNT = new State("getDiscount", "Get Discount");
    private static final WorkflowState SAVE_NEW_ORDER = new State("saveNewOrder", "Save New Order");
    private static final WorkflowState DONE = new State("done", WorkflowStateType.end, "Order Request Finished");

    private static final WorkflowState ERROR = new State("error", manual, "Manual processing of failed applications");


    private BasePriceRemoteService remoteService;
    private OrderValidationService orderValidationService;
    private CustomerRemoteService customerRemoteService;
    private BasePriceRemoteService basePriceRemoteService;
    private DiscountPriceRemoteService discountPriceRemoteService;


    public CarOrderWorkflow(BasePriceRemoteService remoteService, OrderValidationService orderValidationService, CustomerRemoteService customerRemoteService, BasePriceRemoteService basePriceRemoteService, DiscountPriceRemoteService discountPriceRemoteService) {
        super("carOrderWorkflow", ORDER_RECEIVED, ERROR,
                new WorkflowSettings.Builder().setMinErrorTransitionDelay(ZERO).setMaxErrorTransitionDelay(ZERO)
                        .setShortTransitionDelay(ZERO).setMaxRetries(3).build());
        this.remoteService = remoteService;
        this.orderValidationService = orderValidationService;
        this.customerRemoteService = customerRemoteService;
        this.basePriceRemoteService = basePriceRemoteService;
        this.discountPriceRemoteService = discountPriceRemoteService;
        setDescription("Dummy Workflow that simulates a car order workflow");
        permit(ORDER_RECEIVED, CHECK_ORDER_IS_VALID);
        permit(CHECK_ORDER_IS_VALID, GET_CUSTOMER_DETAILS);
        permit(CHECK_ORDER_IS_VALID, DONE);
        permit(GET_CUSTOMER_DETAILS, GET_BASE_PRICE);
        permit(GET_BASE_PRICE, GET_DISCOUNT);
        permit(GET_DISCOUNT, SAVE_NEW_ORDER);
        permit(SAVE_NEW_ORDER, DONE);
    }

    public NextAction orderReceived(@SuppressWarnings("unused") StateExecution execution,
                                              @StateVar(value = "requestData", readOnly = true) OrderRequest request) {
        logger.info("orderReceived");
        return moveToState(CHECK_ORDER_IS_VALID, "Order Has Been Received");
    }

    public NextAction checkOrderIsValid(@SuppressWarnings("unused") StateExecution execution,
                                    @StateVar(value = "requestData", readOnly = true) OrderRequest request,
                                        @StateVar(value = "validationResponse", readOnly = false) Mutable<OrderValidationResponse> mutableValidationResponse) {
        OrderValidationResponse validationResponse = orderValidationService.getValidationResponse(request);
        mutableValidationResponse.setVal(validationResponse);
        if (validationResponse.isValid()){
            logger.info("VALID");
        }
        return moveToState(GET_CUSTOMER_DETAILS, "Done");

    }

    public NextAction getCustomerDetails(@SuppressWarnings("unused") StateExecution execution,
                                        @StateVar(value = "requestData", readOnly = true) OrderRequest request,
                                         @StateVar(value = "customer", readOnly = false) Mutable<Customer> mutableCustomer) {

        Customer customer = customerRemoteService.getCustomer(request.customerId());
        mutableCustomer.setVal(customer);

        return moveToState(GET_BASE_PRICE, "Done");
    }

    public NextAction getBasePrice(@SuppressWarnings("unused") StateExecution execution,
                                         @StateVar(value = "requestData", readOnly = true) OrderRequest request,
                                   @StateVar(value = "basePrice", readOnly = false) Mutable<BasePriceResponse> mutableBasePriceResponse) {
        BasePriceResponse basePrice = basePriceRemoteService.getBasePrice(request);
        mutableBasePriceResponse.setVal(basePrice);
        return moveToState(GET_DISCOUNT, "Done");
    }

    public NextAction getDiscount(@SuppressWarnings("unused") StateExecution execution,
                                   @StateVar(value = "requestData", readOnly = true) OrderRequest request,
                                  @StateVar(value = "customer", readOnly = true) Mutable<Customer> mutableCustomer,
                                  @StateVar(value = "basePrice", readOnly = true) Mutable<BasePriceResponse> mutableBasePriceResponse,
                                  @StateVar(value = "discountPrice", readOnly = false) Mutable<DiscountPriceResponse> mutableDiscountPrice) {
        DiscountPriceResponse discountPrice = discountPriceRemoteService.getDiscountPrice(request, mutableBasePriceResponse.getVal().basePrice(), mutableCustomer.getVal().loyaltyPoints());
        mutableDiscountPrice.setVal(discountPrice);

        return moveToState(SAVE_NEW_ORDER, "Done");
    }

    public NextAction saveNewOrder(@SuppressWarnings("unused") StateExecution execution,
                                   @StateVar(value = "requestData", readOnly = true) OrderRequest request,
                                   @StateVar(value = "customer", readOnly = true) Mutable<Customer> mutableCustomer,
                                   @StateVar(value = "basePrice", readOnly = true) Mutable<BasePriceResponse> mutableBasePriceResponse,
                                   @StateVar(value = "discountPrice", readOnly = true) Mutable<DiscountPriceResponse> mutableDiscountPrice,
                                   @StateVar(value = "order", readOnly = false) Mutable<Order> mutableOrder) {


        mutableOrder.setVal(new Order(UUID.randomUUID().toString(),
                mutableCustomer.getVal().id(),
                mutableCustomer.getVal().name(),
                mutableCustomer.getVal().loyaltyPoints(),
                mutableBasePriceResponse.getVal().basePrice(),
                mutableBasePriceResponse.getVal().currency(),
                mutableDiscountPrice.getVal().totalPrice(),
                "",
                mutableDiscountPrice.getVal().discount()

        ));

        //SAVE TO DB

        logger.info("orderReceived");
        return moveToState(DONE, "Done");
    }


    public void done(@SuppressWarnings("unused") StateExecution execution,
                                    @StateVar(value = "requestData", readOnly = true) OrderRequest request) {
        logger.info("done");
    }

    public void error(@SuppressWarnings("unused") StateExecution execution,
                     @StateVar(value = "requestData", readOnly = true) OrderRequest request) {
        logger.info("error");
    }


}
