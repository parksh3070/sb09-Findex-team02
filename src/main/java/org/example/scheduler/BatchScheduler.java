package org.example.scheduler;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BatchScheduler {

  private final List<BatchTask> batchTaskList;

  @Scheduled(cron = "${findex.batch.cron}")
  public void run() {
    for (BatchTask task : batchTaskList) {
      task.execute();
    }
  }
}
