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

package org.eclipse.che.workspace.infrastructure.kubernetes.provision;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.Long.parseLong;
import static java.time.Instant.now;
import static java.time.Instant.ofEpochSecond;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.eclipse.che.api.core.Pages.iterateLazily;
import static org.eclipse.che.api.workspace.shared.Constants.LAST_ACTIVE_INFRASTRUCTURE_NAMESPACE;
import static org.eclipse.che.api.workspace.shared.Constants.LAST_ACTIVITY_TIME;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.CommonPVCStrategy.COMMON_STRATEGY;
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.AsyncStorageProvisioner.ASYNC_STORAGE;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.dsl.PodResource;
import io.fabric8.kubernetes.client.dsl.RollableScalableResource;
import java.time.Instant;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.user.server.PreferenceManager;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.api.workspace.server.WorkspaceRuntimes;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.schedule.ScheduleDelay;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Periodically checks ability to stop Asynchronous Storage Pod. It will periodically revise
 * UserPreferences of all registered user and check specialized preferences. Preferences should be
 * recorded if last workspace stopped and cleanup on start any workspace. Required preferences to
 * initiate stop procedure for Asynchronous Storage Pod : {@link
 * org.eclipse.che.api.workspace.shared.Constants#LAST_ACTIVE_INFRASTRUCTURE_NAMESPACE} : should
 * contain last used infrastructure namespace {@link
 * org.eclipse.che.api.workspace.shared.Constants#LAST_ACTIVITY_TIME} : seconds then workspace
 * stopped in the Java epoch time format (aka Unix time)
 */
@Singleton
public class AsyncStoragePodWatcher {

  private static final Logger LOG = LoggerFactory.getLogger(AsyncStoragePodWatcher.class);

  private final KubernetesClientFactory kubernetesClientFactory;
  private final UserManager userManager;
  private final PreferenceManager preferenceManager;
  private final WorkspaceRuntimes runtimes;
  private final long shutdownTimeoutSec;
  private final boolean isAsyncStoragePodCanBeRun;

  @Inject
  public AsyncStoragePodWatcher(
      KubernetesClientFactory kubernetesClientFactory,
      UserManager userManager,
      PreferenceManager preferenceManager,
      WorkspaceRuntimes runtimes,
      @Named("che.infra.kubernetes.async.storage.shutdown_timeout_min") long shutdownTimeoutMin,
      @Named("che.infra.kubernetes.pvc.strategy") String pvcStrategy,
      @Named("che.infra.kubernetes.namespace.allow_user_defined")
          boolean allowUserDefinedNamespaces,
      @Nullable @Named("che.infra.kubernetes.namespace.default") String defaultNamespaceName,
      @Named("che.limits.user.workspaces.run.count") int runtimesPerUser) {
    this.kubernetesClientFactory = kubernetesClientFactory;
    this.userManager = userManager;
    this.preferenceManager = preferenceManager;
    this.runtimes = runtimes;
    this.shutdownTimeoutSec = MINUTES.toSeconds(shutdownTimeoutMin);

    isAsyncStoragePodCanBeRun =
        isAsyncStoragePodCanBeRun(
            pvcStrategy, allowUserDefinedNamespaces, defaultNamespaceName, runtimesPerUser);
  }

  /**
   * Checking current system configuration on ability to run Async Storage Pod. Will be checked next
   * value of properties:
   *
   * <ul>
   *   <li>che.infra.kubernetes.namespace.default=<username>-che
   *   <li>che.infra.kubernetes.namespace.allow_user_defined=false
   *   <li>che.infra.kubernetes.pvc.strategy=common
   *   <li>che.limits.user.workspaces.run.count=1
   * </ul>
   */
  private boolean isAsyncStoragePodCanBeRun(
      String pvcStrategy,
      boolean allowUserDefinedNamespaces,
      String defaultNamespaceName,
      int runtimesPerUser) {
    return !allowUserDefinedNamespaces
        && COMMON_STRATEGY.equals(pvcStrategy)
        && runtimesPerUser == 1
        && !isNullOrEmpty(defaultNamespaceName)
        && defaultNamespaceName.contains("<username>");
  }

  @ScheduleDelay(
      unit = MINUTES,
      initialDelay = 1,
      delayParameterName = "che.infra.kubernetes.async.storage.shutdown_check_period_min")
  public void check() {
    if (isAsyncStoragePodCanBeRun
        && shutdownTimeoutSec
            > 0) { // if system not support async storage mode or idling time set to 0 or less
      // do nothing
      for (User user :
          iterateLazily((maxItems, skipCount) -> userManager.getAll(maxItems, skipCount))) {
        try {
          String owner = user.getId();
          Map<String, String> preferences = preferenceManager.find(owner);
          String lastTimeAccess = preferences.get(LAST_ACTIVITY_TIME);
          String namespace = preferences.get(LAST_ACTIVE_INFRASTRUCTURE_NAMESPACE);

          if (isNullOrEmpty(namespace)
              || isNullOrEmpty(lastTimeAccess)
              || !runtimes.getInProgress(owner).isEmpty()) {
            continue;
          }
          long lastTimeAccessSec = parseLong(lastTimeAccess);
          Instant expectedShutdownAfter =
              ofEpochSecond(lastTimeAccessSec).plusSeconds(shutdownTimeoutSec);
          if (now().isAfter(expectedShutdownAfter)) {
            removeAsyncStoragePodWithoutDeployment(namespace);
            RollableScalableResource<Deployment> doneableResource =
                kubernetesClientFactory
                    .create()
                    .apps()
                    .deployments()
                    .inNamespace(namespace)
                    .withName(ASYNC_STORAGE);
            if (doneableResource.get() != null) {
              doneableResource.delete();
            }
          }
        } catch (InfrastructureException | ServerException e) {
          LOG.error(e.getMessage(), e);
        }
      }
    }
  }

  /**
   * Cleanup existed Async Storage pods running without Deployment see
   * https://github.com/eclipse/che/issues/17616. Method can be removed in 7.20.x
   *
   * @param namespace
   * @throws InfrastructureException
   */
  private void removeAsyncStoragePodWithoutDeployment(String namespace)
      throws InfrastructureException {
    PodResource<Pod> doneablePodResource =
        kubernetesClientFactory.create().pods().inNamespace(namespace).withName(ASYNC_STORAGE);
    if (doneablePodResource.get() != null) {
      doneablePodResource.delete();
    }
  }
}
