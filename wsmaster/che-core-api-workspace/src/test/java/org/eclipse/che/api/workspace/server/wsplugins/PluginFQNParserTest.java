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
package org.eclipse.che.api.workspace.server.wsplugins;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertEqualsNoOrder;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.wsplugins.model.ExtendedPluginFQN;
import org.eclipse.che.api.workspace.server.wsplugins.model.PluginFQN;
import org.eclipse.che.api.workspace.shared.Constants;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class PluginFQNParserTest {

  private PluginFQNParser parser;

  @BeforeClass
  public void setUp() throws Exception {
    parser = new PluginFQNParser();
  }

  @Test
  public void shouldReturnEmptyListWhenNoPluginsOrEditors() throws Exception {
    Map<String, String> attributes = ImmutableMap.of("testProperty", "testValue");

    Collection<PluginFQN> result = parser.parsePlugins(attributes);

    assertTrue(
        result.isEmpty(),
        "PluginFQNParser should return empty list when attributes does not contain plugins or editors");
  }

  @Test(dataProvider = "validAttributesProvider")
  public void shouldParseAllPluginsAndEditor(AttributeParsingTestCase testCase) throws Exception {
    Collection<PluginFQN> actual = parser.parsePlugins(testCase.attributes);
    assertEqualsNoOrder(actual.toArray(), testCase.expectedPlugins.toArray());
  }

  @Test(dataProvider = "validPluginStringProvider")
  public void shouldParsePluginOrEditorToExtendedFQN(String plugin, ExtendedPluginFQN expected)
      throws Exception {
    ExtendedPluginFQN actual = parser.parsePluginFQN(plugin);
    assertEquals(actual, expected);
  }

  @Test(
      dataProvider = "invalidAttributeStringProvider",
      expectedExceptions = InfrastructureException.class)
  public void shouldThrowExceptionWhenPluginStringIsInvalid(String plugin) throws Exception {
    Map<String, String> attributes = createAttributes("", plugin);
    parser.parsePlugins(attributes);
  }

  @Test(
      dataProvider = "invalidAttributeStringProvider",
      expectedExceptions = InfrastructureException.class)
  public void shouldThrowExceptionWhenEditorStringIsInvalid(String editor) throws Exception {
    Map<String, String> attributes = createAttributes(editor, "");

    parser.parsePlugins(attributes);
  }

  @Test(
      expectedExceptions = InfrastructureException.class,
      expectedExceptionsMessageRegExp = "Multiple editors.*")
  public void shouldThrowExceptionWhenMultipleEditorsDefined() throws Exception {
    Map<String, String> attributes =
        createAttributes("publisher1/editor1/version1,publisher1/editor2/version1", "");

    parser.parsePlugins(attributes);
  }

  @Test(
      expectedExceptions = InfrastructureException.class,
      expectedExceptionsMessageRegExp =
          "Invalid Che tooling plugins configuration: plugin publisher1/testplugin/1.0 is duplicated")
  public void shouldThrowExceptionWhenDuplicatePluginDefined() throws Exception {
    Map<String, String> attributes =
        createAttributes(
            "",
            formatPlugin(null, "publisher1/testplugin/1.0"),
            formatPlugin(null, "publisher1/testplugin/1.0"));

    parser.parsePlugins(attributes);
  }

  @Test(
      expectedExceptions = InfrastructureException.class,
      expectedExceptionsMessageRegExp =
          "Invalid Che tooling plugins configuration: plugin publisher1/testplugin/1.0 is duplicated")
  public void shouldDetectDuplicatedPluginsIfTheyArePrefixedOrSuffixedWithEmptySpaces()
      throws Exception {
    Map<String, String> attributes =
        createAttributes(
            "",
            " " + formatPlugin(null, "publisher1/testplugin/1.0"),
            formatPlugin(null, "publisher1/testplugin/1.0") + " ");

    parser.parsePlugins(attributes);
  }

  @Test(
      expectedExceptions = InfrastructureException.class,
      expectedExceptionsMessageRegExp =
          "Invalid Che tooling plugins configuration: plugin publisher1/testplugin/latest is duplicated")
  public void shouldDetectDuplicatedPluginsIfOneLatestAndOtherNoVersion() throws Exception {
    Map<String, String> attributes =
        createAttributes(
            "",
            formatPlugin(null, "publisher1/testplugin"),
            formatPlugin(null, "publisher1/testplugin/latest"));

    parser.parsePlugins(attributes);
  }

  @DataProvider(name = "invalidAttributeStringProvider")
  public static Object[][] invalidAttributeStringProvider() {
    return new Object[][] {
      {formatPlugin("http://bad registry url", "testplugin/1.0")},
      {formatPlugin("http://testregistry:8080", "")}
    };
  }

  @DataProvider(name = "validAttributesProvider")
  public static Object[][] validAttributesProvider() {
    PluginFQN basicEditor = new PluginFQN(null, "publisher/editor/ver");
    PluginFQN basicEditorNoVersion = new PluginFQN(null, "publisher/editor");
    PluginFQN basicEditorLatestVersion = new PluginFQN(null, "publisher/editor/latest");
    PluginFQN basicPlugin = new PluginFQN(null, "publisher/plugin/ver");
    PluginFQN basicPluginNoVersion = new PluginFQN(null, "publisher/plugin");
    PluginFQN basicPluginLatestVersion = new PluginFQN(null, "publisher/plugin/latest");

    PluginFQN customRegistry =
        new PluginFQN(URI.create("http://registry.com/plugins/"), "pluginId");
    PluginFQN complexRegistry =
        new PluginFQN(
            URI.create("https://registry.com/plugins.v2/multiple.components_plugin/"),
            "id.version");

    return new AttributeParsingTestCase[][] {
      {
        new AttributeParsingTestCase(
            "Test basic plugin and editor",
            ImmutableList.of(basicEditor, basicPlugin),
            createAttributes(formatPlugin(basicEditor), formatPlugins(basicPlugin)))
      },
      {
        new AttributeParsingTestCase(
            "Test basic plugin with no version",
            ImmutableList.of(basicEditor, basicPluginLatestVersion),
            createAttributes(formatPlugin(basicEditor), formatPlugin(basicPluginNoVersion)))
      },
      {
        new AttributeParsingTestCase(
            "Test basic editor with no version",
            ImmutableList.of(basicEditorLatestVersion, basicPlugin),
            createAttributes(formatPlugin(basicEditorNoVersion), formatPlugin(basicPlugin)))
      },
      {
        new AttributeParsingTestCase( // TODO
            "Test basic plugin with version 'latest'",
            ImmutableList.of(basicEditor, basicPluginLatestVersion),
            createAttributes(formatPlugin(basicEditor), formatPlugin(basicPluginLatestVersion)))
      },
      {
        new AttributeParsingTestCase(
            "Test attributes with no editor field",
            ImmutableList.of(basicPlugin),
            createAttributes(null, formatPlugins(basicPlugin)))
      },
      {
        new AttributeParsingTestCase(
            "Test attributes with empty editor field",
            ImmutableList.of(basicPlugin),
            createAttributes("", formatPlugins(basicPlugin)))
      },
      {
        new AttributeParsingTestCase(
            "Test attributes with no plugin field",
            ImmutableList.of(basicEditor),
            createAttributes(formatPlugin(basicEditor), (String[]) null))
      },
      {
        new AttributeParsingTestCase(
            "Test attributes with empty plugin field",
            ImmutableList.of(basicEditor),
            createAttributes(formatPlugin(basicEditor), ""))
      },
      {
        new AttributeParsingTestCase(
            "Test attributes with no plugin or editor field",
            Collections.emptyList(),
            createAttributes(null, (String[]) null))
      },
      {
        new AttributeParsingTestCase(
            "Test attributes with empty plugin and editor field",
            Collections.emptyList(),
            createAttributes("", ""))
      },
      {
        new AttributeParsingTestCase(
            "Test multiple plugins and an editor",
            ImmutableList.of(basicEditor, basicPlugin, basicPluginLatestVersion),
            createAttributes(
                formatPlugin(basicEditor), formatPlugins(basicPlugin, basicPluginLatestVersion)))
      },
      {
        new AttributeParsingTestCase(
            "Test multiple plugins and an editor, no version specified",
            ImmutableList.of(basicEditorLatestVersion, basicPlugin, basicPluginLatestVersion),
            createAttributes(
                formatPlugin(basicEditorNoVersion),
                formatPlugins(basicPlugin, basicPluginLatestVersion)))
      },
      {
        new AttributeParsingTestCase(
            "Test plugin with custom registry",
            ImmutableList.of(basicEditor, customRegistry),
            createAttributes(formatPlugin(basicEditor), formatPlugin(customRegistry)))
      },
      {
        new AttributeParsingTestCase(
            "Test plugin with complex registry",
            ImmutableList.of(basicEditor, complexRegistry),
            createAttributes(formatPlugin(basicEditor), formatPlugin(complexRegistry)))
      },
    };
  }

  // Objects are
  //   (String plugin, ExtendedPluginFQN expectedPlugin)
  @DataProvider(name = "validPluginStringProvider")
  public static Object[][] validPluginStringProvider() {
    return new Object[][] {
      {
        "http://registry:8080/publisher/editor/ver",
        new ExtendedPluginFQN(
            URI.create("http://registry:8080/publisher/editor/"), "ver", null, null, null)
      },
      {
        "https://registry:8080/publisher/editor/ver",
        new ExtendedPluginFQN(
            URI.create("https://registry:8080/publisher/editor/"), "ver", null, null, null)
      },
      {
        "https://che-registry.com.ua/publisher/editor/ver",
        new ExtendedPluginFQN(
            URI.create("https://che-registry.com.ua/publisher/editor/"), "ver", null, null, null)
      },
      {
        "https://che-registry.com.ua/plugins/publisher/editor/ver",
        new ExtendedPluginFQN(
            URI.create("https://che-registry.com.ua/plugins/publisher/editor/"),
            "ver",
            null,
            null,
            null)
      },
      {
        "https://che-registry.com.ua/some/long/path/publisher/editor/ver",
        new ExtendedPluginFQN(
            URI.create("https://che-registry.com.ua/some/long/path/publisher/editor/"),
            "ver",
            null,
            null,
            null)
      },
      {
        "publisher/editor/ver",
        new ExtendedPluginFQN(null, "publisher/editor/ver", "publisher", "editor", "ver")
      },
      {
        "publisher/editor/1.12.1",
        new ExtendedPluginFQN(null, "publisher/editor/1.12.1", "publisher", "editor", "1.12.1")
      },
      {
        "publisher/editor/v2.12.x",
        new ExtendedPluginFQN(null, "publisher/editor/v2.12.x", "publisher", "editor", "v2.12.x")
      },
      {
        "publisher/che-theia/next",
        new ExtendedPluginFQN(null, "publisher/che-theia/next", "publisher", "che-theia", "next")
      },
      {
        "publisher/che-theia/2.12-latest",
        new ExtendedPluginFQN(
            null, "publisher/che-theia/2.12-latest", "publisher", "che-theia", "2.12-latest")
      },
      {
        "publisher/che-theia",
        new ExtendedPluginFQN(
            null, "publisher/che-theia/latest", "publisher", "che-theia", "latest")
      },
    };
  }

  private static String[] formatPlugins(PluginFQN... plugins) {
    return Arrays.stream(plugins).map(PluginFQNParserTest::formatPlugin).toArray(String[]::new);
  }

  private static String formatPlugin(PluginFQN plugin) {
    String registry = plugin.getRegistry() == null ? null : plugin.getRegistry().toString();
    return formatPlugin(registry, plugin.getId());
  }

  private static String formatPlugin(String registry, String id) {
    if (registry == null) {
      return id;
    } else {
      return registry + id;
    }
  }

  private static String formatPluginWithLatest(PluginFQN plugin) {
    return plugin.getId() + "/latest";
  }

  private static Map<String, String> createAttributes(String editor, String... plugins) {
    Map<String, String> attributes = new HashMap<>();
    if (plugins != null) {
      String allPlugins = String.join(",", plugins);
      attributes.put(Constants.WORKSPACE_TOOLING_PLUGINS_ATTRIBUTE, allPlugins);
    }
    if (editor != null) {
      attributes.put(Constants.WORKSPACE_TOOLING_EDITOR_ATTRIBUTE, editor);
    }
    return ImmutableMap.copyOf(attributes);
  }

  /**
   * Holder of data for a test case. Syntax sugar that primary goal is to allow IDE log test case
   * description (in one of the fields) and compare expected and actual arrays in a way that prints
   * diff when they are not equal
   */
  private static class AttributeParsingTestCase {

    private String description;
    private List<PluginFQN> expectedPlugins;
    private Map<String, String> attributes;

    public AttributeParsingTestCase(
        String description, List<PluginFQN> expectedPlugins, Map<String, String> attributes) {
      this.description = description;
      this.expectedPlugins = expectedPlugins;
      this.attributes = attributes;
    }

    @Override
    public String toString() {
      return description;
    }
  }
}
