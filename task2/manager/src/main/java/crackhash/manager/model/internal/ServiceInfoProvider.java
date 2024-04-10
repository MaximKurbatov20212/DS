package crackhash.manager.model.internal;

import java.util.ArrayList;

import com.google.common.collect.ImmutableSet;

public interface ServiceInfoProvider {

  ImmutableSet<Integer> getAliveWorkersOrWait() throws InterruptedException;

  void increaseStreakForAllWorkers();

  ImmutableSet<Integer> getAliveWorkers();
  
  String getWorkerRouteKey(int workerNum);

  int getAmountOfAliveWorkers();

  void nullifyStreakOrAddAlive(int worker);

  ArrayList<Integer> removeDeadWorkers();

  boolean isRecovered();

  void makeRecovered();
}
