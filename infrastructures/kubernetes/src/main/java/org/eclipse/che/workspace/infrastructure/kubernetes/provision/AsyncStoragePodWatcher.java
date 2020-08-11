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

/**
 * Periodically checks ability to stop Asynchronous Storage Pod. It will periodically revise
 * UserPreferences of all registered user and check specialized preferences. Preferences should be
 * recorded if last workspace stopped and cleanup on start any workspace. Required preferences to
 * initiate Asynchronous Storage Pod: {@link
 * org.eclipse.che.api.workspace.shared.Constants.LAST_ACTIVE_INFRASTRUCTURE_NAMESPACE} : should
 * contain last used infrastructure namespace {@link
 * org.eclipse.che.api.workspace.shared.Constants.LAST_ACTIVE_WORKSPACE_ID} : should contain last
 * used workspace id {@link org.eclipse.che.api.workspace.shared.Constants.LAST_ACTIVITY_TIME} :
 * seconds then workspace stopped in the Java epoch format
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.provision;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.Long.parseLong;
import static org.eclipse.che.api.core.Pages.iterateLazily;
import static org.eclipse.che.api.workspace.shared.Constants.LAST_ACTIVE_INFRASTRUCTURE_NAMESPACE;
import static org.eclipse.che.api.workspace.shared.Constants.LAST_ACTIVITY_TIME;
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.AsyncStorageProvisioner.ASYNC_STORAGE;

import io.fabric8.kubernetes.api.model.DoneablePod;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.dsl.PodResource;
import java.time.Clock;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.user.server.PreferenceManager;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.api.workspace.server.WorkspaceRuntimes;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalRuntime;
import org.eclipse.che.commons.schedule.ScheduleDelay;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AsyncStoragePodWatcher {

  private static final Logger LOG = LoggerFactory.getLogger(AsyncStoragePodWatcher.class);

  private final KubernetesClientFactory kubernetesClientFactory;
  private final UserManager userManager;
  private final PreferenceManager preferenceManager;
  private final WorkspaceRuntimes runtimes;
  private final long shutdownTimeoutSec;
  private final Clock clock = Clock.systemDefaultZone();

  @Inject
  public AsyncStoragePodWatcher(
      KubernetesClientFactory kubernetesClientFactory,
      UserManager userManager,
      PreferenceManager preferenceManager,
      WorkspaceRuntimes runtimes,
      @Named("che.infra.kubernetes.async.storage.shutdown_timeout_min") long shutdownTimeoutMin) {
    this.kubernetesClientFactory = kubernetesClientFactory;
    this.userManager = userManager;
    this.preferenceManager = preferenceManager;
    this.runtimes = runtimes;
    this.shutdownTimeoutSec = shutdownTimeoutMin * 60;
  }

  @ScheduleDelay(
      unit = TimeUnit.MINUTES,
      initialDelay = 1,
      delayParameterName = "che.infra.kubernetes.async.storage.shutdown_check_period_min")
  public void check() {
    for (User user :
        iterateLazily((maxItems, skipCount) -> userManager.getAll(maxItems, skipCount))) {
      try {
        String owner = user.getId();
        Map<String, String> preferences = preferenceManager.find(owner);
        String lastTimeAccess = preferences.get(LAST_ACTIVITY_TIME);
        String namespace = preferences.get(LAST_ACTIVE_INFRASTRUCTURE_NAMESPACE);
        if (isNullOrEmpty(namespace)
            || isNullOrEmpty(lastTimeAccess)
            || isAnyRuntimeInProgress(owner)) {
          continue;
        }
        long lastTimeAccessSec = parseLong(lastTimeAccess);
        long epochSec = clock.instant().getEpochSecond();
        System.out.println(">>>>> :: epoch second" + epochSec);
        System.out.println(">>>>> :: lastTime access" + lastTimeAccessSec);
        long dif = epochSec - lastTimeAccessSec;
        System.out.println(">>>>> :: " + dif + " >= " + shutdownTimeoutSec);
        if (epochSec - lastTimeAccessSec >= shutdownTimeoutSec) {
          PodResource<Pod, DoneablePod> podDoneablePodPodResource =
              kubernetesClientFactory
                  .create()
                  .pods()
                  .inNamespace(namespace)
                  .withName(ASYNC_STORAGE);
          if (podDoneablePodPodResource.get() != null) {
            podDoneablePodPodResource.delete();
          }
        }
      } catch (InfrastructureException | ServerException e) {
        LOG.error(e.getMessage(), e);
      }
    }
  }

  /**
   * Going to check is currently owner has workspaces in progress: it's status is {@link
   * org.eclipse.che.api.core.model.workspace.WorkspaceStatus#STARTING} or {@link
   * org.eclipse.che.api.core.model.workspace.WorkspaceStatus#STOPPING})
   *
   * @param owner the user id to check
   */
  private boolean isAnyRuntimeInProgress(String owner)
      throws ServerException, InfrastructureException {
    Set<String> inProgress = runtimes.getInProgress();
    for (String wsId : inProgress) {
      InternalRuntime<?> internalRuntime = runtimes.getInternalRuntime(wsId);
      if (owner.equals(internalRuntime.getOwner())) {
        return true;
      }
    }
    return false;
  }
}
