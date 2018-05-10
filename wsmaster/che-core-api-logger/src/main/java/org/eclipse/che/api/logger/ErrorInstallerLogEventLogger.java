package org.eclipse.che.api.logger;

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.workspace.shared.dto.RuntimeIdentityDto;
import org.eclipse.che.api.workspace.shared.dto.event.InstallerLogEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * The goal of this class it to catch all InstallerLogEvent events from error stream and dump it to
 * slf4j log.
 */
@Singleton
public class ErrorInstallerLogEventLogger implements EventSubscriber<InstallerLogEvent> {

  private static final Logger LOG = LoggerFactory.getLogger(ErrorInstallerLogEventLogger.class);

  @Inject private EventService eventService;

  @PostConstruct
  public void subscribe() {
    eventService.subscribe(this, InstallerLogEvent.class);
  }

  @Override
  public void onEvent(InstallerLogEvent event) {
    InstallerLogEvent.Stream stream = event.getStream();
    String text = event.getText();
    if (stream != null && stream == InstallerLogEvent.Stream.STDERR && !isNullOrEmpty(text)) {
      RuntimeIdentityDto identity = event.getRuntimeId();
      LOG.error(
          "Installer {} error from machine {} owner {} env {} workspace {} stream {} text {} time {} ",
          event.getInstaller(),
          event.getMachineName(),
          identity.getOwnerId(),
          identity.getEnvName(),
          identity.getWorkspaceId(),
          event.getStream(),
          event.getText(),
          event.getTime());
    }
  }
}
