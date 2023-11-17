package org.example.assignment.service;

import org.example.assignment.gateway.MarkdownGateway;
import org.example.assignment.model.*;
import org.example.assignment.policy.MarkdownPolicy;
import org.example.assignment.policy.impl.CountMarkdownPolicy;
import org.example.assignment.policy.impl.DefaultMarkdownPolicy;
import org.example.assignment.policy.impl.PercentageMarkdownPolicy;
import org.example.assignment.testdouble.DummyMarkdownPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class MarkdownServiceTest {

    private MarkdownService sut;
    private MarkdownGateway markdownGateway;

    @BeforeEach
    void setUp() {
        markdownGateway = Mockito.mock(MarkdownGateway.class);
        sut = new MarkdownService(markdownGateway);
    }

    @Test
    void retrieveTheDefaultPolicyIfMarkdownIsNotConfiguredForAProduct() {

        var id = new ProductID(UUID.randomUUID());
        var price = new Price(1.0);
        var quantity = 10;
        var productBasket = new ProductBasket(id, price, quantity);

        Mockito.when(markdownGateway.getPolicyByProductId(id)).thenReturn(Optional.empty());

        var finalPrice = sut.calculatePrice(productBasket);

        assertEquals(10.0, finalPrice.value());
        Mockito.verify(markdownGateway, Mockito.times(1)).getPolicyByProductId(id);
    }

    @Test
    void applyADummyDiscountPolicy() {

        var id = new ProductID(UUID.randomUUID());
        var price = new Price(1.0);
        var quantity = 10;
        var productBasket = new ProductBasket(id, price, quantity);
        var markdown = new Markdown(new MarkdownID(UUID.randomUUID()), new DummyMarkdownPolicy());

        Mockito.when(markdownGateway.getPolicyByProductId(id)).thenReturn(Optional.of(markdown));

        var finalPrice = sut.calculatePrice(productBasket);

        assertEquals(1.0, finalPrice.value());
        Mockito.verify(markdownGateway, Mockito.times(1)).getPolicyByProductId(id);
    }

    @ParameterizedTest
    @CsvSource({
            "10.0,90",
            "50.0,50",
            "75.0,25"
    })
    void applyAPercentageDiscountPolicy(Float discountPercentage, double expectedPrice) {

        var id = new ProductID(UUID.randomUUID());
        var price = new Price(1.0);
        var quantity = 100;
        var productBasket = new ProductBasket(id, price, quantity);
        var markdownPolicy = new PercentageMarkdownPolicy(discountPercentage);
        var markdown = new Markdown(new MarkdownID(UUID.randomUUID()), markdownPolicy);

        Mockito.when(markdownGateway.getPolicyByProductId(id)).thenReturn(Optional.of(markdown));

        var finalPrice = sut.calculatePrice(productBasket);

        assertEquals(expectedPrice, finalPrice.value());
        Mockito.verify(markdownGateway, Mockito.times(1)).getPolicyByProductId(id);
    }

    @ParameterizedTest
    @MethodSource("countBasedMarkdownProvider")
    void applyACountBaseDiscountPolicy(int quantity, MarkdownPolicy markdownPolicy, double expectedPrice) {

        var id = new ProductID(UUID.randomUUID());
        var price = new Price(1.0);
        var productBasket = new ProductBasket(id, price, quantity);
        var markdown = new Markdown(new MarkdownID(UUID.randomUUID()), markdownPolicy);

        Mockito.when(markdownGateway.getPolicyByProductId(id)).thenReturn(Optional.of(markdown));

        var finalPrice = sut.calculatePrice(productBasket);

        assertEquals(expectedPrice, finalPrice.value());
        Mockito.verify(markdownGateway, Mockito.times(1)).getPolicyByProductId(id);
    }

    static Stream<Arguments> countBasedMarkdownProvider() {
        var thresholds = new HashMap<Integer, Float>() {{
            put(50, 20.0f);
            put(70, 30.0f);
            put(80, 40.0f);
        }};
        return Stream.of(
                Arguments.arguments(40, new CountMarkdownPolicy(thresholds), 40),
                Arguments.arguments(50, new CountMarkdownPolicy(thresholds), 40),
                Arguments.arguments(75, new CountMarkdownPolicy(thresholds), 52.5),
                Arguments.arguments(100, new CountMarkdownPolicy(thresholds), 60)
        );
    }

    @Test
    void createPolicy() {
        var id = UUID.randomUUID();
        var type = MarkdownType.PERCENTAGE;
        var configuration = new MarkdownConfiguration(Optional.of(1f), Optional.empty());
        var specification = new MarkdownPolicySpecification(type, configuration);

        Mockito.when(markdownGateway.createNew(specification)).thenReturn(Optional.of(new MarkdownID(id)));

        var markdownId = sut.createPolicy(specification);

        assertTrue(markdownId.isPresent());
        assertEquals(id, markdownId.get().id());
        Mockito.verify(markdownGateway, Mockito.times(1)).createNew(specification);
    }

    @Test
    void retrievePolicy() {
        var markdownId = new MarkdownID(UUID.randomUUID());
        var markdown = new Markdown(markdownId, new DefaultMarkdownPolicy());

        Mockito.when(markdownGateway.get(markdownId)).thenReturn(Optional.of(markdown));

        var md = sut.retrievePolicy(markdownId);

        assertTrue(md.isPresent());
        assertEquals(markdownId.id(), md.get().id().id());
        Mockito.verify(markdownGateway, Mockito.times(1)).get(markdownId);
    }

    @Test
    void retrieveAllPolices() {
        var markdownId = new MarkdownID(UUID.randomUUID());
        var markdown = new Markdown(markdownId, new DefaultMarkdownPolicy());
        var savedMarkdowns = new LinkedList<Markdown>(){{ add(markdown ); }};

        Mockito.when(markdownGateway.getAll()).thenReturn(savedMarkdowns);

        List<Markdown> allPolicies = sut.retrieveAllPolicies();

        assertFalse(allPolicies.isEmpty());
        assertEquals(markdownId.id(), allPolicies.get(0).id().id());
        Mockito.verify(markdownGateway, Mockito.times(1)).getAll();
    }

    @Test
    void updatePolicy() {
        var markdownId = new MarkdownID(UUID.randomUUID());
        var type = MarkdownType.DEFAULT;
        var configuration = new MarkdownConfiguration(Optional.empty(), Optional.empty());
        var specification = new MarkdownPolicySpecification(type, configuration);
        var savedMarkdown = new Markdown(markdownId, new DefaultMarkdownPolicy());

        Mockito.when(markdownGateway.get(markdownId)).thenReturn(Optional.of(savedMarkdown));
        Mockito.when(markdownGateway.update(markdownId, specification)).thenReturn(Optional.of(Boolean.TRUE));

        var result = sut.updatePolicy(markdownId, specification);

        assertTrue(result.isPresent());
        assertTrue(result.get().equals(Boolean.TRUE));
        Mockito.verify(markdownGateway, Mockito.times(1)).get(markdownId);
        Mockito.verify(markdownGateway, Mockito.times(1)).update(markdownId, specification);
    }

    @Test
    void updateFailDueWrongMarkdownType() {
        var markdownId = new MarkdownID(UUID.randomUUID());
        var type = MarkdownType.PERCENTAGE;
        var configuration = new MarkdownConfiguration(Optional.of(1f), Optional.empty());
        var specification = new MarkdownPolicySpecification(type, configuration);
        var savedMarkdown = new Markdown(markdownId, new DefaultMarkdownPolicy());

        Mockito.when(markdownGateway.get(markdownId)).thenReturn(Optional.of(savedMarkdown));

        var result = sut.updatePolicy(markdownId, specification);

        assertTrue(result.isEmpty());
        Mockito.verify(markdownGateway, Mockito.times(1)).get(markdownId);
    }

    @Test
    void deletePolicy() {
        var markdownId = new MarkdownID(UUID.randomUUID());

        Mockito.when(markdownGateway.delete(markdownId)).thenReturn(Optional.of(Boolean.TRUE));

        var result = sut.deletePolicy(markdownId);

        assertTrue(result.isPresent());
        assertTrue(result.get().equals(Boolean.TRUE));
        Mockito.verify(markdownGateway, Mockito.times(1)).delete(markdownId);
    }

    @Test
    void associateProducts() {
        var markdownId = new MarkdownID(UUID.randomUUID());
        var savedMarkdown = new Markdown(markdownId, new DefaultMarkdownPolicy());
        var productsIds = IntStream.of(3)
                .mapToObj(__ -> new ProductID(UUID.randomUUID()))
                .toList();

        Mockito.when(markdownGateway.get(markdownId)).thenReturn(Optional.of(savedMarkdown));

        var result = sut.associateToProducts(markdownId, productsIds);

        assertTrue(result);
        Mockito.verify(markdownGateway, Mockito.times(1)).associateToProducts(markdownId, productsIds);
    }

    @Test
    void associateProductsWithUnknownMarkdown() {
        var markdownId = new MarkdownID(UUID.randomUUID());
        var productsIds = IntStream.of(3)
                .mapToObj(__ -> new ProductID(UUID.randomUUID()))
                .toList();

        Mockito.when(markdownGateway.get(markdownId)).thenReturn(Optional.empty());

        var result = sut.associateToProducts(markdownId, productsIds);

        assertFalse(result);
        Mockito.verify(markdownGateway, Mockito.times(0)).associateToProducts(markdownId, productsIds);
    }

    @Test
    void removeAssociationToProducts() {
        var markdownId = new MarkdownID(UUID.randomUUID());
        var savedMarkdown = new Markdown(markdownId, new DefaultMarkdownPolicy());
        var productsIds = IntStream.of(3)
                .mapToObj(__ -> new ProductID(UUID.randomUUID()))
                .toList();

        Mockito.when(markdownGateway.get(markdownId)).thenReturn(Optional.of(savedMarkdown));

        var result = sut.removeAssociationToProducts(markdownId, productsIds);

        assertTrue(result);
        Mockito.verify(markdownGateway, Mockito.times(1)).removeAssociationToProducts(markdownId, productsIds);
    }

    @Test
    void removeAssociationToProductsWithUnknownMarkdown() {
        var markdownId = new MarkdownID(UUID.randomUUID());
        var productsIds = IntStream.of(3)
                .mapToObj(__ -> new ProductID(UUID.randomUUID()))
                .toList();

        Mockito.when(markdownGateway.get(markdownId)).thenReturn(Optional.empty());

        var result = sut.removeAssociationToProducts(markdownId, productsIds);

        assertFalse(result);
        Mockito.verify(markdownGateway, Mockito.times(0)).removeAssociationToProducts(markdownId, productsIds);
    }
}