package org.example.assignment.policy;

import org.example.assignment.model.MarkdownPolicySpecification;
import org.example.assignment.policy.impl.CountMarkdownPolicy;
import org.example.assignment.policy.impl.DefaultMarkdownPolicy;
import org.example.assignment.policy.impl.PercentageMarkdownPolicy;

public class PolicyFactory {

    public static MarkdownPolicy create(MarkdownPolicySpecification specification) {
        return switch (specification.type()) {
            case DEFAULT -> new DefaultMarkdownPolicy();
            case PERCENTAGE -> new PercentageMarkdownPolicy(specification.configuration().percentage().get());
            case COUNT -> new CountMarkdownPolicy(specification.configuration().thresholds().get());
        };
    }

}
