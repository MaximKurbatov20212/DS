package crackhash.worker.model.dtos;

public record CrackRequestDto(Integer id, String hash, int maxLen, int partNum, int partTotal) {}
