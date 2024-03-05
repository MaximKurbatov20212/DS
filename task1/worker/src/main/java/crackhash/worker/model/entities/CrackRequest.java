package crackhash.worker.model.entities;

import java.util.ArrayList;

import crackhash.worker.model.dtos.CrackRequestDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CrackRequest {
  private final Integer id;
  private final String hash;
  private final Integer maxLen;
  private final Integer partNum;
  private final Integer partTotal;
  private ArrayList<String> result = new ArrayList<>();

  public CrackRequest(CrackRequestDto dto){
    this.id = dto.id();
    this.hash = dto.hash();
    this.maxLen = dto.maxLen();
    this.partNum = dto.partNum();
    this.partTotal = dto.partTotal();
  }
}