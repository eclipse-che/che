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
package org.eclipse.che.api.devfile.server;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.eclipse.che.api.devfile.model.Devfile;
import org.eclipse.che.api.devfile.server.schema.DevfileSchemaProvider;
import org.eclipse.che.api.devfile.server.validator.DevfileIntegrityValidator;
import org.eclipse.che.api.devfile.server.validator.DevfileSchemaValidator;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.reporters.Files;

@Listeners(MockitoTestNGListener.class)
public class DevfileManagerTest {

  private DevfileSchemaValidator schemaValidator;
  private DevfileIntegrityValidator integrityValidator;
  private DevfileConverter devfileConverter;

  private DevfileManager devfileManager;

  @BeforeClass
  public void setUp() throws Exception {
    schemaValidator = spy(new DevfileSchemaValidator(new DevfileSchemaProvider()));
    integrityValidator = spy(new DevfileIntegrityValidator());
    devfileConverter = spy(new DevfileConverter());
    devfileManager = new DevfileManager(schemaValidator, integrityValidator, devfileConverter);
  }

  @Test
  public void testValidateAndConvert() throws Exception {
    String yamlContent =
        Files.readFile(getClass().getClassLoader().getResourceAsStream("devfile.yaml"));
    devfileManager.validateAndConvert(yamlContent, true);
    verify(schemaValidator).validateBySchema(eq(yamlContent), eq(true));
    verify(integrityValidator).validateDevfile(any(Devfile.class));
    verify(devfileConverter).devFileToWorkspaceConfig(any(Devfile.class));
  }
}
