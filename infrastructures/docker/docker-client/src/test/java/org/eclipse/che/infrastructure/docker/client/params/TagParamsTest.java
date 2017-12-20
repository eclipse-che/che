/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Mykola Morhun */
public class TagParamsTest {

  private static final String IMAGE = "image";
  private static final String REPOSITORY = "repository";
  private static final String TAG = "teg";
  private static final Boolean FORCE = Boolean.TRUE;

  private TagParams tagParams;

  @BeforeMethod
  private void prepare() {
    tagParams = TagParams.create(IMAGE, REPOSITORY);
  }

  @Test
  public void shouldCreateParamsObjectWithRequiredParameters() {
    tagParams = TagParams.create(IMAGE, REPOSITORY);

    assertEquals(tagParams.getImage(), IMAGE);
    assertEquals(tagParams.getRepository(), REPOSITORY);

    assertNull(tagParams.getTag());
    assertNull(tagParams.isForce());
  }

  @Test
  public void shouldCreateParamsObjectWithAllPossibleParameters() {
    tagParams = TagParams.create(IMAGE, REPOSITORY).withTag(TAG).withForce(FORCE);

    assertEquals(tagParams.getImage(), IMAGE);
    assertEquals(tagParams.getRepository(), REPOSITORY);
    assertEquals(tagParams.getTag(), TAG);
    assertEquals(tagParams.isForce(), FORCE);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNullPointerExceptionIfImageRequiredParameterIsNull() {
    tagParams = TagParams.create(null, REPOSITORY);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNullPointerExceptionIfRepositoryRequiredParameterIsNull() {
    tagParams = TagParams.create(IMAGE, null);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNullPointerExceptionIfImageRequiredParameterResetWithNull() {
    tagParams.withImage(null);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowAnNullPointerExceptionIfRepositoryRequiredParameterResetWithNull() {
    tagParams.withRepository(null);
  }

  @Test
  public void tagParameterShouldEqualsNullIfItNotSet() {
    tagParams.withForce(FORCE);

    assertNull(tagParams.getTag());
  }

  @Test
  public void forceParameterShouldEqualsNullIfItNotSet() {
    tagParams.withTag(TAG);

    assertNull(tagParams.isForce());
  }
}
