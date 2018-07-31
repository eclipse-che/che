/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.core.executor.hotupdate;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.che.selenium.core.client.TestUserPreferencesServiceClient;
import org.eclipse.che.selenium.core.executor.OpenShiftCliCommandExecutor;
import org.eclipse.che.selenium.core.webdriver.WebDriverWaitFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;

/**
 * This is a set of methods which make easier to do updating of the che pod and wait of the updating
 * results.
 *
 * @author Ihor Okhrimenko
 */
@Singleton
public class HotUpdateUtil {
  private static final int TIMEOUT_FOR_FINISH_UPDATE_IN_SECONDS = 600;
  private static final int DELAY_BETWEEN_ATTEMPTS_IN_SECONDS = 5;
  private static final String UPDATE_COMMAND = "rollout latest che";
  private static final String PODS_LIST_COMMAND = "get pods | awk 'NR > 1 {print $1}'";
  private static final String COMMAND_FOR_GETTING_CURRENT_DEPLOYMENT_CHE =
      "get dc | grep che | awk '{print $2}'";

  private final OpenShiftCliCommandExecutor openShiftCliCommandExecutor;
  private final WebDriverWaitFactory webDriverWaitFactory;
  private final TestUserPreferencesServiceClient testUserPreferencesServiceClient;

  @Inject
  HotUpdateUtil(
      OpenShiftCliCommandExecutor openShiftCliCommandExecutor,
      WebDriverWaitFactory webDriverWaitFactory,
      TestUserPreferencesServiceClient testUserPreferencesServiceClient) {

    this.openShiftCliCommandExecutor = openShiftCliCommandExecutor;
    this.webDriverWaitFactory = webDriverWaitFactory;
    this.testUserPreferencesServiceClient = testUserPreferencesServiceClient;
  }

  /**
   * Waits during {@code timeout} until update is finished by checking that only single master pod
   * is present and it has incremented {@code masterVersionBeforeUpdate} number in name.
   *
   * @param masterRevisionBeforeUpdate - revision of the master pod before updating.
   * @param timeout - waiting time in seconds.
   * @throws Exception
   */
  public void waitFullMasterPodUpdate(int masterRevisionBeforeUpdate, int timeout)
      throws Exception {
    final int numberOfUpdatedVersion = masterRevisionBeforeUpdate + 1;
    final String beforeUpdatePodNamePattern = "che-" + masterRevisionBeforeUpdate;
    final String updatedPodNamePattern = "che-" + numberOfUpdatedVersion;

    waitPodNameDisappearance(beforeUpdatePodNamePattern, timeout);

    waitPodNamePatternHasSingleOccurrence(updatedPodNamePattern, timeout);
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
   * Waits during {@code timeout} until master pod has a specified {@code expectedRevision}.
   *
   * @param expectedRevision revision of the master pod.
   * @param timeout - waiting time in seconds.
   */
  public void waitMasterPodRevision(int expectedRevision, int timeout) {
    webDriverWaitFactory
        .get(timeout)
        .until((ExpectedCondition<Boolean>) driver -> expectedRevision == getMasterPodRevision());
  }

  /**
   * Waits until master pod has a specified {@code expectedRevision}.
   *
   * @param expectedRevision master pod revision.
   */
  public void waitMasterPodRevision(int expectedRevision) {
    waitMasterPodRevision(expectedRevision, TIMEOUT_FOR_FINISH_UPDATE_IN_SECONDS);
  }

  /**
   * Performs CLI command for master pod updating.
   *
   * @throws Exception
   */
  public void executeMasterPodUpdateCommand() throws Exception {
    openShiftCliCommandExecutor.execute(UPDATE_COMMAND);
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
      return Integer.parseInt(
          openShiftCliCommandExecutor.execute(COMMAND_FOR_GETTING_CURRENT_DEPLOYMENT_CHE));
    } catch (IOException ex) {
      throw new RuntimeException(ex.getLocalizedMessage(), ex);
    }
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

  private void waitPodNameDisappearance(String podName, int timeout) {
    webDriverWaitFactory
        .get(timeout, DELAY_BETWEEN_ATTEMPTS_IN_SECONDS)
        .until(
            (ExpectedCondition<Boolean>)
                driver -> {
                  try {
                    return !isAnyPodNameHasPatternOccurrence(podName);
                  } catch (Exception ex) {
                    throw new RuntimeException(ex.getLocalizedMessage(), ex);
                  }
                });
  }

  private boolean isPodNamePatternHasSingleOccurrence(String podNamePattern) throws Exception {
    return 1 == getPodsNamesWithPatternOccurrence(podNamePattern).size();
  }

  private void waitPodNamePatternHasSingleOccurrence(String podName, int timeout) {
    webDriverWaitFactory
        .get(timeout, DELAY_BETWEEN_ATTEMPTS_IN_SECONDS)
        .until(
            (ExpectedCondition<Boolean>)
                driver -> {
                  try {
                    return isPodNamePatternHasSingleOccurrence(podName);
                  } catch (Exception ex) {
                    throw new RuntimeException(ex);
                  }
                });
  }
}
