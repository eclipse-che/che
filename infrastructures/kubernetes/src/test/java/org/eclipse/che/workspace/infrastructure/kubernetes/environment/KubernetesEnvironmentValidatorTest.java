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
package org.eclipse.che.workspace.infrastructure.kubernetes.environment;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link KubernetesEnvironmentValidator}.
 *
 * @author Sergii Leshchenko
 */
@Listeners(MockitoTestNGListener.class)
public class KubernetesEnvironmentValidatorTest {
  @Mock private KubernetesEnvironmentPodsValidator podsValidator;

  @Mock private KubernetesEnvironment kubernetesEnvironment;

  @InjectMocks private KubernetesEnvironmentValidator environmentValidator;

  @Test
  public void shouldPerformChecksOnEnvironmentValidation() throws Exception {
    // when
    environmentValidator.validate(kubernetesEnvironment);

    // then
    podsValidator.validate(kubernetesEnvironment);
  }
}
