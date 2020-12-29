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
package org.eclipse.che.api.factory.server.scm.kubernetes;

import static java.lang.String.format;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

import io.fabric8.kubernetes.api.model.DoneableSecret;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import java.util.Base64;
import java.util.Collections;
import org.eclipse.che.api.factory.server.scm.PersonalAccessToken;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.api.server.impls.KubernetesNamespaceMetaImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.api.shared.KubernetesNamespaceMeta;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespaceFactory;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class KubernetesGitCredentialManagerTest {

  @Mock private KubernetesNamespaceFactory namespaceFactory;
  @Mock private KubernetesClientFactory clientFactory;

  @Mock private KubernetesClient kubeClient;

  @Mock
  private MixedOperation<Secret, SecretList, DoneableSecret, Resource<Secret, DoneableSecret>>
      secretsMixedOperation;

  @Mock
  NonNamespaceOperation<Secret, SecretList, DoneableSecret, Resource<Secret, DoneableSecret>>
      nonNamespaceOperation;

  KubernetesGitCredentialManager kubernetesGitCredentialManager;

  @BeforeMethod
  protected void init() {
    kubernetesGitCredentialManager =
        new KubernetesGitCredentialManager(namespaceFactory, clientFactory);
    assertNotNull(this.kubernetesGitCredentialManager);
  }

  @Test
  public void testCreateAndSaveHitCredential() throws Exception {
    KubernetesNamespaceMeta meta = new KubernetesNamespaceMetaImpl("test");
    when(namespaceFactory.list()).thenReturn(Collections.singletonList(meta));

    when(clientFactory.create()).thenReturn(kubeClient);
    when(kubeClient.secrets()).thenReturn(secretsMixedOperation);
    when(secretsMixedOperation.inNamespace(eq(meta.getName()))).thenReturn(nonNamespaceOperation);
    ArgumentCaptor<Secret> captor = ArgumentCaptor.forClass(Secret.class);

    PersonalAccessToken token =
        new PersonalAccessToken(
            "https://bitbucket.com", "cheUser", "username", "userId", "token123");

    // when
    kubernetesGitCredentialManager.createOrReplace(token);
    // then
    verify(nonNamespaceOperation).createOrReplace(captor.capture());
    Secret createdSecret = captor.getValue();
    assertNotNull(createdSecret);
    assertEquals(
        createdSecret.getData().get("credentials"),
        Base64.getEncoder()
            .encodeToString(
                format(
                        "%s://%s:%s@%s",
                        token.getScmProviderProtocol(),
                        token.getUserName(),
                        token.getToken(),
                        token.getScmProviderHost())
                    .getBytes()));
  }
}
