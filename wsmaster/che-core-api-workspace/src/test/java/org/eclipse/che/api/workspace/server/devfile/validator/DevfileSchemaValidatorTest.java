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
package org.eclipse.che.api.workspace.server.devfile.validator;

import static java.lang.String.format;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.io.IOException;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileFormatException;
import org.eclipse.che.api.workspace.server.devfile.schema.DevfileSchemaProvider;
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
  public void shouldNotThrowExceptionOnValidationOfValidDevfile(String resourceFilePath)
      throws Exception {
    schemaValidator.validateYaml(getResource(resourceFilePath));
  }

  @DataProvider
  public Object[][] validDevfiles() {
    return new Object[][] {
      {"editor_plugin_component/devfile_editor_plugins.yaml"},
      {"editor_plugin_component/devfile_editor_component_with_custom_registry.yaml"},
      {"editor_plugin_component/devfile_editor_plugins_components_with_memory_limit.yaml"},
      {"editor_plugin_component/devfile_plugin_components_with_preferences.yaml"},
      {"kubernetes_openshift_component/devfile_kubernetes_component_reference.yaml"},
      {"kubernetes_openshift_component/devfile_kubernetes_component_absolute_reference.yaml"},
      {"component/devfile_without_any_component.yaml"},
      {
        "kubernetes_openshift_component/devfile_kubernetes_component_reference_and_content_as_block.yaml"
      },
      {"kubernetes_openshift_component/devfile_openshift_component.yaml"},
      {"kubernetes_openshift_component/devfile_openshift_component_reference_and_content.yaml"},
      {
        "kubernetes_openshift_component/devfile_openshift_component_reference_and_content_as_block.yaml"
      },
      {"kubernetes_openshift_component/devfile_openshift_component_content_without_reference.yaml"},
      {
        "kubernetes_openshift_component/devfile_kubernetes_component_content_without_reference.yaml"
      },
      {"dockerimage_component/devfile_dockerimage_component.yaml"},
      {"dockerimage_component/devfile_dockerimage_component_without_entry_point.yaml"},
      {"editor_plugin_component/devfile_editor_component_with_custom_registry.yaml"},
      {"editor_plugin_component/devfile_editor_plugins_components_with_memory_limit.yaml"},
      {"editor_plugin_component/devfile_plugin_component_with_reference.yaml"}
    };
  }

  @Test(dataProvider = "invalidDevfiles")
  public void shouldThrowExceptionOnValidationOfNonValidDevfile(
      String resourceFilePath, String expectedMessage) throws Exception {
    try {
      schemaValidator.validateYaml(getResource(resourceFilePath));
    } catch (DevfileFormatException e) {
      assertEquals(
          e.getMessage(),
          format("Devfile schema validation failed. Error: %s", expectedMessage),
          "DevfileFormatException thrown with message that doesn't match expected message:");
      return;
    }
    fail("DevfileFormatException expected to be thrown but is was not");
  }

  @DataProvider
  public Object[][] invalidDevfiles() {
    return new Object[][] {
      // Devfile model testing
      {
        "devfile/devfile_empty_metadata.yaml",
        "(/metadata):The object must have a property whose name is \"name\"."
      },
      {
        "devfile/devfile_null_metadata.yaml",
        "(/metadata):The value must be of object type, but actual type is null."
      },
      {
        "devfile/devfile_missing_metadata.yaml",
        "The object must have a property whose name is \"metadata\"."
      },
      {
        "devfile/devfile_missing_name.yaml",
        "(/metadata/something):The object must not have a property whose name is \"something\".(/metadata):The object must have a property whose name is \"name\"."
      },
      {
        "devfile/devfile_missing_api_version.yaml",
        "The object must have a property whose name is \"apiVersion\"."
      },
      {
        "devfile/devfile_with_undeclared_field.yaml",
        "(/unknown):The object must not have a property whose name is \"unknown\"."
      },
      // component model testing
      {
        "component/devfile_missing_component_type.yaml",
        "(/components/0):The object must have a property whose name is \"type\"."
      },
      {
        "component/devfile_unknown_component_type.yaml",
        "(/components/0/type):The value must be one of [\"cheEditor\", \"chePlugin\", \"kubernetes\", \"openshift\", \"dockerimage\"]."
      },
      {
        "component/devfile_component_with_undeclared_field.yaml",
        "(/components/0/unknown):The object must not have a property whose name is \"unknown\"."
      },
      // Command model testing
      {
        "command/devfile_missing_command_name.yaml",
        "(/commands/0):The object must have a property whose name is \"name\"."
      },
      {
        "command/devfile_missing_command_actions.yaml",
        "(/commands/0):The object must have a property whose name is \"actions\"."
      },
      {
        "command/devfile_multiple_commands_actions.yaml",
        "(/commands/0/actions):The array must have at most 1 element(s), but actual number is 2."
      },
      {
        "command/devfile_action_without_commandline_and_reference.yaml",
        "Exactly one of the following sets of problems must be resolved.: [(/commands/0/actions/0):The object must have a property whose name is \"component\".(/commands/0/actions/0):The object must have a property whose name is \"command\".At least one of the following sets of problems must be resolved.: [(/commands/0/actions/0):The object must have a property whose name is \"reference\".(/commands/0/actions/0):The object must have a property whose name is \"referenceContent\".]]"
      },
      // cheEditor/chePlugin component model testing
      {
        "editor_plugin_component/devfile_editor_component_with_missing_id.yaml",
        "Exactly one of the following sets of problems must be resolved.: [(/components/0):The object must have a property whose name is \"id\".(/components/0):The object must have a property whose name is \"reference\".]"
      },
      {
        "editor_plugin_component/devfile_editor_component_with_id_and_reference.yaml",
        "Exactly one of the following sets of problems must be resolved.: "
            + "[(/components/0):The object must not have a property whose name is \"reference\"."
            + "(/components/0):The object must not have a property whose name is \"id\".]"
      },
      {
        "editor_plugin_component/devfile_editor_component_with_indistinctive_field.yaml",
        "(/components/0/unknown):The object must not have a property whose name is \"unknown\"."
      },
      {
        "editor_plugin_component/devfile_editor_component_without_version.yaml",
        "(/components/0/id):The string value must match the pattern \"[a-z0-9_\\-.]+/[a-z0-9_\\-.]+/[a-z0-9_\\-.]+$\"."
      },
      {
        "editor_plugin_component/devfile_editor_plugins_components_with_invalid_memory_limit.yaml",
        "(/components/0/memoryLimit):The value must be of string type, but actual type is integer."
      },
      {
        "editor_plugin_component/devfile_editor_component_with_multiple_colons_in_id.yaml",
        "(/components/0/id):The string value must match the pattern \"[a-z0-9_\\-.]+/[a-z0-9_\\-.]+/[a-z0-9_\\-.]+$\"."
      },
      {
        "editor_plugin_component/devfile_editor_component_with_registry_in_id.yaml",
        "(/components/0/id):The string value must match the pattern \"[a-z0-9_\\-.]+/[a-z0-9_\\-.]+/[a-z0-9_\\-.]+$\"."
      },
      {
        "editor_plugin_component/devfile_editor_component_with_bad_registry.yaml",
        "(/components/0/registryUrl):The string value must match the pattern \"^(https?://)[a-zA-Z0-9_\\-./]+\"."
      },
      // kubernetes/openshift component model testing
      {
        "kubernetes_openshift_component/devfile_openshift_component_with_missing_reference_and_referenceContent.yaml",
        "At least one of the following sets of problems must be resolved.: [(/components/0):The object must have a property whose name is \"reference\".(/components/0):The object must have a property whose name is \"referenceContent\".]"
      },
      {
        "kubernetes_openshift_component/devfile_openshift_component_with_indistinctive_field_id.yaml",
        "(/components/0/id):The object must not have a property whose name is \"id\"."
      },
      // Dockerimage component model testing
      {
        "dockerimage_component/devfile_dockerimage_component_with_missing_image.yaml",
        "(/components/0):The object must have a property whose name is \"image\"."
      },
      {
        "dockerimage_component/devfile_dockerimage_component_with_missing_memory_limit.yaml",
        "(/components/0):The object must have a property whose name is \"memoryLimit\"."
      },
      {
        "dockerimage_component/devfile_dockerimage_component_with_indistinctive_field_selector.yaml",
        "(/components/0/selector):The object must not have a property whose name is \"selector\"."
      },
    };
  }

  private String getResource(String name) throws IOException {
    return Files.readFile(
        getClass().getClassLoader().getResourceAsStream("devfile/schema_test/" + name));
  }
}
