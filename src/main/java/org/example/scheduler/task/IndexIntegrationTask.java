package org.example.scheduler.task;

import lombok.RequiredArgsConstructor;
import org.example.scheduler.BatchTask;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IndexIntegrationTask implements BatchTask {

  @Override
  public void execute() {
   System.out.println("run Index Integration Task");
  }
}
