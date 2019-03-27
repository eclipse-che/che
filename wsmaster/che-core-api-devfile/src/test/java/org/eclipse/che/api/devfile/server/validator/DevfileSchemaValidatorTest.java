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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.io.IOException;
import org.eclipse.che.api.devfile.server.exception.DevfileFormatException;
import org.eclipse.che.api.devfile.server.schema.DevfileSchemaProvider;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.reporters.Files;

public class DevfileSchemaValidatorTest {

  private DevfileSchemaValidator schemaValidator;

  @BeforeClass
  public void setUp() {
    schemaValidator = new DevfileSchemaValidator(new DevfileSchemaProvider());
  }

  @Test(dataProvider = "validDevfiles")
  public void shouldNotThrowExceptionOnValidationValidDevfile(String resourceFilePath)
      throws Exception {
    schemaValidator.validateBySchema(getResource(resourceFilePath));
  }

  @DataProvider
  public Object[][] validDevfiles() {
    return new Object[][] {
      {"editor_plugin_component/devfile_editor_plugins.yaml"},
      {"kubernetes_openshift_component/devfile_kubernetes_component_local.yaml"},
      {
        "kubernetes_openshift_component/devfile_kubernetes_component_local_and_content_as_block.yaml"
      },
      {"kubernetes_openshift_component/devfile_openshift_component.yaml"},
      {"kubernetes_openshift_component/devfile_openshift_component_local_and_content.yaml"},
      {
        "kubernetes_openshift_component/devfile_openshift_component_local_and_content_as_block.yaml"
      },
      {"dockerimage_component/devfile_dockerimage_component.yaml"},
      {"dockerimage_component/devfile_dockerimage_component_without_entry_point.yaml"}
    };
  }

