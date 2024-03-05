package nsu.maxwell.model.dto;

import nsu.maxwell.model.entity.CrackRequest;
import lombok.Getter;

@Getter
public class CrackRequestDto {
  private final Integer id;
  
  private final String hash;

  private final int maxLen;

  private final int partNum;

  private final int partTotal;

  public CrackRequestDto(CrackRequest cr, int partNum, int partTotal){
    id = cr.getId();
    hash = cr.getHash();
    maxLen = cr.getMaxLen();
    this.partNum = partNum;
    this.partTotal = partTotal;
  }
}
