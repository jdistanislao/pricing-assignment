package org.example.assignment.controller.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.NotNull;
import org.example.assignment.model.MarkdownType;

import java.util.Map;
import java.util.UUID;

@JsonSerialize
public class MarkdownDTO {

    public UUID id;

    @NotNull
    public MarkdownType type;

    public Float percentage;

    public Map<Integer, Float> thresholds;
}
