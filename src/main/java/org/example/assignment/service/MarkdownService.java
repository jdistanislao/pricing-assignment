package org.example.assignment.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.example.assignment.gateway.MarkdownGateway;
import org.example.assignment.model.*;
import org.example.assignment.policy.impl.DefaultMarkdownPolicy;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
@Named("MarkdownService")
public class MarkdownService {

    private final MarkdownGateway markdownGateway;

    @Inject
    public MarkdownService(@Named("MarkdownGateway")MarkdownGateway markdownGateway) {
        this.markdownGateway = markdownGateway;
    }

    public Price calculatePrice(ProductBasket productBasket) {
        return markdownGateway
                .getPolicyByProductId(productBasket.productID())
                .map(x -> x.policy())
                .orElse(new DefaultMarkdownPolicy())
                .apply(productBasket);
    }

    public Optional<MarkdownID> createPolicy(MarkdownPolicySpecification specification) {
        return markdownGateway.createNew(specification);
    }

    public Optional<Markdown> retrievePolicy(MarkdownID markdownId) {
        return markdownGateway.get(markdownId);
    }

    public Optional<Boolean> updatePolicy(MarkdownID markdownId, MarkdownPolicySpecification specification) {
        var result = new Object() {Optional<Boolean> value = Optional.empty(); };
        markdownGateway.get(markdownId)
                .filter(markdown -> markdown.policy().describe().type().equals(specification.type()))
                .ifPresentOrElse(
                        __ -> result.value = markdownGateway.update(markdownId, specification),
                        () -> {}
                );
        return result.value;
    }

    public Optional<Boolean> deletePolicy(MarkdownID markdownId) {
        return markdownGateway.delete(markdownId);
    }

    public List<Markdown> retrieveAllPolicies() {
        return markdownGateway.getAll();
    }

    public Boolean associateToProducts(MarkdownID markdownID, List<ProductID> products) {
        var result = new Object() {Boolean value = Boolean.FALSE; };
        markdownGateway.get(markdownID)
                .ifPresent(__ -> {
                    markdownGateway.associateToProducts(markdownID, products);
                    result.value = Boolean.TRUE;
                });
        return result.value;
    }

    public boolean removeAssociationToProducts(MarkdownID markdownID, List<ProductID> products) {
        var result = new Object() {Boolean value = Boolean.FALSE; };
        markdownGateway.get(markdownID)
                .ifPresent(__ -> {
                    markdownGateway.removeAssociationToProducts(markdownID, products);
                    result.value = Boolean.TRUE;
                });
        return result.value;
    }

//    public void removeAssociationToProducts(MarkdownID markdownID, List<ProductID> products) {
//
//    }
}
