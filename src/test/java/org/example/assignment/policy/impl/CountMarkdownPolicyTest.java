package org.example.assignment.policy.impl;

import org.example.assignment.model.Price;
import org.example.assignment.model.ProductBasket;
import org.example.assignment.model.ProductID;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.HashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CountMarkdownPolicyTest {

    @ParameterizedTest
    @CsvSource({
            "39,39",
            "40,36",
            "45,40.5",
            "50,40",
            "70,49",
            "80,48",
            "90,54"
    })
    void applyAMultipleThresholdsPolicy(int quantity, double expectedPrice) {

        var productPrice = new Price(1.0);
        var productBasket = new ProductBasket(new ProductID(UUID.randomUUID()), productPrice, quantity);

        var thresholds = new HashMap<Integer, Float>() {{
            put(40, 10.0f);
            put(50, 20.0f);
            put(70, 30.0f);
            put(80, 40.0f);
        }};

        var sut = new CountMarkdownPolicy(thresholds);

        var finalPrice = sut.apply(productBasket);

        assertEquals(expectedPrice, finalPrice.value());
    }

}