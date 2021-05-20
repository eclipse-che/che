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
package org.eclipse.che.api.workspace.server.devfile.validator;

import static java.lang.String.format;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import org.eclipse.che.api.workspace.server.devfile.Constants;
import org.eclipse.che.api.workspace.server.devfile.DevfileVersionDetector;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileFormatException;
import org.eclipse.che.api.workspace.server.devfile.schema.DevfileSchemaProvider;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.reporters.Files;

public class DevfileSchemaValidatorTest {

  private DevfileSchemaValidator schemaValidator;
  private ObjectMapper yamlMapper;

  @BeforeClass
  public void setUp() {
    yamlMapper = new ObjectMapper(new YAMLFactory());
    schemaValidator =
        new DevfileSchemaValidator(new DevfileSchemaProvider(), new DevfileVersionDetector());
  }

  @Test(dataProvider = "validDevfiles")
  public void shouldNotThrowExceptionOnValidationOfValidDevfile(String resourceFilePath)
      throws Exception {
    schemaValidator.validate(yamlMapper.readTree(getResource(resourceFilePath)));
  }

  @DataProvider
  public Object[][] validDevfiles() {
    return new Object[][] {
      {"editor_plugin_component/devfile_editor_plugins.yaml"},
      {"editor_plugin_component/devfile_editor_component_with_custom_registry.yaml"},
      {"editor_plugin_component/devfile_editor_plugins_components_with_resource_limits.yaml"},
      {"editor_plugin_component/devfile_plugin_components_with_preferences.yaml"},
      {"kubernetes_openshift_component/devfile_kubernetes_component.yaml"},
      {"kubernetes_openshift_component/devfile_kubernetes_component_absolute_reference.yaml"},
      {"component/devfile_without_any_component.yaml"},
      {"component/devfile_component_with_automount_secrets.yaml"},
      {
        "kubernetes_openshift_component/devfile_kubernetes_component_reference_and_content_as_block.yaml"
      },
      {"kubernetes_openshift_component/devfile_openshift_component.yaml"},
      {"kubernetes_openshift_component/devfile_openshift_component_reference_and_content.yaml"},
      {
        "kubernetes_openshift_component/devfile_openshift_component_reference_and_content_as_block.yaml"
      },
      {"kubernetes_openshift_component/devfile_k8s_openshift_component_with_env.yaml"},
      {"kubernetes_openshift_component/devfile_k8s_openshift_component_with_endpoints.yaml"},
      {"kubernetes_openshift_component/devfile_openshift_component_content_without_reference.yaml"},
      {
        "kubernetes_openshift_component/devfile_kubernetes_component_content_without_reference.yaml"
      },
      {"dockerimage_component/devfile_dockerimage_component.yaml"},
      {"dockerimage_component/devfile_dockerimage_component_without_entry_point.yaml"},
      {"editor_plugin_component/devfile_editor_component_with_custom_registry.yaml"},
      {"editor_plugin_component/devfile_plugin_component_with_reference.yaml"},
      {"devfile/devfile_just_generatename.yaml"},
      {"devfile/devfile_name_and_generatename.yaml"},
      {"devfile/devfile_with_sparse_checkout_dir.yaml"},
      {"devfile/devfile_name_and_generatename.yaml"},
      {"command/devfile_command_with_preview_url.yaml"},
      {"command/devfile_command_with_preview_url_only_port.yaml"},
      {"devfile/devfile_v2_just_schemaVersion.yaml"},
      {"devfile/devfile_v2_sample-devfile.yaml"},
      {"devfile/devfile_v2_simple-devfile.yaml"},
      {"devfile/devfile_v2_spring-boot-http-booster-devfile.yaml"},
      {"devfile/devfile_v2-1-0-alpha_just_schemaVersion.yaml"},
      {"devfile/devfile_v2-1-0-alpha_simple-devfile.yaml"},
    };
  }

  @Test(dataProvider = "invalidDevfiles")
  public void shouldThrowExceptionOnValidationOfNonValidDevfile(
      String resourceFilePath, String expectedMessage) throws Exception {
    try {
      schemaValidator.validate(yamlMapper.readTree(getResource(resourceFilePath)));
    } catch (DevfileFormatException e) {
      assertEquals(
          e.getMessage(),
          format("Devfile schema validation failed. Error: %s", expectedMessage),
          "DevfileFormatException thrown with message that doesn't match expected message:");
      return;
    }
    fail("DevfileFormatException expected to be thrown but is was not");
  }