  @Test(dataProvider = "invalidDevfiles")
  public void shouldThrowExceptionOnValidationNonValidDevfile(
      String resourceFilePath, String expectedMessageRegexp) throws Exception {
    try {
      schemaValidator.validateBySchema(getResource(resourceFilePath));
    } catch (DevfileFormatException e) {
      assertEquals(
          e.getMessage(),
          expectedMessageRegexp,
          "DevfileFormatException thrown with message that doesn't match expected pattern:");
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
        "Devfile schema validation failed. Error: /devfile object has missing required properties ([\"name\"])"
      },
      {
        "devfile/devfile_missing_spec_version.yaml",
        "Devfile schema validation failed. Error: /devfile object has missing required properties ([\"specVersion\"])"
      },
      {
        "devfile/devfile_with_undeclared_field.yaml",
        "Devfile schema validation failed. Error: /devfile object instance has properties which are not allowed by the schema: [\"unknown\"]"
      },
      // component model testing
      {
        "component/devfile_missing_component_name.yaml",
        "Devfile schema validation failed. Error: /devfile/components/0 object has missing required properties ([\"name\"])"
      },
      {
        "component/devfile_missing_component_type.yaml",
        "Devfile schema validation failed. Error: /devfile/components/0 object has missing required properties ([\"type\"])"
      },
      {
        "component/devfile_component_with_undeclared_field.yaml",
        "Devfile schema validation failed. Errors: [/devfile/components/0 object instance has properties which are not allowed by the schema: [\"unknown\"],"
            + "instance failed to match exactly one schema (matched 0 out of 3),"
            + "/devfile/components/0 object instance has properties which are not allowed by the schema: [\"unknown\"],"
            + "/devfile/components/0 object instance has properties which are not allowed by the schema: [\"id\",\"unknown\"],"
            + "/devfile/components/0 object has missing required properties ([\"local\"]),"
            + "/devfile/components/0 object instance has properties which are not allowed by the schema: [\"id\",\"unknown\"],"
            + "/devfile/components/0 object has missing required properties ([\"image\",\"memoryLimit\"])]"
      },
      // Command model testing
      {
        "command/devfile_missing_command_name.yaml",
        "Devfile schema validation failed. Error: /devfile/commands/0 object has missing required properties ([\"name\"])"
      },
      {
        "command/devfile_missing_command_actions.yaml",
        "Devfile schema validation failed. Error: /devfile/commands/0 object has missing required properties ([\"actions\"])"
      },
      {
        "command/devfile_multiple_commands_actions.yaml",
        "Devfile schema validation failed. Error: /devfile/commands/0/actions array is too long: must have at most 1 elements but instance has 2 elements"
      },
      // cheEditor/chePlugin component model testing
      {
        "editor_plugin_component/devfile_editor_component_with_missing_id.yaml",
        "Devfile schema validation failed. Errors: [instance failed to match exactly one schema (matched 0 out of 3),"
            + "/devfile/components/0 object has missing required properties ([\"id\"]),"
            + "/devfile/components/0 object has missing required properties ([\"local\"]),"
            + "/devfile/components/0 object has missing required properties ([\"image\",\"memoryLimit\"])]"
      },
      {
        "editor_plugin_component/devfile_editor_component_with_indistinctive_field_local.yaml",
        "Devfile schema validation failed. Errors: [instance failed to match exactly one schema (matched 0 out of 3),"
            + "/devfile/components/0 object instance has properties which are not allowed by the schema: [\"local\"],"
            + "/devfile/components/0 object instance has properties which are not allowed by the schema: [\"id\"],"
            + "/devfile/components/0 object instance has properties which are not allowed by the schema: [\"id\",\"local\"],"
            + "/devfile/components/0 object has missing required properties ([\"image\",\"memoryLimit\"])]"
      },
      // kubernetes/openshift component model testing
      {
        "kubernetes_openshift_component/devfile_openshift_component_with_missing_local.yaml",
        "Devfile schema validation failed. Errors: [instance failed to match exactly one schema (matched 0 out of 3),"
            + "/devfile/components/0 object has missing required properties ([\"id\"]),"
            + "/devfile/components/0 object has missing required properties ([\"local\"]),"
            + "/devfile/components/0 object has missing required properties ([\"image\",\"memoryLimit\"])]"
      },
      {
        "kubernetes_openshift_component/devfile_openshift_component_content_without_local.yaml",
        "Devfile schema validation failed. Errors: [/devfile/components/0 property \"localContent\" of object has missing property dependencies (schema requires [\"local\"]; missing: [\"local\"]),"
            + "instance failed to match exactly one schema (matched 0 out of 3),"
            + "/devfile/components/0 object instance has properties which are not allowed by the schema: [\"localContent\",\"selector\"],"
            + "/devfile/components/0 object has missing required properties ([\"id\"]),"
            + "/devfile/components/0 object has missing required properties ([\"local\"]),"
            + "/devfile/components/0 object instance has properties which are not allowed by the schema: [\"localContent\",\"selector\"],"
            + "/devfile/components/0 object has missing required properties ([\"image\",\"memoryLimit\"])]"
      },
      {
        "kubernetes_openshift_component/devfile_kubernetes_component_content_without_local.yaml",
        "Devfile schema validation failed. Errors: [/devfile/components/0 property \"localContent\" of object has missing property dependencies (schema requires [\"local\"]; missing: [\"local\"]),"
            + "instance failed to match exactly one schema (matched 0 out of 3),"
            + "/devfile/components/0 object instance has properties which are not allowed by the schema: [\"localContent\",\"selector\"],"
            + "/devfile/components/0 object has missing required properties ([\"id\"]),"
            + "/devfile/components/0 object has missing required properties ([\"local\"]),"
            + "/devfile/components/0 object instance has properties which are not allowed by the schema: [\"localContent\",\"selector\"],"
            + "/devfile/components/0 object has missing required properties ([\"image\",\"memoryLimit\"])]"
      },
      {
        "kubernetes_openshift_component/devfile_openshift_component_with_indistinctive_field_id.yaml",
        "Devfile schema validation failed. Errors: [instance failed to match exactly one schema (matched 0 out of 3)"
            + ",/devfile/components/0 object instance has properties which are not allowed by the schema: [\"local\",\"selector\"],"
            + "/devfile/components/0 object instance has properties which are not allowed by the schema: [\"id\"],"
            + "/devfile/components/0 object instance has properties which are not allowed by the schema: [\"id\",\"local\",\"selector\"],"
            + "/devfile/components/0 object has missing required properties ([\"image\",\"memoryLimit\"])]"
      },
      // Dockerimage component model testing
      {
        "dockerimage_component/devfile_dockerimage_component_with_missing_image.yaml",
        "Devfile schema validation failed. Errors: [instance failed to match exactly one schema (matched 0 out of 3),"
            + "/devfile/components/0 object instance has properties which are not allowed by the schema: [\"memoryLimit\"],"
            + "/devfile/components/0 object has missing required properties ([\"id\"]),"
            + "/devfile/components/0 object instance has properties which are not allowed by the schema: [\"memoryLimit\"],"
            + "/devfile/components/0 object has missing required properties ([\"local\"]),"
            + "/devfile/components/0 object has missing required properties ([\"image\"])]"
      },
      {
        "dockerimage_component/devfile_dockerimage_component_with_missing_memory_limit.yaml",
        "Devfile schema validation failed. Errors: [instance failed to match exactly one schema (matched 0 out of 3),"
            + "/devfile/components/0 object instance has properties which are not allowed by the schema: [\"image\"],"
            + "/devfile/components/0 object has missing required properties ([\"id\"]),"
            + "/devfile/components/0 object instance has properties which are not allowed by the schema: [\"image\"],/devfile/components/0 object has missing required properties ([\"local\"]),"
            + "/devfile/components/0 object has missing required properties ([\"memoryLimit\"])]"
      },
      {
        "dockerimage_component/devfile_dockerimage_component_with_indistinctive_field_selector.yaml",
        "Devfile schema validation failed. Errors: [instance failed to match exactly one schema (matched 0 out of 3),"
            + "/devfile/components/0 object instance has properties which are not allowed by the schema: [\"endpoints\",\"env\",\"image\",\"memoryLimit\",\"selector\",\"volumes\"],"
            + "/devfile/components/0 object has missing required properties ([\"id\"]),"
            + "/devfile/components/0 object instance has properties which are not allowed by the schema: [\"endpoints\",\"env\",\"image\",\"memoryLimit\",\"volumes\"],"
            + "/devfile/components/0 object has missing required properties ([\"local\"]),"
            + "/devfile/components/0 object instance has properties which are not allowed by the schema: [\"selector\"]]"
      },
    };
  }

  private String getResource(String name) throws IOException {
    return Files.readFile(getClass().getClassLoader().getResourceAsStream("schema_test/" + name));
  }
}
