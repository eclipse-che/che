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

import static io.fabric8.kubernetes.api.model.DeletionPropagation.BACKGROUND;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.CHE_WORKSPACE_ID_LABEL;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.EditReplacePatchDeletable;
import io.fabric8.kubernetes.client.dsl.FilterWatchListDeletable;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link KubernetesSecrets}.
 *
 * @author Sergii Leshchenko
 */
@Listeners(MockitoTestNGListener.class)
public class KubernetesSecretsTest {

  private static final String NAMESPACE = "namespace";
  private static final String WORKSPACE_ID = "workspace123";

  @Mock private KubernetesClient client;
  @Mock private KubernetesClientFactory clientFactory;

  @Mock private MixedOperation<Secret, SecretList, Resource<Secret>> secretsMixedOperation;

  @Mock private NonNamespaceOperation<Secret, SecretList, Resource<Secret>> nonNamespaceOperation;

  @Mock private FilterWatchListDeletable<Secret, SecretList> deletableList;
  @Mock private EditReplacePatchDeletable<Secret> deletableSecret;

  private KubernetesSecrets kubernetesSecrets;

  @BeforeMethod
  public void setUp() throws Exception {
    kubernetesSecrets = new KubernetesSecrets("namespace", "workspace123", clientFactory);

    when(clientFactory.create("workspace123")).thenReturn(client);

    when(client.secrets()).thenReturn(secretsMixedOperation);
    lenient().when(secretsMixedOperation.inNamespace(any())).thenReturn(nonNamespaceOperation);
    lenient().when(nonNamespaceOperation.withLabel(any(), any())).thenReturn(deletableList);
    lenient().doReturn(deletableSecret).when(deletableList).withPropagationPolicy(eq(BACKGROUND));
  }

  @Test
  public void testSecretCreation() throws Exception {
    Secret secret = new Secret();

    kubernetesSecrets.create(secret);

    assertEquals(secret.getMetadata().getLabels().get(CHE_WORKSPACE_ID_LABEL), WORKSPACE_ID);
    verify(secretsMixedOperation).inNamespace(NAMESPACE);
    verify(nonNamespaceOperation).create(secret);
  }

  @Test
  public void testSecretsRemoving() throws Exception {
    kubernetesSecrets.delete();

    verify(secretsMixedOperation).inNamespace(NAMESPACE);
    verify(nonNamespaceOperation).withLabel(CHE_WORKSPACE_ID_LABEL, WORKSPACE_ID);
    verify(deletableList).withPropagationPolicy(eq(BACKGROUND));
    verify(deletableSecret).delete();
  }
}
