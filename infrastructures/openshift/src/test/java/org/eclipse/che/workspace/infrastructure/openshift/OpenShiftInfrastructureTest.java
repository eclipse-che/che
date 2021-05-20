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
package org.eclipse.che.workspace.infrastructure.openshift;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.fabric8.kubernetes.client.Config;
import java.net.URI;
import java.util.Collections;
import javax.ws.rs.core.HttpHeaders;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.KubernetesRuntimeStateCache;
import org.eclipse.che.workspace.infrastructure.openshift.project.OpenShiftProjectFactory;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class OpenShiftInfrastructureTest {
  @Mock private OpenShiftClientFactory factory;
  private OpenShiftInfrastructure infra;

  @BeforeMethod
  public void setup() {
    infra =
        new OpenShiftInfrastructure(
            mock(EventService.class),
            mock(OpenShiftRuntimeContextFactory.class),
            Collections.emptySet(),
            mock(KubernetesRuntimeStateCache.class),
            mock(OpenShiftProjectFactory.class),
            factory);

    when(factory.getDefaultConfig()).thenReturn(mock(Config.class));
  }

  @Test
  public void testUsesAuthenticatedKubernetesClient() throws Exception {
    // when
    try {
      infra.sendDirectInfrastructureRequest(
          "GET", URI.create("somewhere/over/the/rainbow"), mock(HttpHeaders.class), null);
    } catch (Exception e) {
      // we don't care that this fails, because it fails during the execution of the HTTP request
      // that we intentionally don't set up fully.
      // it is enough for this test to verify that the code is trying to use the authenticated HTTP
      // client.
    }

    // then
    verify(factory).getAuthenticatedHttpClient();
  }
}
