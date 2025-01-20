package nl.saxion.Models.DTOs;

import java.util.List;

public record PrintDTO(String name, int height, int width, int length, List<Double> filamentLength, int printTime) {
}
