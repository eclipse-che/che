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
package org.eclipse.che.ide.maven.tools;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Collections.emptyList;
import static org.eclipse.che.commons.xml.NewElement.createElement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.commons.xml.Element;
import org.eclipse.che.commons.xml.ElementMapper;
import org.eclipse.che.commons.xml.NewElement;

/**
 * The {@literal <build>} element contains project build settings.
 *
 * <p>Supported next data:
 *
 * <ul>
 *   <li>sourceDirectory
 *   <li>testSourceDirectory
 *   <li>scriptSourceDirectory
 *   <li>outputDirectory
 *   <li>testOutputDirectory
 *   <li>resources
 * </ul>
 *
 * @author Eugene Voevodin
 */
public class Build extends BuildBase {

  private static final ElementMapper<Resource> RESOURCE_MAPPER = new ResourceMapper();
  private static final ElementMapper<Plugin> PLUGIN_MAPPER = new PluginMapper();

  private String sourceDirectory;
  private String testSourceDirectory;
  private String scriptSourceDirectory;
  private String outputDirectory;
  private String testOutputDirectory;
  private List<Resource> resources;
  private List<Plugin> plugins;

  Element buildElement;

  public Build() {
    super();
  }

  Build(Element buildElement) {
    super(buildElement);
    this.buildElement = buildElement;
    sourceDirectory = buildElement.getChildText("sourceDirectory");
    testSourceDirectory = buildElement.getChildText("testSourceDirectory");
    scriptSourceDirectory = buildElement.getChildText("scriptSourceDirectory");
    outputDirectory = buildElement.getChildText("outputDirectory");
    testOutputDirectory = buildElement.getChildText("testOutputDirectory");
    if (buildElement.hasSingleChild("resources")) {
      resources = buildElement.getSingleChild("resources").getChildren(RESOURCE_MAPPER);
    }
    if (buildElement.hasSingleChild("plugins")) {
      plugins = buildElement.getSingleChild("plugins").getChildren(PLUGIN_MAPPER);
    }
  }

  /** Returns path to directory where compiled application classes are placed. */
  public String getOutputDirectory() {
    return outputDirectory;
  }

  /**
   * Returns path to directory containing the script sources of the project.
   *
   * <p>This directory is meant to be different from the sourceDirectory, in that its contents will
   * be copied to the output directory in most cases (since scripts are interpreted rather than
   * compiled).
   */
  public String getScriptSourceDirectory() {
    return scriptSourceDirectory;
  }

  /**
   * Returns path to directory containing the source of the project.
   *
   * <p>The generated build system will compile the source in this directory when the project is
   * built. The path given is relative to the project descriptor.
   */
  public String getSourceDirectory() {
    return sourceDirectory;
  }

  /** Returns path to directory where compiled test classes are placed. */
  public String getTestOutputDirectory() {
    return testOutputDirectory;
  }

  /**
   * Returns path to directory containing the unit test source of the project.
   *
   * <p>The generated build system will compile these directories when the project is being tested.
   * The path given is relative to the project descriptor.
   */
  public String getTestSourceDirectory() {
    return testSourceDirectory;
  }

  /** Sets the path to directory where compiled application classes are placed. */
  public Build setOutputDirectory(String outputDirectory) {
    this.outputDirectory = outputDirectory;
    if (!isNew()) {
      if (outputDirectory == null) {
        buildElement.removeChild("outputDirectory");
      } else if (buildElement.hasSingleChild("outputDirectory")) {
        buildElement.getSingleChild("outputDirectory").setText(outputDirectory);
      } else {
        buildElement.appendChild(createElement("outputDirectory", outputDirectory));
      }
    }
    return this;
  }

  /**
   * Sets the path to directory containing the script sources of the project
   *
   * <p>If {@code scriptSourceDirectory} is {@code null} and this build instance is associated with
   * xml element then {@code scriptSourceDirectory} will be removed from model as well as from xml.
   *
   * @param scriptSourceDirectory new build script source directory
   * @return this build instance
   */
  public Build setScriptSourceDirectory(String scriptSourceDirectory) {
    this.scriptSourceDirectory = scriptSourceDirectory;
    if (!isNew()) {
      if (scriptSourceDirectory == null) {
        buildElement.removeChild("scriptSourceDirectory");
      } else if (buildElement.hasSingleChild("scriptSourceDirectory")) {
        buildElement.getSingleChild("scriptSourceDirectory").setText(scriptSourceDirectory);
      } else {
        buildElement.appendChild(createElement("scriptSourceDirectory", scriptSourceDirectory));
      }
    }
    return this;
  }

