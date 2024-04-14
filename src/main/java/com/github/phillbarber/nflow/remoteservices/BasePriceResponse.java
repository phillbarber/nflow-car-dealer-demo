package com.github.phillbarber.nflow.remoteservices;

import java.math.BigDecimal;

public record BasePriceResponse (BigDecimal basePrice, String currency){
}
