package nsu.maxwell.controller;

import jakarta.annotation.PostConstruct;
import nsu.maxwell.Ping;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
public class ManagerController {
  @Value("${numWorkers}")
  private Integer numWorkers;

  @Value("${timeout}")
  private Integer timeout;
  List<Status> workers = Arrays.asList(Status.UNAVAILABLE, Status.UNAVAILABLE, Status.UNAVAILABLE);
  Status managerStatus = Status.UNAVAILABLE;


  enum Status {
    AVAILABLE,
    UNAVAILABLE
  }

  /**
   * Запрос статуса задачи. (ERROR, IN_PROGRESS, READY)
   * Может быть некорректный id
   * GetStatusDto(String status, ArrayList<String> data)
  **/
  @GetMapping("/api/hash/status")
  public String getRequest(@RequestParam Integer requestId) {
    return "ok";
  }

  @PostConstruct
  void init() {
    System.out.println("All service unavailable");
  }

  @Scheduled(fixedRateString = "${timeout}")
  private void checkTimeouts(){
    RestTemplate restTemplate = new RestTemplate();

    for (int i = 1; i <= numWorkers; i++) {
      String addr = "http://worker" + i + ":8080/api/worker/ping";
      try {
        ResponseEntity<Ping> response = restTemplate.postForEntity(addr, null, Ping.class);
        if (workers.get(i - 1) == Status.UNAVAILABLE) {
          workers.set(i - 1, Status.AVAILABLE);
          System.out.println("Worker " + (i - 1) + " available");
        }
      }
      catch (RestClientException e) {
        if (workers.get(i - 1) == Status.AVAILABLE) {
          workers.set(i - 1, Status.UNAVAILABLE);
          System.out.println("Worker " + (i - 1) + " unavailable");
        }
      }
    }

    String addr = "http://manager:8080/api/manager/ping";
    try {
      ResponseEntity<Ping> response = restTemplate.postForEntity(addr, null, Ping.class);
      if (managerStatus == Status.UNAVAILABLE) {
        managerStatus = Status.AVAILABLE;
        System.out.println("Manager available");
      }
    } catch(RestClientException e) {
      if (managerStatus == Status.AVAILABLE) {
        managerStatus = Status.UNAVAILABLE;
        System.out.println("Manager unavailable");
      }
    }
  }
}