package org.example.assignment.model;

import java.util.Map;
import java.util.Optional;

public record MarkdownConfiguration(Optional<Float> percentage, Optional<Map<Integer, Float>> thresholds) {
}
