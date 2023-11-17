package org.example.assignment.gateway.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import org.example.assignment.gateway.MarkdownGateway;
import org.example.assignment.model.Markdown;
import org.example.assignment.model.MarkdownID;
import org.example.assignment.model.MarkdownPolicySpecification;
import org.example.assignment.model.ProductID;
import org.example.assignment.policy.MarkdownPolicy;
import org.example.assignment.policy.PolicyFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * In memory implementation of a simple KV storage.
 * Redis, Dynamo, MongoDB backends could be valid alternatives in a production scenario
 * where we need a fast access to a non relational data model
 */
@ApplicationScoped
@Named("MarkdownGateway")
public class MarkdownGatewayImpl implements MarkdownGateway {

    private final Map<UUID, MarkdownPolicy> markdownStorage;
    private final Map<UUID, UUID> productMarkdownAssociation;

    public MarkdownGatewayImpl() {
        this.markdownStorage = new HashMap<>();
        this.productMarkdownAssociation = new HashMap<>();
    }

    @Override
    public Optional<Markdown> getPolicyByProductId(ProductID id) {
        if(productMarkdownAssociation.containsKey(id.id())){
            var markdownId = productMarkdownAssociation.get(id.id());
            return get(new MarkdownID(markdownId));
        }
        return Optional.empty();
    }

    @Override
    public Optional<MarkdownID> createNew(MarkdownPolicySpecification specification) {
        var id = UUID.randomUUID();
        markdownStorage.put(id, PolicyFactory.create(specification));
        return Optional.of(new MarkdownID(id));
    }

    @Override
    public Optional<Markdown> get(MarkdownID markdownId) {
        if(markdownStorage.containsKey(markdownId.id())){
            var markdownPolicy = markdownStorage.get(markdownId.id());
            return Optional.of(new Markdown(markdownId, markdownPolicy));
        }
        return Optional.empty();
    }

    @Override
    public Optional<Boolean> update(MarkdownID markdownId, MarkdownPolicySpecification specification) {
        if(markdownStorage.containsKey(markdownId.id())){
            markdownStorage.replace(markdownId.id(), PolicyFactory.create(specification));
            return Optional.of(Boolean.TRUE);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Boolean> delete(MarkdownID markdownId) {
        if(markdownStorage.containsKey(markdownId.id())){
            markdownStorage.remove(markdownId.id());
            return Optional.of(Boolean.TRUE);
        }
        return Optional.empty();
    }

    @Override
    public List<Markdown> getAll() {
        return markdownStorage
                .entrySet()
                .stream()
                .map(x -> new Markdown(new MarkdownID(x.getKey()), x.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public void associateToProducts(MarkdownID markdownId, List<ProductID> products) {
        products.forEach(p -> productMarkdownAssociation.put(p.id(), markdownId.id()));
    }

    @Override
    public void removeAssociationToProducts(MarkdownID markdownID, List<ProductID> products) {
        products.forEach(p -> productMarkdownAssociation.remove(p.id()));
    }
}
