package nsu.maxwell.controller;

import java.util.Calendar;
import java.util.concurrent.ConcurrentHashMap;

import nsu.maxwell.model.dto.PatchRequestBodyDto;
import nsu.maxwell.model.dto.PostRequestBody;
import nsu.maxwell.model.entity.CrackRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import nsu.maxwell.model.dto.CrackRequestDto;
import nsu.maxwell.model.dto.GetStatusDto;
import nsu.maxwell.model.dto.PostRequestResponse;

@RestController
public class ManagerController {
  @Value("${numWorkers}")
  private Integer numWorkers;

  @Value("${timeoutHalfMil}")
  private Integer timeoutHalfMil;

  private final ConcurrentHashMap<Integer, CrackRequest> requests = new ConcurrentHashMap<>();


  /**
   * Запрос на взлом хэша.
   * PostRequestBody (String hash, Integer maxLength)
   * PostRequestResponse (int id)
  **/
  @PostMapping("/api/hash/crack")
  public ResponseEntity<PostRequestResponse> postRequest(@RequestBody PostRequestBody request) {

    CrackRequest cr = new CrackRequest(
            request.hash(),
            request.maxLength(),
            timeoutHalfMil / 500);

    requests.put(cr.getId(), cr);

    sendTaskToWorkers(cr);

    return new ResponseEntity<>(new PostRequestResponse(cr.getId()), HttpStatus.OK);
  }

  /**
   * Раздача воркерам задач.
   * Каждый воркер стартует на своём параметризиваронном url.
   **/
  private void sendTaskToWorkers(CrackRequest crackRequest){
    RestTemplate restTemplate = new RestTemplate();

    for (int i = 1; i <= numWorkers; i++) {

      String addr = "http://localhost:8081/internal/api/worker/hash/crack/task";

      CrackRequestDto body = new CrackRequestDto(crackRequest, i, numWorkers);

      HttpEntity<CrackRequestDto> request = new HttpEntity<>(body);

      restTemplate.postForEntity(addr, request, Void.class);
    }
  }

  /**
   * Запрос статуса задачи. (ERROR, IN_PROGRESS, READY)
   * Может быть некорректный id
  **/
  @GetMapping("/api/hash/status")
  public ResponseEntity<GetStatusDto> getRequest(@RequestParam Integer requestId) {

    requests.entrySet().forEach(System.out::println);

    CrackRequest cr = requests.get(requestId);

    if (cr == null || cr.getStatus().equals(CrackRequest.CrackStatus.ERROR)) {
      return new ResponseEntity<>(new GetStatusDto(CrackRequest.CrackStatus.ERROR.name(), null), HttpStatus.OK);
    }

    else if (cr.getStatus().equals(CrackRequest.CrackStatus.IN_PROGRESS)) {
      return new ResponseEntity<>(new GetStatusDto(CrackRequest.CrackStatus.IN_PROGRESS.name(), null), HttpStatus.OK);
    }

    return new ResponseEntity<>(new GetStatusDto(CrackRequest.CrackStatus.READY.name(), cr.getResult()), HttpStatus.OK);
  }

  /**
   * Ожидание от воркера выполненной задачи
   * PatchRequestBodyDto (int id, ArrayList<String> data)
  **/
  @PatchMapping("/internal/api/manager/hash/crack/request")
  public void patchRequest(@RequestBody PatchRequestBodyDto pr) {
    try {
      CrackRequest cr = requests.get(pr.id());

      cr.addResult(pr.data());

      // количество воркеров, которые посчитали свою часть
      cr.increaseAcs();

      if (cr.getAcs() == numWorkers) {
        cr.setStatus(CrackRequest.CrackStatus.READY);
      }

    } catch (NullPointerException ignore) {}
  }

  /**
   *  Опрос задач, потенциальных для постановки в статус ERROR
   *  Периодичность запроса задается параметром timeoutHalfMil
  **/
  @Scheduled(fixedRateString = "${timeoutHalfMil}")
  private void checkTimeouts(){
    Calendar curTime = Calendar.getInstance();

    for (var entry : requests.entrySet()){
      // Только задачи IN_PROGRESS потенциальные для просрочки
      if (!entry.getValue().getStatus().equals(CrackRequest.CrackStatus.IN_PROGRESS)) {
        continue;
      }

      if (entry.getValue().getExpirationTime().before(curTime)){
        entry.getValue().setStatus(CrackRequest.CrackStatus.ERROR);
      }
    }
  }
}