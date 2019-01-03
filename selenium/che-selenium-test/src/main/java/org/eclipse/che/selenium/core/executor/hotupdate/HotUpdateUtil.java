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
package org.eclipse.che.selenium.core.executor.hotupdate;

import static java.lang.String.format;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import org.eclipse.che.selenium.core.client.TestUserPreferencesServiceClient;
import org.eclipse.che.selenium.core.executor.OpenShiftCliCommandExecutor;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.slf4j.LoggerFactory;

/**
 * This is a set of methods which make easier to do updating of the che pod and wait of the updating
 * results.
 *
 * @author Ihor Okhrimenko
 */
@Singleton
public class HotUpdateUtil {
  private static final int TIMEOUT_FOR_FINISH_UPDATE_IN_SECONDS = 600;
  private static final int DELAY_BETWEEN_ATTEMPTS_IN_MILLISECS = 5000;
  private static final String PODS_LIST_COMMAND = "get pods | awk 'NR > 1 {print $1}'";
  private static final String COMMAND_TO_GET_REVISION_OF_CHE_DEPLOYMENT_TEMPLATE =
      "get dc | grep %s | awk '{print $2}'";
  private static final String COMMAND_TO_GET_NAME_OF_CHE_DEPLOYMENT_TEMPLATE =
      "get dc | grep %s | awk '{print $1}'";
  private static final String UPDATE_COMMAND_TEMPLATE = "rollout latest %s";
  private static final String DEFAULT_CHE_OPENSHIFT_POD = "che";

  private final OpenShiftCliCommandExecutor openShiftCliCommandExecutor;
  private final TestUserPreferencesServiceClient testUserPreferencesServiceClient;

  @Inject(optional = true)
  @Named("che.openshift.pod")
  private String cheOpenshiftPod;

  @Inject
  public HotUpdateUtil(
      OpenShiftCliCommandExecutor openShiftCliCommandExecutor,
      TestUserPreferencesServiceClient testUserPreferencesServiceClient) {

    this.openShiftCliCommandExecutor = openShiftCliCommandExecutor;
    this.testUserPreferencesServiceClient = testUserPreferencesServiceClient;
  }

  /**
   * Waits during {@code timeoutInSec} until update is finished by checking that only single master
   * pod is present and it has incremented {@code masterVersionBeforeUpdate} number in name.
   *
   * @param masterRevisionBeforeUpdate - revision of the master pod before updating.
   * @param timeoutInSec - waiting time in seconds.
   */
  public void waitFullMasterPodUpdate(int masterRevisionBeforeUpdate, int timeoutInSec)
      throws TimeoutException, InterruptedException, ExecutionException {
    final int numberOfUpdatedVersion = masterRevisionBeforeUpdate + 1;
    final String beforeUpdatePodNamePattern = "che-" + masterRevisionBeforeUpdate;
    final String updatedPodNamePattern = "che-" + numberOfUpdatedVersion;

    waitPodNameDisappearance(beforeUpdatePodNamePattern, timeoutInSec);

    waitPodNamePatternHasSingleOccurrence(updatedPodNamePattern, timeoutInSec);
  }

  /**
   * Waits until update is finished by checking that only single master pod is present and it has
   * incremented {@code masterVersionBeforeUpdate} number in name.
   *
   * @param masterRevisionBeforeUpdate - revision of the master pod before updating.
   * @throws Exception
   */
  public void waitFullMasterPodUpdate(int masterRevisionBeforeUpdate) throws Exception {
    waitFullMasterPodUpdate(masterRevisionBeforeUpdate, TIMEOUT_FOR_FINISH_UPDATE_IN_SECONDS);
  }

  /**
   * Waits during {@code timeoutInSec} until master pod has a specified {@code expectedRevision}.
   *
   * @param expectedRevision revision of the master pod.
   * @param timeoutInSec - waiting time in seconds.
   */
  public void waitMasterPodRevision(int expectedRevision, int timeoutInSec)
      throws TimeoutException, InterruptedException, ExecutionException {
    WaitUtils.waitSuccessCondition(() -> expectedRevision == getMasterPodRevision(), timeoutInSec);
  }

  /**
   * Waits until master pod has a specified {@code expectedRevision}.
   *
   * @param expectedRevision master pod revision.
   */
  public void waitMasterPodRevision(int expectedRevision)
      throws TimeoutException, InterruptedException, ExecutionException {
    waitMasterPodRevision(expectedRevision, TIMEOUT_FOR_FINISH_UPDATE_IN_SECONDS);
  }

