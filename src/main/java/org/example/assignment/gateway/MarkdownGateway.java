package org.example.assignment.gateway;

import org.example.assignment.model.Markdown;
import org.example.assignment.model.MarkdownID;
import org.example.assignment.model.MarkdownPolicySpecification;
import org.example.assignment.model.ProductID;

import java.util.List;
import java.util.Optional;

public interface MarkdownGateway {
    Optional<Markdown> getPolicyByProductId(ProductID id);

    Optional<MarkdownID> createNew(MarkdownPolicySpecification specification);

    Optional<Markdown> get(MarkdownID markdownId);

    Optional<Boolean> update(MarkdownID markdownId, MarkdownPolicySpecification specification);

    Optional<Boolean> delete(MarkdownID markdownId);

    List<Markdown> getAll();

    void associateToProducts(MarkdownID markdownId, List<ProductID> products);

    void removeAssociationToProducts(MarkdownID markdownID, List<ProductID> products);
}
