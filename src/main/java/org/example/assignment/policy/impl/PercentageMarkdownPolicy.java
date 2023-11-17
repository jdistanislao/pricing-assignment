package org.example.assignment.policy.impl;

import org.example.assignment.model.*;
import org.example.assignment.policy.MarkdownPolicy;

import java.util.Optional;

public class PercentageMarkdownPolicy implements MarkdownPolicy {
    private final Float discountPercentage;

    public PercentageMarkdownPolicy(Float discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    @Override
    public Price apply(ProductBasket productBasket) {
        var fullPrice = productBasket.productPrice().value() * productBasket.quantity();
        var discount = (fullPrice * discountPercentage) / 100;
        return new Price(fullPrice - discount );
    }

    @Override
    public MarkdownPolicySpecification describe() {
        return new MarkdownPolicySpecification(
                MarkdownType.PERCENTAGE,
                new MarkdownConfiguration(Optional.of(discountPercentage), Optional.empty()));
    }
}
