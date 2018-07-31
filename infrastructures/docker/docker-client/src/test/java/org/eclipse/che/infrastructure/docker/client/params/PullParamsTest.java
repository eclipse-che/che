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
package org.eclipse.che.infrastructure.docker.client.params;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.eclipse.che.infrastructure.docker.auth.dto.AuthConfigs;
import org.mockito.Mock;
import org.testng.annotations.Test;

/** @author Mykola Morhun */
public class PullParamsTest {

  private static final String IMAGE = "image";
  private static final String TAG = "tag";
  private static final String REGISTRY = "registry";

  @Mock private AuthConfigs authConfigs;

  private PullParams pullParams;

  @Test
  public void shouldCreateParamsObjectWithRequiredParameters() {
    pullParams = PullParams.create(IMAGE);

    assertEquals(pullParams.getImage(), IMAGE);

    assertNull(pullParams.getTag());
    assertNull(pullParams.getRegistry());
    assertNull(pullParams.getAuthConfigs());
  }

  @Test
  public void shouldCreateParamsObjectWithAllPossibleParameters() {
    pullParams =
        PullParams.create(IMAGE).withTag(TAG).withRegistry(REGISTRY).withAuthConfigs(authConfigs);

    assertEquals(pullParams.getImage(), IMAGE);
    assertEquals(pullParams.getTag(), TAG);
    assertEquals(pullParams.getRegistry(), REGISTRY);
    assertEquals(pullParams.getAuthConfigs(), authConfigs);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNullPointerExceptionIfImageRequiredParameterIsNull() {
    pullParams = PullParams.create(null);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNullPointerExceptionIfImageRequiredParameterResetWithNull() {
    pullParams = PullParams.create(IMAGE).withImage(null);
  }

  @Test
  public void tagParameterShouldEqualsNullIfItNotSet() {
    pullParams = PullParams.create(IMAGE).withRegistry(REGISTRY).withAuthConfigs(authConfigs);

    assertNull(pullParams.getTag());
  }

  @Test
  public void registryParameterShouldEqualsNullIfItNotSet() {
    pullParams = PullParams.create(IMAGE).withTag(TAG).withAuthConfigs(authConfigs);

    assertNull(pullParams.getRegistry());
  }

  @Test
  public void AuthConfigsParameterShouldEqualsNullIfItNotSet() {
    pullParams = PullParams.create(IMAGE).withRegistry(REGISTRY).withTag(TAG);

    assertNull(pullParams.getAuthConfigs());
  }

  @Test
  public void getFullRepoShouldReturnRegistryAndImage() {
    pullParams = PullParams.create(IMAGE).withRegistry(REGISTRY).withTag(TAG);

    assertEquals(pullParams.getFullRepo(), REGISTRY + '/' + IMAGE);
  }

  @Test
  public void getFullRepoShouldReturnImageOnlyIfRegistryIsNotSet() {
    pullParams = PullParams.create(IMAGE).withTag(TAG);

    assertEquals(pullParams.getFullRepo(), IMAGE);
  }

  @Test
  public void getFullRepoShouldReturnImageOnlyIfRegistryIsDockerHub() {
    pullParams = PullParams.create(IMAGE).withRegistry("docker.io").withTag(TAG);

    assertEquals(pullParams.getFullRepo(), IMAGE);
  }
}
