package crackhash.manager.reciever;

import crackhash.manager.model.entities.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import crackhash.manager.database.CrackRequestRepo;
import crackhash.manager.database.PartRepo;
import crackhash.manager.model.dtos.WorkerPingDto;
import crackhash.manager.model.internal.ServiceInfoProvider;

import java.util.Optional;

@Component
class ReceiverImpl implements Receiver{
  private final CrackRequestRepo requestRepo;
  private final PartRepo partRepo;
  private final ServiceInfoProvider infoProvider;
  private final ObjectMapper jsonMapper;

  @Autowired
  ReceiverImpl(ObjectMapper jsonMapper, CrackRequestRepo cRequestRepo, ServiceInfoProvider infoProvider, PartRepo partRepo){
    this.requestRepo = cRequestRepo;
    this.partRepo = partRepo;
    this.infoProvider = infoProvider;
    this.jsonMapper = jsonMapper;
  }

  @Override
  public synchronized void receiveWorkerAnswer(String answerString) throws JsonProcessingException {
    System.out.println("Worker answer received");

    WorkerAnswer answer = jsonMapper.readValue(answerString, WorkerAnswer.class);
    RequestPart part = partRepo.findById(answer.partId()).orElse(null);

    if (null == part) {
      return;
    }

    CrackRequest req = requestRepo.findById(part.getRequestId()).orElse(null);
    if (null == req) {
      return;
    }

    if (part.getStatus().equals(PartStatus.SOLVED)) {
      return;
    }

    if (req.getStatus().equals(RequestStatus.READY)) {
      throw new RuntimeException("Inconsistent data has been found: got ACKED part for READY request.");
    }

    part.setStatus(PartStatus.SOLVED);
    
    req.getResult().addAll(answer.data());
    req.setAcs(req.getAcs() + 1);

    assert(req.getTotalParts() >= req.getAcs());

    if (req.getAcs().equals(req.getTotalParts())) {
      req.setStatus(RequestStatus.READY);
    }

    System.out.println("Save crack result");
    requestRepo.save(req);

    boolean saved = false;

    while (!saved) {
      saved = savePart(part);
      if (!saved) {
        part = partRepo.findById(part.getId()).get();
        part.setStatus(PartStatus.SOLVED);
      }
    }
  }

  private boolean savePart(RequestPart part){
    try {
      partRepo.save(part);
      return true;
    } catch (OptimisticLockingFailureException e) {
      return false;
    }
  }

  @Override
  public void receivePing(String pingString) throws JsonProcessingException {
    var ping = jsonMapper.readValue(pingString, WorkerPingDto.class);
    infoProvider.nullifyStreakOrAddAlive(ping.workerNum());
    System.out.println("Alive workers amount : " + infoProvider.getAmountOfAliveWorkers());
  }
}
