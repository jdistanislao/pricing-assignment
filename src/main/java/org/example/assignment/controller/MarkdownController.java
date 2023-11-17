package org.example.assignment.controller;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.example.assignment.controller.dto.MarkdownDTO;
import org.example.assignment.model.*;
import org.example.assignment.service.MarkdownService;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Path("/v1/pricing/markdowns")
public class MarkdownController {

    @Inject
    @Named("MarkdownService")
    private MarkdownService markdownService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll() {
        var markdowns = markdownService
                .retrieveAllPolicies()
                .stream()
                .map(x -> toMarkdownDTO(x))
                .collect(Collectors.toList());
        return Response.ok(markdowns).build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(@PathParam("id") UUID id) {
        var result = new Object() {Response value = Response.status(Response.Status.NOT_FOUND).build(); };
        markdownService
                .retrievePolicy(new MarkdownID(id))
                .ifPresentOrElse(
                        x -> result.value = Response.ok(toMarkdownDTO(x)).build(),
                        () -> {}
                );
        return result.value;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createNew(@Valid MarkdownDTO markdown) {
        var result = new Object() {Response value = Response.status(Response.Status.BAD_REQUEST).build(); };
        markdownService
                .createPolicy(toMarkdownSpecification(markdown))
                .ifPresentOrElse(
                        x -> result.value = Response
                                .ok()
                                .location(URI.create("/v1/pricing/markdowns/"+x.id()))
                                .build(),
                        () -> {}
                );
        return result.value;
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        var result = new Object() {Response value = Response.status(Response.Status.NOT_FOUND).build(); };
        markdownService
                .deletePolicy(new MarkdownID(id))
                .ifPresentOrElse(
                        x -> result.value = Response.ok().build(),
                        () -> {}
                );
        return result.value;
    }

    @PATCH
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(@PathParam("id") UUID id, @Valid MarkdownDTO markdown) {
        var result = new Object() {Response value = Response.status(Response.Status.BAD_REQUEST).build(); };
        markdownService
                .updatePolicy(new MarkdownID(id), toMarkdownSpecification(markdown))
                .ifPresentOrElse(
                        x -> result.value = x.booleanValue()
                                ? Response.ok().build()
                                : Response.status(Response.Status.NOT_FOUND).build()  ,
                        () -> {}
                );
        return result.value;
    }

    @POST
    @Path("/{id}/associations")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response associateToProducts(@PathParam("id") UUID id, List<UUID> productIds) {
        var products = productIds.stream().map(x -> new ProductID(x)).collect(Collectors.toList());
        MarkdownID markdownID = new MarkdownID(id);
        return markdownService.associateToProducts(markdownID, products)
                ? Response.status(Response.Status.OK).build()
                : Response.status(Response.Status.NOT_FOUND).build();
    }

    @DELETE
    @Path("/{id}/associations")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeAssociationToProducts(@PathParam("id") UUID id, List<UUID> productIds) {
        var products = productIds.stream().map(x -> new ProductID(x)).collect(Collectors.toList());
        MarkdownID markdownID = new MarkdownID(id);
        return markdownService.removeAssociationToProducts(markdownID, products)
                ? Response.status(Response.Status.OK).build()
                : Response.status(Response.Status.NOT_FOUND).build();
    }

    private MarkdownPolicySpecification toMarkdownSpecification(MarkdownDTO markdown) {
        Optional<Float> percentage = markdown.percentage == null ? Optional.empty() : Optional.of(markdown.percentage);
        Optional<Map<Integer, Float>> thresholds = markdown.thresholds == null ? Optional.empty() : Optional.of(markdown.thresholds);
        var configuration = new MarkdownConfiguration(percentage, thresholds);
        return new MarkdownPolicySpecification(markdown.type, configuration);
    }

    private MarkdownDTO toMarkdownDTO(Markdown markdown) {
        var specs = markdown.policy().describe();
        var dto = new MarkdownDTO();
        dto.id = markdown.id().id();
        dto.type = specs.type();
        dto.percentage = specs.configuration().percentage().orElse(null);
        dto.thresholds = specs.configuration().thresholds().orElse(null);
        return dto;
    }


}
