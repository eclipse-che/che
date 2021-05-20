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
package org.eclipse.che.api.workspace.server.wsplugins;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
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
import org.eclipse.che.api.workspace.server.devfile.FileContentProvider;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.wsplugins.model.ExtendedPluginFQN;
import org.eclipse.che.api.workspace.server.wsplugins.model.PluginFQN;
import org.eclipse.che.api.workspace.shared.Constants;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class PluginFQNParserTest {

  @Mock private FileContentProvider fileContentProvider;

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

  @Test
  public void shouldComposeIdWhenAllPartsGivenToTheConstructor() {
    ExtendedPluginFQN pluginFQN =
        new ExtendedPluginFQN("reference", "publisher", "name", "version");
    assertEquals(pluginFQN.getId(), "publisher/name/version");
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

  @Test(dataProvider = "validPluginReferencesProvider")
  public void shouldParsePluginOrEditorFromReference(
      String reference, String pluginYaml, ExtendedPluginFQN expected) throws Exception {
    when(fileContentProvider.fetchContent(eq(reference))).thenReturn(pluginYaml);
    ExtendedPluginFQN actual = parser.evaluateFqn(reference, fileContentProvider);
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
            formatPlugin("http://testregistry1:8080", "publisher1/testplugin/1.0"),
            formatPlugin("http://testregistry2:8080", "publisher1/testplugin/1.0"));

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
            " " + formatPlugin("http://testregistry1:8080", "publisher1/testplugin/1.0"),
            formatPlugin("http://testregistry2:8080", "publisher1/testplugin/1.0") + " ");

    parser.parsePlugins(attributes);
  }

  @DataProvider(name = "invalidAttributeStringProvider")
  public static Object[][] invalidAttributeStringProvider() {
    return new Object[][] {
      {formatPlugin("http://bad registry url", "testplugin/1.0")},
      {formatPlugin("http://testregistry:8080", "bad:pluginname/1.0")},
      {formatPlugin("http://testregistry:8080", "/version")},
      {formatPlugin("http://testregistry:8080", "id/")},
      {formatPlugin("http://testregistry:8080", "name/version")},
      {formatPlugin("http://testregistry:8080", "id:version")},
      {formatPlugin("http://testregistry:8080", "publisher/name:version")},
    };
  }

  @DataProvider(name = "validAttributesProvider")
  public static Object[][] validAttributesProvider() {
    PluginFQN basicEditor =
        new PluginFQN(URI.create("http://registry:8080"), "publisher/editor/ver");
    PluginFQN withRegistry =
        new PluginFQN(URI.create("http://registry:8080"), "publisher/plugin/1.0");
    PluginFQN noRegistry = new PluginFQN(null, "publisher/pluginnoregistry/2.0");
    PluginFQN pathRegistry =
        new PluginFQN(
            URI.create("http://registry/multiple/path"), "publisher/pluginpathregistry/3.0");
    PluginFQN reference = new PluginFQN("http://mysite:8080/multiple/path/meta.yaml");
    return new AttributeParsingTestCase[][] {
      {
        new AttributeParsingTestCase(
            "Test plugin with registry",
            ImmutableList.of(basicEditor, withRegistry),
            createAttributes(formatPlugin(basicEditor), formatPlugins(withRegistry)))
      },
      {
        new AttributeParsingTestCase(
            "Test plugin with https registry",
            ImmutableList.of(
                new PluginFQN(URI.create("https://registry:8080"), "publisher/editor/ver")),
            createAttributes(
                null,
                formatPlugin(
                    new PluginFQN(URI.create("https://registry:8080"), "publisher/editor/ver"))))
      },
      {
        new AttributeParsingTestCase(
            "Test plugin with registry containing path",
            ImmutableList.of(
                new PluginFQN(
                    URI.create("https://registry:8080/some/path/v3"), "publisher/editor/ver")),
            createAttributes(
                null,
                formatPlugin(
                    new PluginFQN(
                        URI.create("https://registry:8080/some/path/v3/"),
                        "publisher/editor/ver"))))
      },
      {
        new AttributeParsingTestCase(
            "Test plugin without registry",
            ImmutableList.of(basicEditor, noRegistry),
            createAttributes(formatPlugin(basicEditor), formatPlugins(noRegistry)))
      },
      {
        new AttributeParsingTestCase(
            "Test plugin with multi-level path in registry",
            ImmutableList.of(basicEditor, pathRegistry),
            createAttributes(formatPlugin(basicEditor), formatPlugins(pathRegistry)))
      },
      {
        new AttributeParsingTestCase(
            "Test plugin described by reference",
            ImmutableList.of(basicEditor, reference),
            createAttributes(
                formatPlugin(basicEditor), "http://mysite:8080/multiple/path/meta.yaml"))
      },
      {
        new AttributeParsingTestCase(
            "Test attributes with no editor field",
            ImmutableList.of(withRegistry),
            createAttributes(null, formatPlugins(withRegistry)))
      },
      {
        new AttributeParsingTestCase(
            "Test attributes with empty editor field",
            ImmutableList.of(withRegistry),
            createAttributes("", formatPlugins(withRegistry)))
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
            ImmutableList.of(basicEditor, withRegistry, noRegistry, pathRegistry),
            createAttributes(
                formatPlugin(basicEditor), formatPlugins(withRegistry, noRegistry, pathRegistry)))
      },
    };
  }

  // Objects are
  //   (String plugin, ExtendedPluginFQN expectedPlugin)
  @DataProvider(name = "validPluginStringProvider")
  public static Object[][] validPluginStringProvider() {
    return new Object[][] {
      {
        "http://registry:8080#publisher/editor/ver",
        new ExtendedPluginFQN(
            URI.create("http://registry:8080"),
            "publisher/editor/ver",
            "publisher",
            "editor",
            "ver")
      },
      {
        "https://registry:8080#publisher/editor/ver",
        new ExtendedPluginFQN(
            URI.create("https://registry:8080"),
            "publisher/editor/ver",
            "publisher",
            "editor",
            "ver")
      },
      {
        "https://che-registry.com.ua#publisher/editor/ver",
        new ExtendedPluginFQN(
            URI.create("https://che-registry.com.ua"),
            "publisher/editor/ver",
            "publisher",
            "editor",
            "ver")
      },
      {
        "https://che-registry.com.ua/plugins#publisher/editor/ver",
        new ExtendedPluginFQN(
            URI.create("https://che-registry.com.ua/plugins"),
            "publisher/editor/ver",
            "publisher",
            "editor",
            "ver")
      },
      {
        "https://che-registry.com.ua/plugins/v3/#publisher/editor/ver",
        new ExtendedPluginFQN(
            URI.create("https://che-registry.com.ua/plugins/v3"),
            "publisher/editor/ver",
            "publisher",
            "editor",
            "ver")
      },
      {
        "https://che-registry.com.ua/some/long/path#publisher/editor/ver",
        new ExtendedPluginFQN(
            URI.create("https://che-registry.com.ua/some/long/path"),
            "publisher/editor/ver",
            "publisher",
            "editor",
            "ver")
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
    };
  }

  // Objects are
  //   (String reference, String yamlContent, ExtendedPluginFQN expectedPlugin)
  @DataProvider(name = "validPluginReferencesProvider")
  public static Object[][] validPluginReferencesProvider() {
    return new Object[][] {
      {
        "http://registry:8080/publisher/editor/ver/meta.yaml",
        "apiVersion: v2\n"
            + "publisher: publisher\n"
            + "name: editor\n"
            + "version: ver\n"
            + "type: Che Editor",
        new ExtendedPluginFQN(
            "http://registry:8080/publisher/editor/ver/meta.yaml", "publisher", "editor", "ver")
      },
      {
        "https://pastebin.com/1ij3475rh",
        "apiVersion: v2\n"
            + "publisher: publisher\n"
            + "name: editor\n"
            + "version: 0.0.5\n"
            + "type: Che Editor",
        new ExtendedPluginFQN("https://pastebin.com/1ij3475rh", "publisher", "editor", "0.0.5")
      },
      {
        "https://che-registry.com.ua/publisher/plugin/0.0.5/meta.yaml",
        "apiVersion: v2\n"
            + "publisher: publisher\n"
            + "name: plugin123\n"
            + "version: 0.0.5\n"
            + "type: Che Plugin",
        new ExtendedPluginFQN(
            "https://che-registry.com.ua/publisher/plugin/0.0.5/meta.yaml",
            "publisher",
            "plugin123",
            "0.0.5")
      },
      {
        "https://pastebin.com/raw/EBmz0YMS",
        "apiVersion: v2\n"
            + "publisher: publisher\n"
            + "name: numericVersion\n"
            + "version: 1.1\n"
            + "type: Che Plugin",
        new ExtendedPluginFQN(
            "https://pastebin.com/raw/EBmz0YMS", "publisher", "numericVersion", "1.1")
      }
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
      return registry + "#" + id;
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
