/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.infrastructure.docker.client.params;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.eclipse.che.infrastructure.docker.client.dto.AuthConfigs;
import org.mockito.Mock;
import org.testng.annotations.Test;

/** @author Mykola Morhun */
public class PushParamsTest {

  private static final String REPOSITORY = "repository";
  private static final String TAG = "tag";
  private static final String REGISTRY = "registry";

  @Mock private AuthConfigs authConfigs;

  private PushParams pushParams;

  @Test
  public void shouldCreateParamsObjectWithRequiredParameters() {
    pushParams = PushParams.create(REPOSITORY);

    assertEquals(pushParams.getRepository(), REPOSITORY);

    assertNull(pushParams.getTag());
    assertNull(pushParams.getRegistry());
    assertNull(pushParams.getAuthConfigs());
  }

  @Test
  public void shouldCreateParamsObjectWithAllPossibleParameters() {
    pushParams =
        PushParams.create(REPOSITORY)
            .withTag(TAG)
            .withRegistry(REGISTRY)
            .withAuthConfigs(authConfigs);

    assertEquals(pushParams.getRepository(), REPOSITORY);
    assertEquals(pushParams.getTag(), TAG);
    assertEquals(pushParams.getRegistry(), REGISTRY);
    assertEquals(pushParams.getAuthConfigs(), authConfigs);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNullPointerExceptionIfRepositoryRequiredParameterIsNull() {
    pushParams = PushParams.create(null);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNullPointerExceptionIfRepositoryRequiredParameterResetWithNull() {
    pushParams = PushParams.create(REPOSITORY).withRepository(null);
  }

  @Test
  public void tagParameterShouldEqualsNullIfItNotSet() {
    pushParams = PushParams.create(REPOSITORY).withRegistry(REGISTRY).withAuthConfigs(authConfigs);

    assertNull(pushParams.getTag());
  }

  @Test
  public void registryParameterShouldEqualsNullIfItNotSet() {
    pushParams = PushParams.create(REPOSITORY).withTag(TAG).withAuthConfigs(authConfigs);

    assertNull(pushParams.getRegistry());
  }

  @Test
  public void authConfigsParameterShouldEqualsNullIfItNotSet() {
    pushParams = PushParams.create(REPOSITORY).withTag(TAG).withRegistry(REGISTRY);

    assertNull(pushParams.getAuthConfigs());
  }

  @Test
  public void getFullRepoShouldReturnRegistryAndRepository() {
    pushParams = PushParams.create(REPOSITORY).withRegistry(REGISTRY).withTag(TAG);

    assertEquals(pushParams.getFullRepo(), REGISTRY + '/' + REPOSITORY);
  }

  @Test
  public void getFullRepoShouldReturnRepositoryOnlyIfRegistryIsNotSet() {
    pushParams = PushParams.create(REPOSITORY).withTag(TAG);

    assertEquals(pushParams.getFullRepo(), REPOSITORY);
  }

  @Test
  public void getFullRepoShouldReturnRepositoryOnlyIfRegistryIsDockerHub() {
    pushParams = PushParams.create(REPOSITORY).withRegistry("docker.io").withTag(TAG);

    assertEquals(pushParams.getFullRepo(), REPOSITORY);
  }
}
