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

import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;
import static org.eclipse.che.commons.xml.NewElement.createElement;
import static org.eclipse.che.commons.xml.XMLTreeLocation.after;
import static org.eclipse.che.commons.xml.XMLTreeLocation.inTheBegin;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.commons.xml.Element;
import org.eclipse.che.commons.xml.NewElement;

/**
 * Describes <i>/project/build/plugins/plugin</i>.
 *
 * <p>Supports next data:
 *
 * <ul>
 *   <li>artifactId
 *   <li>groupId
 *   <li>configuration
 * </ul>
 *
 * @author Eugene Voevodin
 */
public class Plugin {

  private String artifactId;
  private String groupId;
  private String version;
  private Map<String, String> configuration;

  Element pluginElement;

  public Plugin() {}

  Plugin(Element element) {
    pluginElement = element;
    if (element.hasSingleChild("artifactId")) {
      artifactId = element.getChildText("artifactId");
    }
    if (element.hasSingleChild("groupId")) {
      groupId = element.getChildText("groupId");
    }
    if (element.hasSingleChild("version")) {
      groupId = element.getChildText("version");
    }
    if (element.hasSingleChild("configuration")) {
      configuration = fetchConfiguration(element.getSingleChild("configuration"));
    }
  }

  /** Returns plugin artifact identifier */
  public String getArtifactId() {
    return artifactId;
  }

  /** Returns plugin group identifier */
  public String getGroupId() {
    return groupId;
  }

  /**
   * Returns plugin configuration. If plugin has nested configuration elements it will not be
   * fetched.
   *
   * <p>Consider following configuration:
   *
   * <pre>{@code
   * <configuration>
   *     <item1>value1</item1>
   *     <item2>value2</item2>
   *     <properties>
   *         <property1>property</property1>
   *     </properties>
   * </configuration>
   * }</pre>
   *
   * <p>Resulting map will contain next data <i>item1="value1"</i>, <i>item2="value2"</i> and
   * <i>properties=null</i>
   *
   * <p><b>Note: update methods should not be used on returned map</b>
   *
   * @return plugin configuration or empty map when plugin doesn't have configuration
   */
  public Map<String, String> getConfiguration() {
    if (configuration == null) {
      return emptyMap();
    }
    return new HashMap<>(configuration);
  }

  /**
   * Sets plugin artifact identifier
   *
   * @param artifactId new artifact identifier, if new artifact id is {@code null} and current
   *     plugin element related with xml element then <i>artifactId</i> element will be removed from
   *     xml as well as from plugin model
   * @return this plugin instance
   */
  public Plugin setArtifactId(String artifactId) {
    this.artifactId = artifactId;
    if (!isNew()) {
      if (artifactId == null) {
        pluginElement.removeChild("artifactId");
      } else if (pluginElement.hasSingleChild("artifactId")) {
        pluginElement.getSingleChild("artifactId").setText(artifactId);
      } else {
        pluginElement.insertChild(
            createElement("artifactId", artifactId), after("groupId").or(inTheBegin()));
      }
    }
    return this;
  }

  /** Returns plugin version. */
  public String getVersion() {
    return version;
  }

  /** Sets plugin version. */
  public Plugin setVersion(String version) {
    this.version = version;
    if (!isNew()) {
      if (version == null) {
        pluginElement.removeChild("version");
      } else if (pluginElement.hasSingleChild("version")) {
        pluginElement.getSingleChild("version").setText(version);
      } else {
        pluginElement.insertChild(
            createElement("version", version), after("artifactId").or(inTheBegin()));
      }
    }
    return this;
  }

  /**
   * Sets plugin group identifier
   *
   * @param groupId new group identifier, if new group id is {@code null} and current plugin element
   *     related with xml element then <i>groupId</i> element will be removed from xml as well as
   *     from plugin model
   * @return this plugin instance
   */
  public Plugin setGroupId(String groupId) {
    this.groupId = groupId;
    if (!isNew()) {
      if (groupId == null) {
        pluginElement.removeChild("groupId");
      } else if (pluginElement.hasSingleChild("groupId")) {
        pluginElement.getSingleChild("groupId").setText(groupId);
      } else {
        pluginElement.insertChild(createElement("groupId", groupId), inTheBegin());
      }
    }
    return this;
  }

  /**
   * Sets new configuration with new configuration
   *
   * @param configuration new plugin configuration, if new configuration is {@code null} or
   *     <i>empty</i> and plugin element related with xml element then <i>configuration</i> element
   *     will be removed from xml as well as from plugin model
   * @return this plugin instance
   */
  public Plugin setConfiguration(Map<String, String> configuration) {
    if (configuration == null || configuration.isEmpty()) {
      removeConfiguration();
    } else {
      setConfiguration0(configuration);
    }
    return this;
  }

