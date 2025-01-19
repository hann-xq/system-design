package nl.saxion.Models.DTOs;

import nl.saxion.Models.models.FilamentType;

public record SpoolDTO(int id, String color, FilamentType filamentType, double length) {
}
