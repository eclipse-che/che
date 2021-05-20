/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.resource.api.workspace;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.eclipse.che.multiuser.resource.api.workspace.TestObjects.createConfig;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.multiuser.resource.api.exception.NoEnoughResourcesException;
import org.eclipse.che.multiuser.resource.api.type.RamResourceType;
import org.eclipse.che.multiuser.resource.api.type.RuntimeResourceType;
import org.eclipse.che.multiuser.resource.api.type.WorkspaceResourceType;
import org.eclipse.che.multiuser.resource.api.usage.ResourceManager;
import org.eclipse.che.multiuser.resource.api.usage.tracker.EnvironmentRamCalculator;
import org.eclipse.che.multiuser.resource.spi.impl.ResourceImpl;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link LimitsCheckingWorkspaceManager}.
 *
 * @author Yevhenii Voevodin
 * @author Alexander Garagatyi
 * @author Igor Vinokur
 * @author Sergii Leshchenko
 */
@Listeners(MockitoTestNGListener.class)
public class LimitsCheckingWorkspaceManagerTest {

  public static final String NAMESPACE = "namespace";
  public static final String ACCOUNT_ID = "accountId";
  @Mock private EnvironmentRamCalculator environmentRamCalculator;
  @Mock private ResourceManager resourceManager;

  @Test
  public void shouldUseRamOfSpecifiedEnvironmentOnCheckingAvailabilityOfRamResource()
      throws Exception {
    // given
    LimitsCheckingWorkspaceManager manager =
        managerBuilder()
            .setResourceManager(resourceManager)
            .setEnvironmentRamCalculator(environmentRamCalculator)
            .build();

    when(environmentRamCalculator.calculate(any(Environment.class))).thenReturn(3000L);

    WorkspaceConfig config = createConfig("3gb");
    String envToStart = config.getDefaultEnv();

    // when
    manager.checkRamResourcesAvailability(ACCOUNT_ID, NAMESPACE, config, envToStart);

    // then
    verify(environmentRamCalculator).calculate(config.getEnvironments().get(envToStart));
    verify(resourceManager)
        .checkResourcesAvailability(
            ACCOUNT_ID,
            singletonList(new ResourceImpl(RamResourceType.ID, 3000, RamResourceType.UNIT)));
  }

  @Test
  public void shouldUseRamOfDefaultEnvironmentOnCheckingAvailabilityOfRamResourceWhen()
      throws Exception {
    // given
    LimitsCheckingWorkspaceManager manager =
        managerBuilder()
            .setResourceManager(resourceManager)
            .setEnvironmentRamCalculator(environmentRamCalculator)
            .build();

    when(environmentRamCalculator.calculate(any(Environment.class))).thenReturn(3000L);

    WorkspaceConfig config = createConfig("3gb");

    // when
    manager.checkRamResourcesAvailability(ACCOUNT_ID, NAMESPACE, config, null);

    // then
    verify(environmentRamCalculator)
        .calculate(config.getEnvironments().get(config.getDefaultEnv()));
    verify(resourceManager)
        .checkResourcesAvailability(
            ACCOUNT_ID,
            singletonList(new ResourceImpl(RamResourceType.ID, 3000, RamResourceType.UNIT)));
  }

  @Test(
      expectedExceptions = LimitExceededException.class,
      expectedExceptionsMessageRegExp =
          "Workspace namespace/workspace.. needs 3000MB to start\\. "
              + "Your account has 200MB available and 100MB in use\\. "
              + "The workspace can't be start. Stop other workspaces or grant more resources\\.")
  public void shouldThrowLimitExceedExceptionIfAccountDoesNotHaveEnoughAvailableRamResource()
      throws Exception {
    doThrow(
            new NoEnoughResourcesException(
                singletonList(new ResourceImpl(RamResourceType.ID, 200L, RamResourceType.UNIT)),
                singletonList(new ResourceImpl(RamResourceType.ID, 3000L, RamResourceType.UNIT)),
                emptyList()))
        .when(resourceManager)
        .checkResourcesAvailability(any(), any());
    doReturn(singletonList(new ResourceImpl(RamResourceType.ID, 100L, RamResourceType.UNIT)))
        .when(resourceManager)
        .getUsedResources(any());

    // given
    LimitsCheckingWorkspaceManager manager =
        managerBuilder()
            .setResourceManager(resourceManager)
            .setEnvironmentRamCalculator(environmentRamCalculator)
            .build();

    when(environmentRamCalculator.calculate(any(Environment.class))).thenReturn(3000L);

    WorkspaceConfig config = createConfig("3gb");

    // when
    manager.checkRamResourcesAvailability(ACCOUNT_ID, NAMESPACE, config, null);
  }

  @Test
  public void shouldNotThrowLimitExceedExceptionIfAccountHasEnoughAvailableWorkspaceResource()
      throws Exception {
    // given
    LimitsCheckingWorkspaceManager manager =
        managerBuilder().setResourceManager(resourceManager).build();

    // when
    manager.checkWorkspaceResourceAvailability(ACCOUNT_ID);

    // then
    verify(resourceManager)
        .checkResourcesAvailability(
            ACCOUNT_ID,
            singletonList(
                new ResourceImpl(WorkspaceResourceType.ID, 1, WorkspaceResourceType.UNIT)));
  }

