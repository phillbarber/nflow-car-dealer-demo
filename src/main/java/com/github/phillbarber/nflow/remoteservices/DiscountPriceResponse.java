package com.github.phillbarber.nflow.remoteservices;

import java.math.BigDecimal;

public record DiscountPriceResponse (BigDecimal discount, String promotionCode, BigDecimal totalPrice) {
}