  /**
   * Sets the path to directory containing the source of the project.
   *
   * <p>If {@code sourceDirectory} is {@code null} and this build instance is associated with xml
   * element then {@code sourceDirectory} will be removed from model as well as from xml.
   *
   * @param sourceDirectory new build source directory
   */
  public Build setSourceDirectory(String sourceDirectory) {
    this.sourceDirectory = sourceDirectory;
    if (!isNew()) {
      if (sourceDirectory == null) {
        buildElement.removeChild("sourceDirectory");
      } else if (buildElement.hasSingleChild("sourceDirectory")) {
        buildElement.getSingleChild("sourceDirectory").setText(sourceDirectory);
      } else {
        buildElement.appendChild(createElement("sourceDirectory", sourceDirectory));
      }
    }
    return this;
  }

  /**
   * Sets the path to directory where compiled test classes are placed.
   *
   * <p>If {@code testOutputDirectory} is {@code null} and this build instance is associated with
   * xml element then {@code testOutputDirectory} will be removed from model as well as from xml.
   *
   * @param testOutputDirectory new build test output directory
   * @return this build instance
   */
  public Build setTestOutputDirectory(String testOutputDirectory) {
    this.testOutputDirectory = testOutputDirectory;
    if (!isNew()) {
      if (testOutputDirectory == null) {
        buildElement.removeChild("testOutputDirectory");
      } else if (buildElement.hasSingleChild("testOutputDirectory")) {
        buildElement.getSingleChild("testOutputDirectory").setText(testOutputDirectory);
      } else {
        buildElement.appendChild(createElement("testOutputDirectory", testOutputDirectory));
      }
    }
    return this;
  }

  /**
   * Sets the path to directory containing the unit test source of the project.
   *
   * <p>If {@code testSourceDirectory} is {@code null} and this build instance is associated with
   * xml element then {@code testSourceDirectory} will be removed from model as well as from xml.
   *
   * @param testSourceDirectory new build test source directory
   * @return this build instance
   */
  public Build setTestSourceDirectory(String testSourceDirectory) {
    this.testSourceDirectory = testSourceDirectory;
    if (!isNew()) {
      if (testSourceDirectory == null) {
        buildElement.removeChild("testSourceDirectory");
      } else if (buildElement.hasSingleChild("testSourceDirectory")) {
        buildElement.getSingleChild("testSourceDirectory").setText(testSourceDirectory);
      } else {
        buildElement.appendChild(createElement("testSourceDirectory", testSourceDirectory));
      }
    }
    return this;
  }

  /**
   * Returns list of resource elements which contains information about where associated with
   * project files should be included
   *
   * <p><b>Note: update methods should not be used on returned list</b>
   */
  public List<Resource> getResources() {
    if (resources == null) {
      return emptyList();
    }
    return new ArrayList<>(resources);
  }

  /**
   * Sets build resources, each resource contains information about where associated with project
   * files should be included.
   *
   * <p>If {@code resources} is {@code null} or <i>empty</i> and this build instance is associated
   * with xml element then {@code resources} will be removed from model as well as from xml.
   *
   * @param resources new build resources
   * @return this build instance
   */
  public Build setResources(Collection<? extends Resource> resources) {
    if (resources == null || resources.isEmpty()) {
      removeResources();
    } else {
      setResources0(resources);
    }
    return this;
  }

  /**
   * Returns build plugins.
   *
   * <p><b>Note: update methods should not be used on returned list</b>
   *
   * @return build plugins or empty map when build doesn't have plugins
   */
  public List<Plugin> getPlugins() {
    if (plugins == null) {
      return emptyList();
    }
    return new ArrayList<>(plugins);
  }

  /**
   * Returns build plugins mapped as {@code plugin.getId() -> plugin}
   *
   * @return mapped plugins or empty map if build doesn't have plugins
   * @see Plugin#getId()
   */
  public Map<String, Plugin> getPluginsAsMap() {
    final Map<String, Plugin> pluginsMap = new HashMap<>();
    for (Plugin plugin : plugins()) {
      pluginsMap.put(plugin.getId(), plugin);
    }
    return pluginsMap;
  }

  /**
   * Sets build plugins.
   *
   * @param plugins new build plugins, if {@code plugins} parameter is {@code null} and build
   *     associated with xml element then plugins will be removed from xml as well as from model
   * @return this build instance
   */
  public Build setPlugins(Collection<? extends Plugin> plugins) {
    if (plugins == null || plugins.isEmpty()) {
      removePlugins();
    } else {
      setPlugins0(plugins);
    }
    return this;
  }

