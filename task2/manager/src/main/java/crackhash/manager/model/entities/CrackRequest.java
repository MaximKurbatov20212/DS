package crackhash.manager.model.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.data.annotation.Id;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class CrackRequest {

  @Id
  private final String id;
  private final String hash;
  private final Integer maxLen;
  private Integer totalParts;
  private RequestStatus status;
  private Integer acs;
  private ArrayList<String> result;

  public static List<RequestPart> splitRequestIntoParts(CrackRequest request){
    List<RequestPart> parts = new ArrayList<>();
    int totalParts = request.getTotalParts();

    for (int partNum = 1; partNum <= totalParts; partNum++) {
      parts.add(
        new RequestPart(
          null,
          null,
          request.getId(),
          request.getHash(),
          request.getMaxLen(),
          request.getTotalParts(),
          partNum,
          null,
          PartStatus.CREATED)
      );
    }
    return parts;
  }
}
