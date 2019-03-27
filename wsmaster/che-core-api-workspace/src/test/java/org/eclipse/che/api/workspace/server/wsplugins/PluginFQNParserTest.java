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
import org.eclipse.che.api.workspace.server.wsplugins.model.PluginFQN;
import org.eclipse.che.api.workspace.shared.Constants;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class PluginFQNParserTest {

  private static final String PLUGIN_FORMAT = "%s/%s:%s";
  private static final String PLUGIN_FORMAT_WITHOUT_REGISTRY = "%s:%s";

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
  public void shouldParseAllPluginsAndEditor(
      String desc, List<PluginFQN> expected, Map<String, String> attributes) throws Exception {
    Collection<PluginFQN> actual = parser.parsePlugins(attributes);
    assertEqualsNoOrder(actual.toArray(), expected.toArray(), desc);
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
    Map<String, String> attributes = createAttributes("editor1,editor2", "");

    parser.parsePlugins(attributes);
  }

  @Test(
      expectedExceptions = InfrastructureException.class,
      expectedExceptionsMessageRegExp =
          "Invalid Che tooling plugins configuration: plugin .*:.* is duplicated")
  public void shouldThrowExceptionWhenDuplicatePluginDefined() throws Exception {
    Map<String, String> attributes =
        createAttributes(
            "",
            formatPlugin("http://testregistry1:8080", "testplugin", "1.0"),
            formatPlugin("http://testregistry2:8080", "testplugin", "1.0"));

    parser.parsePlugins(attributes);
  }

  @Test(
      expectedExceptions = InfrastructureException.class,
      expectedExceptionsMessageRegExp =
          "Invalid Che tooling plugins configuration: plugin .*:.* is duplicated")
  public void shouldDetectDuplicatedPluginsIfTheyArePrefixedSuffixedWithEmptySpaces()
      throws Exception {
    Map<String, String> attributes =
        createAttributes(
            "",
            " " + formatPlugin("http://testregistry1:8080", "testplugin", "1.0"),
            formatPlugin("http://testregistry2:8080", "testplugin", "1.0") + " ");

    parser.parsePlugins(attributes);
  }

  @DataProvider(name = "invalidAttributeStringProvider")
  public static Object[][] invalidAttributeStringProvider() {
    return new Object[][] {
      {formatPlugin("http://bad registry url", "testplugin", "1.0")},
      {formatPlugin("http://testregistry:8080", "bad:pluginname", "1.0")},
      {formatPlugin("http://testregistry:8080", "", "emptyID")},
      {formatPlugin("http://testregistry:8080", "emptyVersion", "")}
    };
  }

  // Objects are
  //   (String description, List<PluginFQN> expectedPlugins, Map<String, String> attributes)
  @DataProvider(name = "validAttributesProvider")
  public static Object[][] validAttributesProvider() {
    PluginFQN basicEditor = new PluginFQN(URI.create("http://registry:8080"), "editor", "ver");
    PluginFQN withRegistry = new PluginFQN(URI.create("http://registry:8080"), "plugin", "1.0");
    PluginFQN noRegistry = new PluginFQN(null, "pluginNoRegistry", "2.0");
    PluginFQN pathRegistry =
        new PluginFQN(URI.create("http://registry/multiple/path/"), "pluginPathRegistry", "3.0");
    return new Object[][] {
      {
        "Test plugin with registry",
        ImmutableList.of(basicEditor, withRegistry),
        createAttributes(formatPlugin(basicEditor), formatPlugins(withRegistry))
      },
      {
        "Test plugin without registry",
        ImmutableList.of(basicEditor, noRegistry),
        createAttributes(formatPlugin(basicEditor), formatPlugins(noRegistry))
      },
      {
        "Test plugin with multi-level path in registry",
        ImmutableList.of(basicEditor, pathRegistry),
        createAttributes(formatPlugin(basicEditor), formatPlugins(pathRegistry))
      },
      {
        "Test attributes with no editor field",
        ImmutableList.of(withRegistry),
        createAttributes(null, formatPlugins(withRegistry))
      },
      {
        "Test attributes with empty editor field",
        ImmutableList.of(withRegistry),
        createAttributes("", formatPlugins(withRegistry))
      },
      {
        "Test attributes with no plugin field",
        ImmutableList.of(basicEditor),
        createAttributes(formatPlugin(basicEditor), (String[]) null)
      },
      {
        "Test attributes with empty plugin field",
        ImmutableList.of(basicEditor),
        createAttributes(formatPlugin(basicEditor), "")
      },
      {
        "Test attributes with no plugin or editor field",
        Collections.emptyList(),
        createAttributes(null, (String[]) null)
      },
      {
        "Test attributes with empty plugin and editor field",
        Collections.emptyList(),
        createAttributes("", "")
      },
      {
        "Test multiple plugins and an editor",
        ImmutableList.of(basicEditor, withRegistry, noRegistry, pathRegistry),
        createAttributes(
            formatPlugin(basicEditor), formatPlugins(withRegistry, noRegistry, pathRegistry))
      },
    };
  }

  private static String[] formatPlugins(PluginFQN... plugins) {
    return Arrays.stream(plugins).map(p -> formatPlugin(p)).toArray(String[]::new);
  }

  private static String formatPlugin(PluginFQN plugin) {
    String registry = plugin.getRegistry() == null ? null : plugin.getRegistry().toString();
    return formatPlugin(registry, plugin.getId(), plugin.getVersion());
  }

  private static String formatPlugin(String registry, String id, String version) {
    if (registry == null) {
      return String.format(PLUGIN_FORMAT_WITHOUT_REGISTRY, id, version);
    } else {
      return String.format(PLUGIN_FORMAT, registry, id, version);
    }
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
}
