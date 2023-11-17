package org.example.assignment.controller;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.validation.Valid;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.example.assignment.controller.dto.FinalPriceRequest;
import org.example.assignment.model.Price;
import org.example.assignment.model.ProductBasket;
import org.example.assignment.model.ProductID;
import org.example.assignment.service.MarkdownService;

import java.util.UUID;

@Path("/v1/pricing")
public class PricingController {

    @Inject
    @Named("MarkdownService")
    private MarkdownService markdownService;

    @GET
    @Path("/finalprice")
    @Produces(MediaType.APPLICATION_JSON)
    public Response calculateFinalPrice(@Valid FinalPriceRequest request) {
        ProductBasket productBasket = toProductBasket(request);
        var price = markdownService.calculatePrice(productBasket);
        return Response.ok(price.value()).build();
    }

    private ProductBasket toProductBasket(FinalPriceRequest request) {
        return new ProductBasket(
                new ProductID(UUID.fromString(request.productId)),
                new Price(request.productPrice),
                request.quantity
        );
    }
}
