package org.example.assignment.policy.impl;

import org.example.assignment.model.*;
import org.example.assignment.policy.MarkdownPolicy;

import java.util.Optional;

public class DefaultMarkdownPolicy implements MarkdownPolicy {
    @Override
    public Price apply(ProductBasket productBasket) {
        return new Price(productBasket.productPrice().value() * productBasket.quantity());
    }

    @Override
    public MarkdownPolicySpecification describe() {
        return new MarkdownPolicySpecification(
                MarkdownType.DEFAULT,
                new MarkdownConfiguration(Optional.empty(), Optional.empty()));
    }
}
