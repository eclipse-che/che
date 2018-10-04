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
package org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.events;

import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.eclipse.che.api.workspace.server.model.impl.RuntimeIdentityImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.api.workspace.server.wsplugins.model.ChePlugin;
import org.eclipse.che.api.workspace.shared.dto.BrokerStatus;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.BrokersResult;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.KubernetesPluginsToolingValidator;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link BrokerStatusListener}.
 *
 * @author Sergii Leshchenko
 */
@Listeners(MockitoTestNGListener.class)
public class BrokerStatusListenerTest {

  public static final String WORKSPACE_ID = "workspace123";
  @Mock private CompletableFuture<List<ChePlugin>> finishFuture;

  private BrokerStatusListener brokerStatusListener;

  @BeforeMethod
  public void setUp() {
    brokerStatusListener =
        new BrokerStatusListener(
            WORKSPACE_ID, new KubernetesPluginsToolingValidator(), new BrokersResult());
  }

  @Test
  public void shouldDoNothingIfEventWithForeignWorkspaceIdIsReceived() {
    // given
    BrokerEvent event =
        new BrokerEvent().withRuntimeId(new RuntimeIdentityImpl("foreignWorkspace", null, null));

    // when
    brokerStatusListener.onEvent(event);

    // then
    verifyNoMoreInteractions(finishFuture);
  }

  @Test
  public void shouldDoNothingIfEventWithoutRuntimeIdentityIsReceived() {
    // given
    BrokerEvent event = new BrokerEvent().withRuntimeId(null);

    // when
    brokerStatusListener.onEvent(event);

    // then
    verifyNoMoreInteractions(finishFuture);
  }

  @Test
  public void shouldCompleteFinishFutureWhenDoneEventIsReceivedAndToolingIsNotNull() {
    // given
    BrokerEvent event =
        new BrokerEvent()
            .withRuntimeId(new RuntimeIdentityImpl(WORKSPACE_ID, null, null))
            .withStatus(BrokerStatus.DONE)
            .withTooling(emptyList());

    // when
    brokerStatusListener.onEvent(event);

    // then
    verify(finishFuture).complete(emptyList());
  }

  @Test
  public void shouldCompleteExceptionallyFinishFutureWhenDoneEventIsReceivedButToolingIsNull() {
    // given
    BrokerEvent event =
        new BrokerEvent()
            .withRuntimeId(new RuntimeIdentityImpl(WORKSPACE_ID, null, null))
            .withStatus(BrokerStatus.DONE)
            .withTooling(null);

    // when
    brokerStatusListener.onEvent(event);

    // then
    verify(finishFuture).completeExceptionally(any(InternalInfrastructureException.class));
  }

  @Test
  public void shouldCompleteExceptionallyFinishFutureWhenFailedEventIsReceived() {
    // given
    BrokerEvent event =
        new BrokerEvent()
            .withRuntimeId(new RuntimeIdentityImpl(WORKSPACE_ID, null, null))
            .withStatus(BrokerStatus.FAILED)
            .withError("error");

    // when
    brokerStatusListener.onEvent(event);

    // then
    verify(finishFuture).completeExceptionally(any(InfrastructureException.class));
  }
}
