package nl.saxion.Models.DTOs;

import nl.saxion.Models.models.FilamentType;

import java.util.List;

public record PrintTaskDTO(PrintDTO print, List<String> colors, FilamentType filamentType) {
}