  /**
   * Sets configuration property value as {@literal <name>value</name>}.
   *
   * <p>If element doesn't have configuration element it will be created as well. <b>Note: it should
   * not be used with nested configuration elements</b>
   *
   * @param name property name to set
   * @param value property value to set
   * @return this plugin instance
   * @throws NullPointerException when {@code name} or {@code value} is {@code null}
   */
  public Plugin setConfigProperty(String name, String value) {
    requireNonNull(name, "Configuration property name should not be null");
    requireNonNull(value, "Configuration property value should not be null");
    if (!isNew()) {
      addConfigPropertyToXML(name, value);
    }
    configuration().put(name, value);
    return this;
  }

  /**
   * Removes configuration property. If configuration has nested element with removal {@code name}
   * it will be removed with all related children.
   *
   * @param name configuration property which indicated element that should be removed
   * @return this plugin instance
   * @throws NullPointerException when {@code name} is null
   */
  public Plugin removeConfigProperty(String name) {
    requireNonNull(name, "Configuration property name should ne null");
    if (configuration().remove(name) != null && !isNew()) {
      removeConfigPropertyFromXML(name);
    }
    return this;
  }

  /** Returns plugin identifier as <i>groupId:artifactId</i> */
  public String getId() {
    return groupId + ':' + artifactId;
  }

  @Override
  public String toString() {
    return getId();
  }

  private void removeConfigPropertyFromXML(String name) {
    if (configuration.isEmpty()) {
      pluginElement.removeChild("configuration");
    } else {
      pluginElement.getSingleChild("configuration").removeChild(name);
    }
  }

  private void addConfigPropertyToXML(String name, String value) {
    if (configuration().containsKey(name)) {
      pluginElement.getSingleChild("configuration").getSingleChild(name).setText(value);
    } else if (configuration.isEmpty()) {
      pluginElement.appendChild(createElement("configuration", createElement(name, value)));
    } else {
      pluginElement.getSingleChild("configuration").appendChild(createElement(name, value));
    }
  }

  private Map<String, String> configuration() {
    return configuration == null ? configuration = new HashMap<>() : configuration;
  }

  private void setConfiguration0(Map<String, String> configuration) {
    this.configuration = new HashMap<>(configuration);

    if (isNew()) return;

    if (pluginElement.hasSingleChild("configuration")) {
      final Element confElement = pluginElement.getSingleChild("configuration");
      // remove all configuration properties from element
      for (Element property : confElement.getChildren()) {
        property.remove();
      }
      // append each new property to "configuration" element
      for (Map.Entry<String, String> property : configuration.entrySet()) {
        confElement.appendChild(createElement(property.getKey(), property.getValue()));
      }
    } else {
      final NewElement newConfiguration = createElement("configuration");
      for (Map.Entry<String, String> entry : configuration.entrySet()) {
        newConfiguration.appendChild(createElement(entry.getKey(), entry.getValue()));
      }
      // insert new configuration to xml
      pluginElement.appendChild(newConfiguration);
    }
  }

  private void removeConfiguration() {
    if (!isNew()) {
      pluginElement.removeChild("properties");
    }
    configuration = null;
  }

  private Map<String, String> fetchConfiguration(Element element) {
    final Map<String, String> configuration = new HashMap<>();
    for (Element configProperty : element.getChildren()) {
      if (!configProperty.hasChildren()) {
        configuration.put(configProperty.getName(), configProperty.getText());
      }
    }
    return configuration;
  }

  NewElement asXMLElement() {
    final NewElement xmlPlugin = createElement("plugin");
    if (groupId != null) {
      xmlPlugin.appendChild(createElement("groupId", groupId));
    }
    if (artifactId != null) {
      xmlPlugin.appendChild(createElement("artifactId", artifactId));
    }
    if (version != null) {
      xmlPlugin.appendChild(createElement("version", version));
    }
    if (configuration != null && !configuration.isEmpty()) {
      final NewElement configElement = createElement("configuration");
      for (Map.Entry<String, String> configProperty : configuration.entrySet()) {
        configElement.appendChild(
            createElement(configProperty.getKey(), configProperty.getValue()));
      }
      xmlPlugin.appendChild(configElement);
    }
    return xmlPlugin;
  }

  private boolean isNew() {
    return pluginElement == null;
  }
}
