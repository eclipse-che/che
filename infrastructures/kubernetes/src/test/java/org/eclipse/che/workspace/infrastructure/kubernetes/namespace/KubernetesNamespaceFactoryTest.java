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
package org.eclipse.che.workspace.infrastructure.kubernetes.namespace;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.eclipse.che.commons.subject.SubjectImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link KubernetesNamespaceFactory}.
 *
 * @author Sergii Leshchenko
 */
@Listeners(MockitoTestNGListener.class)
public class KubernetesNamespaceFactoryTest {
  @Mock private KubernetesClientFactory clientFactory;
  private KubernetesNamespaceFactory namespaceFactory;

  @Test
  public void shouldReturnTrueIfNamespaceIsNotEmptyOnCheckingIfNamespaceIsPredefined() {
    // given
    namespaceFactory = new KubernetesNamespaceFactory("predefined", "", "", clientFactory);

    // when
    boolean isPredefined = namespaceFactory.isPredefined();

    // then
    assertTrue(isPredefined);
  }

  @Test
  public void shouldReturnTrueIfNamespaceIsEmptyOnCheckingIfNamespaceIsPredefined() {
    // given
    namespaceFactory = new KubernetesNamespaceFactory("", "", "", clientFactory);

    // when
    boolean isPredefined = namespaceFactory.isPredefined();

    // then
    assertFalse(isPredefined);
  }

  @Test
  public void shouldReturnTrueIfNamespaceIsNullOnCheckingIfNamespaceIsPredefined() {
    // given
    namespaceFactory = new KubernetesNamespaceFactory(null, "", "", clientFactory);

    // when
    boolean isPredefined = namespaceFactory.isPredefined();

    // then
    assertFalse(isPredefined);
  }

  @Test
  public void shouldCreateAndPrepareNamespaceWithPredefinedValueIfItIsNotEmpty() throws Exception {
    // given
    namespaceFactory = spy(new KubernetesNamespaceFactory("predefined", "", "", clientFactory));
    KubernetesNamespace toReturnNamespace = mock(KubernetesNamespace.class);
    doReturn(toReturnNamespace).when(namespaceFactory).doCreateNamespace(any(), any());

    // when
    KubernetesNamespace namespace = namespaceFactory.create("workspace123");

    // then
    assertEquals(toReturnNamespace, namespace);
    verify(namespaceFactory).doCreateNamespace("workspace123", "predefined");
    verify(toReturnNamespace).prepare();
  }

  @Test
  public void shouldCreateAndPrepareNamespaceWithWorkspaceIdAsNameIfConfiguredNameIsNotPredefined()
      throws Exception {
    // given
    namespaceFactory = spy(new KubernetesNamespaceFactory("", "", "", clientFactory));
    KubernetesNamespace toReturnNamespace = mock(KubernetesNamespace.class);
    doReturn(toReturnNamespace).when(namespaceFactory).doCreateNamespace(any(), any());

    // when
    KubernetesNamespace namespace = namespaceFactory.create("workspace123");

    // then
    assertEquals(toReturnNamespace, namespace);
    verify(namespaceFactory).doCreateNamespace("workspace123", "workspace123");
    verify(toReturnNamespace).prepare();
  }

  @Test
  public void
      shouldCreateNamespaceAndDoNotPrepareNamespaceOnCreatingNamespaceWithWorkspaceIdAndNameSpecified()
          throws Exception {
    // given
    namespaceFactory = spy(new KubernetesNamespaceFactory("", "", "", clientFactory));
    KubernetesNamespace toReturnNamespace = mock(KubernetesNamespace.class);
    doReturn(toReturnNamespace).when(namespaceFactory).doCreateNamespace(any(), any());

    // when
    KubernetesNamespace namespace = namespaceFactory.create("workspace123", "name");

    // then
    assertEquals(toReturnNamespace, namespace);
    verify(namespaceFactory).doCreateNamespace("workspace123", "name");
    verify(toReturnNamespace, never()).prepare();
  }

  @Test
  public void shouldPrepareWorkspaceServiceAccountIfItIsConfiguredAndNamespaceIsNotPredefined()
      throws Exception {
    // given
    namespaceFactory = spy(new KubernetesNamespaceFactory("", "serviceAccount", "", clientFactory));
    KubernetesNamespace toReturnNamespace = mock(KubernetesNamespace.class);
    doReturn(toReturnNamespace).when(namespaceFactory).doCreateNamespace(any(), any());

    KubernetesWorkspaceServiceAccount serviceAccount =
        mock(KubernetesWorkspaceServiceAccount.class);
    doReturn(serviceAccount).when(namespaceFactory).doCreateServiceAccount(any(), any());

    // when
    namespaceFactory.create("workspace123");

    // then
    verify(namespaceFactory).doCreateServiceAccount("workspace123", "workspace123");
    verify(serviceAccount).prepare();
  }

  @Test
  public void shouldNotPrepareWorkspaceServiceAccountIfItIsConfiguredAndProjectIsPredefined()
      throws Exception {
    // given
    namespaceFactory =
        spy(
            new KubernetesNamespaceFactory(
                "namespace", "serviceAccount", "clusterRole", clientFactory));
    KubernetesNamespace toReturnNamespace = mock(KubernetesNamespace.class);
    doReturn(toReturnNamespace).when(namespaceFactory).doCreateNamespace(any(), any());

    KubernetesWorkspaceServiceAccount serviceAccount =
        mock(KubernetesWorkspaceServiceAccount.class);
    doReturn(serviceAccount).when(namespaceFactory).doCreateServiceAccount(any(), any());

    // when
    namespaceFactory.create("workspace123");

    // then
    verify(namespaceFactory, never()).doCreateServiceAccount(any(), any());
  }

  @Test
  public void shouldNotPrepareWorkspaceServiceAccountIfItIsNotConfiguredAndProjectIsNotPredefined()
      throws Exception {
    // given
    namespaceFactory = spy(new KubernetesNamespaceFactory("", "", "", clientFactory));
    KubernetesNamespace toReturnNamespace = mock(KubernetesNamespace.class);
    doReturn(toReturnNamespace).when(namespaceFactory).doCreateNamespace(any(), any());

    KubernetesWorkspaceServiceAccount serviceAccount =
        mock(KubernetesWorkspaceServiceAccount.class);
    doReturn(serviceAccount).when(namespaceFactory).doCreateServiceAccount(any(), any());

    // when
    namespaceFactory.create("workspace123");

    // then
    verify(namespaceFactory, never()).doCreateServiceAccount(any(), any());
  }

  @Test
  public void testPlaceholder() {
    namespaceFactory =
        new KubernetesNamespaceFactory(
            "blabol-<userid>-<username>-<userid>-<username>--", "", "", clientFactory);
    String namespace =
        namespaceFactory.evalNamespaceName(null, new SubjectImpl("JonDoe", "123", null, false));

    assertEquals(namespace, "blabol-123-JonDoe-123-JonDoe--");
  }
}
