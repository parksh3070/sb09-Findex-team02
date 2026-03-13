package org.example.scheduler.task;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.entity.AutoSyncConfig;
import org.example.repository.AutoSyncConfigRepository;
import org.example.scheduler.BatchTask;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AutoSyncTask implements BatchTask {

  private AutoSyncConfigRepository autoSyncConfigRepository;

  @Override
  public void execute() {

    List<AutoSyncConfig> configList = autoSyncConfigRepository.findAllByEnabled(true);

    System.out.println("run Index Integration Task");
  }
}