  private void removePlugins() {
    if (!isNew()) {
      buildElement.removeChild("plugins");
    }
    plugins = null;
  }

  private void setPlugins0(Collection<? extends Plugin> plugins) {
    this.plugins = new ArrayList<>(plugins);

    if (isNew()) return;
    // if plugins element exists we should replace it children
    // with new set of plugins, otherwise create element for it
    if (buildElement.hasSingleChild("plugins")) {
      // remove "plugins" element children
      final Element pluginsElement = buildElement.getSingleChild("plugins");
      for (Element plugin : pluginsElement.getChildren()) {
        plugin.remove();
      }
      // append each new plugin to "plugins" element
      for (Plugin plugin : plugins) {
        pluginsElement.appendChild(plugin.asXMLElement());
      }
    } else {
      buildElement.appendChild(newPluginsElement(plugins));
    }
  }

  private List<Plugin> plugins() {
    return plugins == null ? plugins = new ArrayList<>() : plugins;
  }

  public NewElement asXMLElement() {
    final NewElement xmlBuildElement = createElement("build");
    if (!isNullOrEmpty(getDefaultGoal())) {
      xmlBuildElement.appendChild(createElement("defaultGoal", getDefaultGoal()));
    }
    if (!isNullOrEmpty(getFinalName())) {
      xmlBuildElement.appendChild(createElement("finalName", getFinalName()));
    }
    if (resources != null && !resources.isEmpty()) {
      xmlBuildElement.appendChild(newResourcesElement(resources));
    }
    final List<Resource> testResources = getTestResources();
    if (testResources != null && !testResources.isEmpty()) {
      xmlBuildElement.appendChild(newTestResourcesElement(testResources));
    }
    if (sourceDirectory != null) {
      xmlBuildElement.appendChild(createElement("sourceDirectory", sourceDirectory));
    }
    if (testSourceDirectory != null) {
      xmlBuildElement.appendChild(createElement("testSourceDirectory", testSourceDirectory));
    }
    if (scriptSourceDirectory != null) {
      xmlBuildElement.appendChild(createElement("scriptSourceDirectory", scriptSourceDirectory));
    }
    if (outputDirectory != null) {
      xmlBuildElement.appendChild(createElement("outputDirectory", outputDirectory));
    }
    if (testOutputDirectory != null) {
      xmlBuildElement.appendChild(createElement("testOutputDirectory", testOutputDirectory));
    }
    if (resources != null && !resources.isEmpty()) {
      xmlBuildElement.appendChild(newResourcesElement(resources));
    }
    if (plugins != null && !plugins.isEmpty()) {
      xmlBuildElement.appendChild(newPluginsElement(plugins));
    }
    return xmlBuildElement;
  }

  private boolean isNew() {
    return buildElement == null;
  }

  private NewElement newPluginsElement(Collection<? extends Plugin> plugins) {
    final NewElement xmlPlugins = createElement("plugins");
    for (Plugin plugin : plugins) {
      xmlPlugins.appendChild(plugin.asXMLElement());
    }
    return xmlPlugins;
  }

  private NewElement newResourcesElement(List<Resource> resources) {
    final NewElement resourcesElement = createElement("resources");
    for (Resource resource : resources) {
      resourcesElement.appendChild(resource.asXMLElement());
    }
    return resourcesElement;
  }

  private void setResources0(Collection<? extends Resource> resources) {
    this.resources = new ArrayList<>(resources);

    if (isNew()) return;
    // if resources element exists we should replace it children
    // with new set of resources, otherwise create element for it
    if (buildElement.hasSingleChild("resources")) {
      // remove "resources" element children
      final Element resourcesElement = buildElement.getSingleChild("resources");
      for (Element resource : resourcesElement.getChildren()) {
        resource.remove();
      }
      // append each new resource to "resources" element
      for (Resource resource : resources) {
        resourcesElement.appendChild(resource.asXMLElement());
        resource.resourceElement = resourcesElement.getLastChild();
      }
    } else {
      buildElement.appendChild(newResourcesElement(this.resources));
    }
  }

  private void removeResources() {
    if (!isNew()) {
      buildElement.removeChild("resources");
    }
    this.resources = null;
  }

  private static class ResourceMapper implements ElementMapper<Resource> {

    @Override
    public Resource map(Element element) {
      return new Resource(element);
    }
  }

  private static class PluginMapper implements ElementMapper<Plugin> {

    @Override
    public Plugin map(Element element) {
      return new Plugin(element);
    }
  }
}
