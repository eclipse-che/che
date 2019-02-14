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
package org.eclipse.che.api.devfile.server.validator;

import static org.testng.Assert.fail;

import java.io.IOException;
import java.util.regex.Pattern;
import org.eclipse.che.api.devfile.server.DevfileFormatException;
import org.eclipse.che.api.devfile.server.schema.DevfileSchemaProvider;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.reporters.Files;

public class DevfileSchemaValidatorTest {

  private DevfileSchemaValidator schemaValidator;

  @BeforeClass
  public void setUp() throws Exception {
    schemaValidator = new DevfileSchemaValidator(new DevfileSchemaProvider());
  }

  @Test(dataProvider = "validDevfiles")
  public void shouldDoNotThrowExceptionOnValidationValidDevfile(String resourceFilePath)
      throws Exception {
    schemaValidator.validateBySchema(getResource(resourceFilePath), false);
  }

  @DataProvider
  public Object[][] validDevfiles() {
    return new Object[][] {
      {"editor_plugin_tool/devfile_editor_plugins.yaml"},
      {"kubernetes_openshift_tool/devfile_openshift_tool.yaml"},
      {"dockerimage_tool/devfile_dockerimage_tool.yaml"}
    };
  }

  @Test(dataProvider = "invalidDevfiles")
  public void shouldThrowExceptionOnValidationNonValidDevfile(
      String resourceFilePath, String expectedMessageRegexp) throws Exception {
    try {
      schemaValidator.validateBySchema(getResource(resourceFilePath), false);
    } catch (DevfileFormatException e) {
      if (!Pattern.matches(expectedMessageRegexp, e.getMessage())) {
        fail("DevfileFormatException with unexpected message is thrown: " + e.getMessage());
      }
      return;
    }
    fail("DevfileFormatException expected to be thrown but is was not");
  }

  @DataProvider
  public Object[][] invalidDevfiles() {
    return new Object[][] {
      // Devfile model testing
      {
        "devfile/devfile_missing_name.yaml",
        "Devfile schema validation failed. Errors: \\[object has missing required properties \\(\\[\"name\"\\]\\)\\]$"
      },
      {
        "devfile/devfile_missing_spec_version.yaml",
        "Devfile schema validation failed. Errors: \\[object has missing required properties \\(\\[\"specVersion\"\\]\\)\\]$"
      },
      {
        "devfile/devfile_with_undeclared_field.yaml",
        "Devfile schema validation failed. Errors: \\[object instance has properties which are not allowed by the schema: \\[\"unknown\"\\]\\]$"
      },
      // Tool model testing
      {
        "tool/devfile_missing_tool_name.yaml",
        "Devfile schema validation failed. Errors: \\[object has missing required properties \\(\\[\"name\"\\]\\)\\]$"
      },
      {
        "tool/devfile_missing_tool_type.yaml",
        "Devfile schema validation failed. Errors: \\[object has missing required properties \\(\\[\"type\"\\]\\)\\]$"
      },
      {
        "tool/devfile_tool_with_undeclared_field.yaml",
        "Devfile schema validation failed. Errors: \\[object instance has properties which are not allowed by the schema: \\[\"unknown\"\\]\\]$"
      },
      // Command model testing
      {
        "command/devfile_missing_command_name.yaml",
        "Devfile schema validation failed. Errors: \\[object has missing required properties \\(\\[\"name\"\\]\\)\\]$"
      },
      {
        "command/devfile_missing_command_actions.yaml",
        "Devfile schema validation failed. Errors: \\[object has missing required properties \\(\\[\"actions\"\\]\\)\\]$"
      },
      {
        "command/devfile_multiple_commands_actions.yaml",
        "Devfile schema validation failed. Errors: \\[array is too long: must have at most 1 elements but instance has 2 elements\\]$"
      },
      // cheEditor/chePlugin tool model testing
      {
        "editor_plugin_tool/devfile_editor_tool_with_missing_id.yaml",
        "Devfile schema validation failed\\. Errors: \\[instance failed to match exactly one schema \\(matched 0 out of 3\\)\\]"
      },
      // kubernetes/openshift tool model testing
      {
        "kubernetes_openshift_tool/devfile_openshift_tool_with_missing_local.yaml",
        "Devfile schema validation failed\\. Errors: \\[instance failed to match exactly one schema \\(matched 0 out of 3\\)\\]"
      },
      // Dockerimage tool model testing
      {
        "dockerimage_tool/devfile_dockerimage_tool_with_missing_image.yaml",
        "Devfile schema validation failed\\. Errors: \\[instance failed to match exactly one schema \\(matched 0 out of 3\\)\\]"
      },
      {
        "dockerimage_tool/devfile_dockerimage_tool_with_missing_memory_limit.yaml",
        "Devfile schema validation failed\\. Errors: \\[instance failed to match exactly one schema \\(matched 0 out of 3\\)\\]"
      },
    };
  }

  private String getResource(String name) throws IOException {
    return Files.readFile(getClass().getClassLoader().getResourceAsStream("schema_test/" + name));
  }
}
