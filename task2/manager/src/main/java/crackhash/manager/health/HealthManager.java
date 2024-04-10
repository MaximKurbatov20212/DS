package crackhash.manager.health;

import java.util.ArrayList;

import crackhash.manager.model.entities.CrackRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import crackhash.manager.database.CrackRequestRepo;
import crackhash.manager.database.PartRepo;
import crackhash.manager.model.entities.PartStatus;
import crackhash.manager.model.entities.RequestStatus;
import crackhash.manager.model.internal.ServiceInfoProvider;
import crackhash.manager.sender.CommandFactory;
import crackhash.manager.sender.PartSender;

@Component
class HealthManager {
  private final PartSender sender;
  private final CommandFactory commandFactory;
  private final ServiceInfoProvider infoProvider;
  private final CrackRequestRepo requestRepo;
  private final PartRepo partRepo;

  private boolean wasZero = true;
  private boolean recovered = false;

  @Autowired
  public HealthManager(
    CommandFactory commandFactory,
    PartSender sender,
    ServiceInfoProvider infoProvider,
    CrackRequestRepo cRequestRepo,
    PartRepo partRepo){
    this.sender = sender;
    this.infoProvider = infoProvider;
    this.requestRepo = cRequestRepo;
    this.partRepo = partRepo;
    this.commandFactory = commandFactory;
  }

  private void makeRecovery() throws InterruptedException{
      var requests = requestRepo.findByStatusAndTotalPartsNot(RequestStatus.IN_PROGRESS, 0);

      for (CrackRequest req : requests) {
        if (partRepo.findFirst1ByRequestId(req.getId()).isEmpty()) {
          req.setTotalParts(0);
          requestRepo.save(req);
        }
      }

      // created parts can be not in sender's queue
      sender.addCommandToExecute(commandFactory.getResendCreatedCommand());
      resendZeroParts();

      var aliveWorkers = new ArrayList<>(infoProvider.getAliveWorkers());

      if (aliveWorkers.isEmpty()) {
        aliveWorkers.add(-1);
      }

      var possibleLostParts = partRepo.findByStatusAndWorkerNumNotIn(PartStatus.ACKED, aliveWorkers);
      possibleLostParts.addAll(partRepo.findByStatusAndWorkerNumNotIn(PartStatus.NOT_ACKED, aliveWorkers));

      if (!possibleLostParts.isEmpty()) {
        sender.addPartsToSend(possibleLostParts);
        sender.addCommandToExecute(commandFactory.getSendCommand());
      }

      infoProvider.makeRecovered();
  }
  
  @Scheduled(fixedRateString = "${pingTickMillis}")
  private void checkTimeouts() throws InterruptedException{
    infoProvider.increaseStreakForAllWorkers();

    ArrayList<Integer> removed = infoProvider.removeDeadWorkers();
    int aliveCount = infoProvider.getAmountOfAliveWorkers();

    if (!recovered && aliveCount > 0) {
      makeRecovery();
      recovered = infoProvider.isRecovered();
      return;
    }

    if (!recovered) {
      return;
    }

    if (wasZero && 0 != aliveCount) {
      resendZeroParts();
      wasZero = false;
    }
    else if (!wasZero && 0 == aliveCount) {
      wasZero = true;
    }

    for (var worker : removed) {
      resendKicked(worker);
    }
  }

  private void resendZeroParts() throws InterruptedException{
    sender.addCommandToExecute(commandFactory.getResendZeroPartsCommand());
  }

  private void resendKicked(int worker) throws InterruptedException{
    sender.addCommandToExecute(commandFactory.getResendCommand(worker));
  }
}