  @Test(
      expectedExceptions = LimitExceededException.class,
      expectedExceptionsMessageRegExp = "You are not allowed to create more workspaces\\.")
  public void shouldThrowLimitExceedExceptionIfAccountDoesNotHaveEnoughAvailableWorkspaceResource()
      throws Exception {
    // given
    doThrow(new NoEnoughResourcesException(emptyList(), emptyList(), emptyList()))
        .when(resourceManager)
        .checkResourcesAvailability(any(), any());
    doReturn(
            singletonList(
                new ResourceImpl(WorkspaceResourceType.ID, 5, WorkspaceResourceType.UNIT)))
        .when(resourceManager)
        .getTotalResources(anyString());
    LimitsCheckingWorkspaceManager manager =
        managerBuilder().setResourceManager(resourceManager).build();

    // when
    manager.checkWorkspaceResourceAvailability(ACCOUNT_ID);
  }

  @Test
  public void shouldNotThrowLimitExceedExceptionIfAccountHasEnoughAvailableRuntimeResource()
      throws Exception {
    // given
    LimitsCheckingWorkspaceManager manager =
        managerBuilder().setResourceManager(resourceManager).build();

    // when
    manager.checkRuntimeResourceAvailability(ACCOUNT_ID);

    // then
    verify(resourceManager)
        .checkResourcesAvailability(
            ACCOUNT_ID,
            singletonList(new ResourceImpl(RuntimeResourceType.ID, 1, RuntimeResourceType.UNIT)));
  }

  @Test(
      expectedExceptions = LimitExceededException.class,
      expectedExceptionsMessageRegExp = "You are not allowed to start more workspaces\\.")
  public void shouldThrowLimitExceedExceptionIfAccountDoesNotHaveEnoughAvailableRuntimeResource()
      throws Exception {
    // given
    doThrow(new NoEnoughResourcesException(emptyList(), emptyList(), emptyList()))
        .when(resourceManager)
        .checkResourcesAvailability(any(), any());
    doReturn(singletonList(new ResourceImpl(RuntimeResourceType.ID, 5, RuntimeResourceType.UNIT)))
        .when(resourceManager)
        .getTotalResources(anyString());
    LimitsCheckingWorkspaceManager manager =
        managerBuilder().setResourceManager(resourceManager).build();

    // when
    manager.checkRuntimeResourceAvailability(ACCOUNT_ID);
  }

  @Test(
      expectedExceptions = LimitExceededException.class,
      expectedExceptionsMessageRegExp = "You are only allowed to use 2048 mb. RAM per workspace.")
  public void shouldNotBeAbleToCreateWorkspaceWhichExceedsRamLimit() throws Exception {
    when(environmentRamCalculator.calculate(any(Environment.class))).thenReturn(3072L);
    final WorkspaceConfig config = createConfig("3gb");
    final LimitsCheckingWorkspaceManager manager =
        managerBuilder()
            .setMaxRamPerEnv("2gb")
            .setEnvironmentRamCalculator(environmentRamCalculator)
            .build();

    manager.checkMaxEnvironmentRam(config);
  }

  @Test
  public void shouldNotCheckWorkspaceRamLimitIfItIsSetToMinusOne() throws Exception {
    final WorkspaceConfig config = createConfig("3gb");
    final LimitsCheckingWorkspaceManager manager =
        managerBuilder()
            .setMaxRamPerEnv("-1")
            .setEnvironmentRamCalculator(environmentRamCalculator)
            .build();

    manager.checkMaxEnvironmentRam(config);

    verify(environmentRamCalculator, never()).calculate(any(Environment.class));
  }

  private static ManagerBuilder managerBuilder() throws ServerException {
    return new ManagerBuilder();
  }

  private static class ManagerBuilder {

    private String maxRamPerEnv;
    private EnvironmentRamCalculator environmentRamCalculator;
    private ResourceManager resourceManager;

    ManagerBuilder() throws ServerException {
      maxRamPerEnv = "1gb";
    }

    public LimitsCheckingWorkspaceManager build() {
      return spy(
          new LimitsCheckingWorkspaceManager(
              null,
              null,
              null,
              null,
              null,
              null,
              maxRamPerEnv,
              environmentRamCalculator,
              resourceManager,
              null,
              null));
    }

    ManagerBuilder setMaxRamPerEnv(String maxRamPerEnv) {
      this.maxRamPerEnv = maxRamPerEnv;
      return this;
    }

    ManagerBuilder setEnvironmentRamCalculator(EnvironmentRamCalculator environmentRamCalculator) {
      this.environmentRamCalculator = environmentRamCalculator;
      return this;
    }

    ManagerBuilder setResourceManager(ResourceManager resourceManager) {
      this.resourceManager = resourceManager;
      return this;
    }
  }
}
