/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.workspace.server.model.impl.RuntimeIdentityImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
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
  @Mock BrokersResult brokersResult;
  @Mock KubernetesPluginsToolingValidator validator;

  private BrokerStatusListener brokerStatusListener;

  @BeforeMethod
  public void setUp() {
    brokerStatusListener = new BrokerStatusListener(WORKSPACE_ID, validator, brokersResult);
  }

  @Test
  public void shouldDoNothingIfEventWithForeignWorkspaceIdIsReceived() {
    // given
    BrokerEvent event =
        new BrokerEvent()
            .withRuntimeId(new RuntimeIdentityImpl("foreignWorkspace", null, null, null));

    // when
    brokerStatusListener.onEvent(event);

    // then
    verifyNoMoreInteractions(brokersResult);
  }

  @Test
  public void shouldDoNothingIfEventWithoutRuntimeIdentityIsReceived() {
    // given
    BrokerEvent event = new BrokerEvent().withRuntimeId(null);

    // when
    brokerStatusListener.onEvent(event);

    // then
    verifyNoMoreInteractions(brokersResult);
  }

  @Test
  public void shouldAddResultWhenDoneEventIsReceivedAndToolingIsNotNull() throws Exception {
    // given
    BrokerEvent event =
        new BrokerEvent()
            .withRuntimeId(new RuntimeIdentityImpl(WORKSPACE_ID, null, null, null))
            .withStatus(BrokerStatus.DONE)
            .withTooling(emptyList());

    // when
    brokerStatusListener.onEvent(event);

    // then
    verify(brokersResult).setResult(emptyList());
  }

  @Test
  public void shouldSubmitErrorWhenDoneEventIsReceivedButToolingIsValidationFails()
      throws Exception {
    // given
    doThrow(new ValidationException("test")).when(validator).validatePluginNames(anyList());
    BrokerEvent event =
        new BrokerEvent()
            .withRuntimeId(new RuntimeIdentityImpl(WORKSPACE_ID, null, null, null))
            .withStatus(BrokerStatus.DONE)
            .withTooling(emptyList());

    // when
    brokerStatusListener.onEvent(event);

    // then
    verify(brokersResult).error(any(ValidationException.class));
  }

  @Test
  public void shouldNotCallAddResultWhenValidationFails() throws Exception {
    // given
    doThrow(new ValidationException("test")).when(validator).validatePluginNames(anyList());
    BrokerEvent event =
        new BrokerEvent()
            .withRuntimeId(new RuntimeIdentityImpl(WORKSPACE_ID, null, null, null))
            .withStatus(BrokerStatus.DONE)
            .withTooling(emptyList());

    // when
    brokerStatusListener.onEvent(event);

    // then
    verify(brokersResult, never()).setResult(anyList());
  }

  @Test
  public void shouldSubmitErrorWhenDoneEventIsReceivedButToolingIsNull() {
    // given
    BrokerEvent event =
        new BrokerEvent()
            .withRuntimeId(new RuntimeIdentityImpl(WORKSPACE_ID, null, null, null))
            .withStatus(BrokerStatus.DONE)
            .withTooling(null);

    // when
    brokerStatusListener.onEvent(event);

    // then
    verify(brokersResult).error(any(InternalInfrastructureException.class));
  }

  @Test
  public void shouldSubmitErrorWhenFailedEventIsReceived() {
    // given
    BrokerEvent event =
        new BrokerEvent()
            .withRuntimeId(new RuntimeIdentityImpl(WORKSPACE_ID, null, null, null))
            .withStatus(BrokerStatus.FAILED)
            .withError("error");

    // when
    brokerStatusListener.onEvent(event);

    // then
    verify(brokersResult).error(any(InfrastructureException.class));
  }
}
