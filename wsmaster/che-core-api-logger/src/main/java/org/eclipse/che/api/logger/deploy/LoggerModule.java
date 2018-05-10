package org.eclipse.che.api.logger.deploy;

import com.google.inject.AbstractModule;
import org.eclipse.che.api.logger.ErrorInstallerLogEventLogger;
import org.eclipse.che.api.logger.ErrorMachineLogEventLogger;

public class LoggerModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(org.eclipse.che.api.logger.LoggerService.class);
    bind(ErrorInstallerLogEventLogger.class).asEagerSingleton();
    bind(ErrorMachineLogEventLogger.class).asEagerSingleton();
  }
}