  /**
   * Performs CLI command for master pod updating.
   *
   * @throws Exception
   */
  public void executeMasterPodUpdateCommand() throws Exception {
    String updateCommand = format(UPDATE_COMMAND_TEMPLATE, getMasterPodName());
    openShiftCliCommandExecutor.execute(updateCommand);
  }

  /** Performs GET request to master pod API for checking its availability. */
  public void checkMasterPodAvailabilityByPreferencesRequest() {
    try {
      testUserPreferencesServiceClient.getPreferences();
    } catch (Exception ex) {
      throw new RuntimeException("Master POD is not available", ex);
    }
  }

  /**
   * Performs CLI request to the master pod for getting its revision.
   *
   * @return revision of the master pod.
   */
  public int getMasterPodRevision() {
    try {
      String getRevisionOfCheDeploymentCommand = getRevisionOfCheDeploymentCommand();
      LoggerFactory.getLogger(this.getClass())
          .info("getRevisionOfCheDeploymentCommand: " + getRevisionOfCheDeploymentCommand);
      return Integer.parseInt(
          openShiftCliCommandExecutor.execute(getRevisionOfCheDeploymentCommand()));
    } catch (IOException ex) {
      throw new RuntimeException(ex.getLocalizedMessage(), ex);
    }
  }

  /**
   * Performs CLI request to the master pod to get its name.
   *
   * @return name of Che master pod.
   */
  public String getMasterPodName() {
    try {
      String getNameOfCheDeploymentCommand = getNameOfCheDeploymentCommand();
      LoggerFactory.getLogger(this.getClass())
          .info("getNameOfCheDeploymentCommand: " + getNameOfCheDeploymentCommand);

      return openShiftCliCommandExecutor.execute(getNameOfCheDeploymentCommand());
    } catch (IOException ex) {
      throw new RuntimeException(ex.getLocalizedMessage(), ex);
    }
  }

  private String getRevisionOfCheDeploymentCommand() {
    return format(
        COMMAND_TO_GET_REVISION_OF_CHE_DEPLOYMENT_TEMPLATE,
        cheOpenshiftPod != null ? cheOpenshiftPod : DEFAULT_CHE_OPENSHIFT_POD);
  }

  private String getNameOfCheDeploymentCommand() {
    return format(
        COMMAND_TO_GET_NAME_OF_CHE_DEPLOYMENT_TEMPLATE,
        cheOpenshiftPod != null ? cheOpenshiftPod : DEFAULT_CHE_OPENSHIFT_POD);
  }

  private List<String> getPods() throws Exception {
    return Arrays.asList(openShiftCliCommandExecutor.execute(PODS_LIST_COMMAND).split("\n"));
  }

  private boolean isAnyPodNameHasPatternOccurrence(String pattern) throws Exception {
    return !getPodsNamesWithPatternOccurrence(pattern).isEmpty();
  }

  private List<String> getPodsNamesWithPatternOccurrence(String pattern) throws Exception {
    return getPods()
        .stream()
        .filter(podName -> podName.contains(pattern))
        .collect(Collectors.toList());
  }

  private void waitPodNameDisappearance(String podName, int timeout)
      throws TimeoutException, InterruptedException, ExecutionException {
    WaitUtils.waitSuccessCondition(
        () -> {
          try {
            return !isAnyPodNameHasPatternOccurrence(podName);
          } catch (Exception ex) {
            throw new RuntimeException(ex.getLocalizedMessage(), ex);
          }
        },
        timeout,
        DELAY_BETWEEN_ATTEMPTS_IN_MILLISECS,
        TimeUnit.SECONDS);
  }

  private boolean isPodNamePatternHasSingleOccurrence(String podNamePattern) throws Exception {
    return 1 == getPodsNamesWithPatternOccurrence(podNamePattern).size();
  }

  private void waitPodNamePatternHasSingleOccurrence(String podName, int timeout)
      throws TimeoutException, InterruptedException, ExecutionException {
    WaitUtils.waitSuccessCondition(
        () -> {
          try {
            return isPodNamePatternHasSingleOccurrence(podName);
          } catch (Exception ex) {
            throw new RuntimeException(ex);
          }
        },
        timeout,
        DELAY_BETWEEN_ATTEMPTS_IN_MILLISECS,
        TimeUnit.SECONDS);
  }
}
