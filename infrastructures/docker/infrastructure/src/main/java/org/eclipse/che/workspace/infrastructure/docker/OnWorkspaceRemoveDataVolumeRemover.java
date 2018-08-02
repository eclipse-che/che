/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.docker;

import static java.lang.String.format;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.workspace.shared.event.WorkspaceRemovedEvent;
import org.eclipse.che.infrastructure.docker.client.DockerConnector;
import org.eclipse.che.infrastructure.docker.client.json.volume.Volume;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.volume.VolumeNames;
import org.slf4j.Logger;

/** @author Alexander Garagatyi */
@Singleton
public class OnWorkspaceRemoveDataVolumeRemover implements EventSubscriber<WorkspaceRemovedEvent> {

  private static final Logger LOG = getLogger(OnWorkspaceRemoveDataVolumeRemover.class);

  private final DockerConnector docker;

  @Inject
  public OnWorkspaceRemoveDataVolumeRemover(DockerConnector docker) {
    this.docker = docker;
  }

  @Override
  public void onEvent(WorkspaceRemovedEvent event) {
    String wsId = event.getWorkspace().getId();
    try {
      List<Volume> volumes = docker.getVolumes().getVolumes();
      // May happen because Docker API is not very consistent on whether it returns null or empty
      // collection
      if (volumes == null) {
        return;
      }
      for (Volume volume : volumes) {
        String volumeName = volume.getName();
        if (VolumeNames.matches(volumeName, wsId)) {
          try {
            docker.removeVolume(volumeName);
          } catch (IOException e) {
            LOG.error(
                format(
                    "Error occurs on removing of volume '%s' of workspace '%s'. Error: %s",
                    volumeName, wsId, e.getLocalizedMessage()),
                e);
          }
        }
      }
    } catch (IOException e) {
      LOG.error(
          format(
              "Error occurs on removal of volumes of workspace '%s'. Error: %s",
              wsId, e.getLocalizedMessage()),
          e);
    }
  }

  @Inject
  public void subscribe(EventService eventService) {
    eventService.subscribe(this);
  }
}
