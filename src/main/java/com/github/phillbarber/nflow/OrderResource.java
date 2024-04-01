package com.github.phillbarber.nflow;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Path("/order")
public class OrderResource {

    private static Logger logger = LoggerFactory.getLogger(OrderResource.class);

    public OrderResource() {
        logger.info("I AM BORN");
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Map createOrder(Map input) {

        logger.info("MESSAGE RECEIVED" + input.toString());

        HashMap<Object, Object> objectObjectHashMap = new HashMap<>();
        objectObjectHashMap.put("WOO", "YEAH");

        return objectObjectHashMap;//this is a hack

    }
}
