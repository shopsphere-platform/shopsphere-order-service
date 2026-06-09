package com.shopsphere.order.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateOrderRequest {
    @NotBlank  private String     shippingName;
    @NotBlank  private String     shippingAddress;
    @NotBlank  private String     shippingCity;
    @NotBlank  private String     shippingZip;
    @NotBlank  private String     shippingCountry;
    @NotEmpty  private List<Item> items;

    @Data
    public static class Item {
        private Long       productId;
        private String     productName;
        private String     selectedStrap;
        private Integer    quantity;
        private BigDecimal unitPrice;
    }
}
