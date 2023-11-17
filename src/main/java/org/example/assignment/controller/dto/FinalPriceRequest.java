package org.example.assignment.controller.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@JsonSerialize
public class FinalPriceRequest {

    @org.hibernate.validator.constraints.UUID(allowNil = false)
    @NotBlank
    public String productId;

    @Positive
    public Double productPrice;

    @Positive
    public Integer quantity;

}
