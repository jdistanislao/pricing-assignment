package org.example.assignment.testdouble;

import org.example.assignment.model.*;
import org.example.assignment.policy.MarkdownPolicy;

import java.util.Optional;

public class DummyMarkdownPolicy implements MarkdownPolicy {
    @Override
    public Price apply(ProductBasket productBasket) {
        return productBasket.productPrice();
    }

    @Override
    public MarkdownPolicySpecification describe() {
        return new MarkdownPolicySpecification(MarkdownType.COUNT,
                new MarkdownConfiguration(Optional.empty(), Optional.empty()));
    }
}
