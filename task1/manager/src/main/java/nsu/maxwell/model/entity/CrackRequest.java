package nsu.maxwell.model.entity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.Getter;
import lombok.Setter;


public class CrackRequest {
  public enum CrackStatus {IN_PROGRESS, READY, ERROR}
  private final static AtomicInteger idGenerator = new AtomicInteger(0);
  @Getter
  private final Integer id;
  @Getter
  private final String hash;
  @Getter
  private final Integer maxLen;

  @Getter
  @Setter
  private CrackStatus status = CrackStatus.IN_PROGRESS;

  // количество воркеров, которые выполнили свою часть этой задачи
  private final AtomicInteger acs = new AtomicInteger(0);

  // Множество слов, хеш которых совпал с нужным
  private final ArrayList<String> result = new ArrayList<>();

  @Getter
  private final Calendar expirationTime;

  public CrackRequest(String hash, int maxLen, int timeoutSec){
    this.id = idGenerator.getAndIncrement();
    this.hash = hash;
    this.maxLen = maxLen;
    this.expirationTime = Calendar.getInstance();
    this.expirationTime.add(Calendar.SECOND, timeoutSec);
  }

  public int getAcs(){
    return acs.get();
  }

  public int increaseAcs(){
    return acs.incrementAndGet();
  }

  public ArrayList<String> getResult() {
    if (status.equals(CrackStatus.READY)) {
      return result;
    }

    return null;
  }

  public synchronized void addResult(ArrayList<String> workerRes) {
    result.addAll(workerRes);
  }
}
