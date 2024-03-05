package nsu.maxwell.model.dto;

import java.util.ArrayList;

public record PatchRequestBodyDto(int id, ArrayList<String> data) {}
