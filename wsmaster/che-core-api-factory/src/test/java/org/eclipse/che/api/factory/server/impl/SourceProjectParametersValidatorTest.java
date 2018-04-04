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
package org.eclipse.che.api.factory.server.impl;

import java.util.HashMap;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.factory.FactoryParameter;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.eclipse.che.dto.server.DtoFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SourceProjectParametersValidatorTest {
  private SourceStorageParametersValidator validator;

  private SourceStorageDto sourceStorage;

  @BeforeMethod
  public void setUp() throws Exception {
    validator = new SourceStorageParametersValidator();

    sourceStorage =
        DtoFactory.getInstance()
            .createDto(SourceStorageDto.class)
            .withLocation("location")
            .withType("git")
            .withParameters(
                new HashMap<String, String>() {
                  {
                    put("branch", "master");
                    put("commitId", "123456");
                    put("keepVcs", "true");
                    put("fetch", "12345");
                    put("keepDir", "/src");
                  }
                });
  }

  @Test
  public void shouldBeAbleValidateGitSource() throws Exception {
    validator.validate(sourceStorage, FactoryParameter.Version.V4_0);
  }

  @Test
  public void shouldBeAbleValidateESBWSO2Source() throws Exception {
    sourceStorage.setType("esbwso2");

    validator.validate(sourceStorage, FactoryParameter.Version.V4_0);
  }

  @Test(
    expectedExceptions = ConflictException.class,
    expectedExceptionsMessageRegExp =
        "You have provided an invalid parameter .* for this version of Factory parameters.*"
  )
  public void shouldThrowExceptionIfUnknownParameterIsUsed() throws Exception {
    sourceStorage.getParameters().put("other", "value");

    validator.validate(sourceStorage, FactoryParameter.Version.V4_0);
  }

  @Test(
    expectedExceptions = ConflictException.class,
    expectedExceptionsMessageRegExp =
        "The parameter .* has a value submitted .* with a value that is unexpected.*"
  )
  public void shouldThrowExceptionIfKeepVcsIsNotTrueOrFalse() throws Exception {
    sourceStorage.getParameters().put("keepVcs", "qwerty");

    validator.validate(sourceStorage, FactoryParameter.Version.V4_0);
  }
}