  @Test
  public void shouldThrowExceptionWhenDevfileHasUnsupportedApiVersion() throws Exception {
    try {
      String devfile =
          "---\n" + "apiVersion: 111.111\n" + "metadata:\n" + "  name: test-invalid-apiversion\n";
      schemaValidator.validate(yamlMapper.readTree(devfile));
    } catch (DevfileFormatException e) {
      assertEquals(
          e.getMessage(),
          "Devfile schema validation failed. Error: Version '111.111' of the devfile is not supported. "
              + "Supported versions are '"
              + Constants.SUPPORTED_VERSIONS
              + "'.");
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
        "At least one of the following sets of problems must be resolved.: [(/metadata):The object must have a property whose name is \"name\".(/metadata):The object must have a property whose name is \"generateName\".]"
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
        "devfile/devfile_missing_name_and_generatename.yaml",
        "(/metadata/something):The object must not have a property whose name is \"something\".At least one of the following sets of problems must be resolved.: [(/metadata):The object must have a property whose name is \"name\".(/metadata):The object must have a property whose name is \"generateName\".]"
      },
      {
        "devfile/devfile_missing_api_version.yaml",
        "Neither of `apiVersion` or `schemaVersion` found. This is not a valid devfile."
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
        "(/components/0/id):The string value must match the pattern \"[a-z0-9_\\-.]+/[a-z0-9_\\-.]+/[a-z0-9_\\-.]+\\z\"."
      },
      {
        "editor_plugin_component/devfile_editor_plugins_components_with_invalid_memory_limit.yaml",
        "At least one of the following sets of problems must be resolved.: [(/components/0/memoryLimit):The value must be of string type, but actual type is integer.(/components/0/memoryLimit):The numeric value must be greater than 0.]At least one of the following sets of problems must be resolved.: [(/components/1/memoryLimit):The value must be of string type, but actual type is integer.(/components/1/memoryLimit):The numeric value must be greater than 0.]"
      },
      {
        "editor_plugin_component/devfile_editor_component_with_multiple_colons_in_id.yaml",
        "(/components/0/id):The string value must match the pattern \"[a-z0-9_\\-.]+/[a-z0-9_\\-.]+/[a-z0-9_\\-.]+\\z\"."
      },
      {
        "editor_plugin_component/devfile_editor_component_with_registry_in_id.yaml",
        "(/components/0/id):The string value must match the pattern \"[a-z0-9_\\-.]+/[a-z0-9_\\-.]+/[a-z0-9_\\-.]+\\z\"."
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
        "dockerimage_component/devfile_dockerimage_component_with_invalid_memory_limit.yaml",
        "At least one of the following sets of problems must be resolved.: [(/components/0/memoryLimit):The value must be of string type, but actual type is integer.(/components/0/memoryLimit):The numeric value must be greater than 0.]"
      },
      {
        "dockerimage_component/devfile_dockerimage_component_with_indistinctive_field_selector.yaml",
        "(/components/0/selector):The object must not have a property whose name is \"selector\"."
      },
      {
        "command/devfile_command_with_empty_preview_url.yaml",
        "(/commands/0/previewUrl):The value must be of object type, but actual type is null."
      },
      {
        "command/devfile_command_with_preview_url_port_is_string.yaml",
        "(/commands/0/previewUrl/port):The value must be of number type, but actual type is string."
      },
      {
        "command/devfile_command_with_preview_url_port_is_too_high.yaml",
        "(/commands/0/previewUrl/port):The numeric value must be less than or equal to 65535."
      },
      {
        "command/devfile_command_with_preview_url_port_is_negative.yaml",
        "(/commands/0/previewUrl/port):The numeric value must be greater than or equal to 0."
      },
      {
        "command/devfile_command_with_preview_url_only_path.yaml",
        "(/commands/0/previewUrl):The object must have a property whose name is \"port\"."
      },
      {
        "devfile/devfile_v2_invalid_schemaVersion.yaml",
        "Version 'a.b.c' of the devfile is not supported. Supported versions are '[1.0.0, 2.0.0, 2.1.0-alpha]'."
      },
      {
        "devfile/devfile_v2_unsupported_schemaVersion.yaml",
        "Version '22.33.44' of the devfile is not supported. Supported versions are '[1.0.0, 2.0.0, 2.1.0-alpha]'."
      },
      {
        "devfile/devfile_v2-1-0-alpha_unsupported_schemaVersion.yaml",
        "Version '2.1.0-beta' of the devfile is not supported. Supported versions are '[1.0.0, 2.0.0, 2.1.0-alpha]'."
      },
      {
        "devfile/devfile_v2-1-0-alpha_with_invalid_plugin_definition.yaml",
        "(/components/0/plugin):The object must not have a property whose name is \"plugin\".(/components/0):The object must have a property whose name is \"name\".Exactly one of the following sets of problems must be resolved.: [(/components/0):The object must have a property whose name is \"container\".(/components/0):The object must have a property whose name is \"kubernetes\".(/components/0):The object must have a property whose name is \"openshift\".(/components/0):The object must have a property whose name is \"volume\".]"
      }
    };
  }

  private String getResource(String name) throws IOException {
    return Files.readFile(
        getClass().getClassLoader().getResourceAsStream("devfile/schema_test/" + name));
  }
}
