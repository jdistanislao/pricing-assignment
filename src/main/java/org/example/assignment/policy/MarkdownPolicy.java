package org.example.assignment.policy;

import org.example.assignment.model.MarkdownPolicySpecification;
import org.example.assignment.model.Price;
import org.example.assignment.model.ProductBasket;

public interface MarkdownPolicy {

    Price apply(ProductBasket productBasket);

    MarkdownPolicySpecification describe();
}
