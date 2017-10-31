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
public class CommitParamsTest {

  private static final String CONTAINER = "container";
  private static final String REPOSITORY = "repository";
  private static final String TAG = "tag";
  private static final String COMMENT = "comment";
  private static final String AUTHOR = "author";

  private CommitParams commitParams;

  @BeforeMethod
  private void prepare() {
    commitParams = CommitParams.create(CONTAINER);
  }

  @Test
  public void shouldCreateParamsObjectWithRequiredParameters() {
    commitParams = CommitParams.create(CONTAINER);

    assertEquals(commitParams.getContainer(), CONTAINER);

    assertNull(commitParams.getRepository());
    assertNull(commitParams.getTag());
    assertNull(commitParams.getComment());
    assertNull(commitParams.getAuthor());
  }

  @Test
  public void shouldCreateParamsObjectWithAllPossibleParameters() {
    commitParams =
        CommitParams.create(CONTAINER)
            .withRepository(REPOSITORY)
            .withTag(TAG)
            .withComment(COMMENT)
            .withAuthor(AUTHOR);

    assertEquals(commitParams.getContainer(), CONTAINER);
    assertEquals(commitParams.getRepository(), REPOSITORY);
    assertEquals(commitParams.getTag(), TAG);
    assertEquals(commitParams.getComment(), COMMENT);
    assertEquals(commitParams.getAuthor(), AUTHOR);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNullPointerExceptionIfContainerRequiredParameterIsNull() {
    commitParams = CommitParams.create(null);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNullPointerExceptionIfContainerRequiredParameterResetWithNull() {
    commitParams.withContainer(null);
  }

  @Test
  public void tagParameterShouldEqualsNullIfItNotSet() {
    commitParams.withAuthor(AUTHOR).withComment(COMMENT);

    assertNull(commitParams.getTag());
  }

  @Test
  public void repositoryParameterShouldEqualsNullIfItNotSet() {
    commitParams.withComment(COMMENT).withTag(TAG).withAuthor(AUTHOR);

    assertNull(commitParams.getRepository());
  }

  @Test
  public void commentParameterShouldEqualsNullIfItNotSet() {
    commitParams.withContainer(CONTAINER).withTag(TAG).withAuthor(AUTHOR);

    assertNull(commitParams.getComment());
  }

  @Test
  public void authorParameterShouldEqualsNullIfItNotSet() {
    commitParams.withContainer(CONTAINER).withTag(TAG).withComment(COMMENT);

    assertNull(commitParams.getAuthor());
  }
}
