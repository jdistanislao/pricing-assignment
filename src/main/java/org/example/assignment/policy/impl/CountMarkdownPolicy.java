package org.example.assignment.policy.impl;

import org.example.assignment.model.*;
import org.example.assignment.policy.MarkdownPolicy;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public class CountMarkdownPolicy implements MarkdownPolicy {

    private static final float NO_DISCOUNT_PERCENTAGE = 0f;

    private final TreeMap<Integer, Float> thresholds;

    public CountMarkdownPolicy(Map<Integer, Float> thresholds) {
        this.thresholds = new TreeMap<>(thresholds);
    }

    @Override
    public Price apply(ProductBasket productBasket) {
        var fullPrice = productBasket.productPrice().value() * productBasket.quantity();
        var discount = (fullPrice * findDiscountPercentage(productBasket.quantity())) / 100;
        return new Price(fullPrice - discount);
    }

    @Override
    public MarkdownPolicySpecification describe() {
        return new MarkdownPolicySpecification(
                MarkdownType.COUNT,
                new MarkdownConfiguration(Optional.empty(), Optional.of(thresholds)));
    }

    private float findDiscountPercentage(int quantity) {
        return thresholds
                .entrySet()
                .stream()
                .filter(x -> x.getKey() <= quantity)
                .map(x -> x.getValue())
                .reduce((__, x) -> x)
                .orElse(NO_DISCOUNT_PERCENTAGE);
    }
}
