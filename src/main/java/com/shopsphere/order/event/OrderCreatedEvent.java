package com.shopsphere.order.event;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderCreatedEvent {
    private Long        orderId;
    private String      orderNumber;
    private String      userEmail;
    private BigDecimal  total;
    private String      status;
    private List<Item>  items;

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class Item {
        private Long       productId;
        private String     productName;
        private String     selectedStrap;
        private Integer    quantity;
        private BigDecimal unitPrice;
    }
}
