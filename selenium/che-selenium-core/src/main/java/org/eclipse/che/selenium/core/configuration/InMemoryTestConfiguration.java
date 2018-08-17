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
package org.eclipse.che.selenium.core.configuration;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.eclipse.che.inject.CheBootstrap;

/** In memory storage of TestConfiguration based on Map<String, String> config */
public class InMemoryTestConfiguration implements TestConfiguration {

  private final Map<String, String> config;

  public InMemoryTestConfiguration() {
    this(new HashMap<>());
  }

  public InMemoryTestConfiguration(TestConfiguration... configuration) {
    this();
    for (TestConfiguration testConfiguration : configuration) {
      addAll(testConfiguration.getMap());
    }
    // convert value of CHE_INFRASTRUCTURE to upper case to comply with Infrastructure
    // enumeration;
    config.put("che.infrastructure", config.get("che.infrastructure").toUpperCase());
  }

  public InMemoryTestConfiguration(Map<String, String> config) {
    this.config = config;
  }

  void addAll(Map<String, String> config) {
    this.config.putAll(config);
  }

  @Override
  public boolean isConfigured(String key) {
    return config.containsKey(key);
  }

  @Override
  public String getString(String key) {
    String value = config.get(key);
    if (value == null) {
      StringBuilder builder = new StringBuilder();
      builder
          .append("\n")
          .append("======== IMPORTANT =========\n")
          .append("Key ")
          .append(key)
          .append(" is not configured\n")
          .append("You can configure it as :\n")
          .append("1. System property.      Example: \t-D")
          .append(key)
          .append("=yourvalue \n")
          .append("2. Environment variable. Example: \texport CODENVY_")
          .append(key.toUpperCase().replace("_", "=").replace('.', '_').replace("=", "__"))
          .append("=yourvalue \n")
          .append("3. Or configured it in a property file in folder declared as ")
          .append(CheBootstrap.CHE_LOCAL_CONF_DIR)
          .append(" environment variable\n")
          .append("============================\n");
      throw new ConfigurationException(builder.toString());
    }
    return value;
  }

  @Override
  public Boolean getBoolean(String key) {
    return Boolean.parseBoolean(config.get(key));
  }

  @Override
  public Integer getInt(String key) {
    return Integer.parseInt(config.get(key));
  }

  @Override
  public Long getLong(String key) {
    return Long.parseLong(config.get(key));
  }

  @Override
  public Map<String, String> getMap() {
    return ImmutableMap.copyOf(config);
  }

  @Override
  public Map<String, String> getMap(String keyPrefix) {
    return config
        .entrySet()
        .stream()
        .filter(e -> e.getKey().startsWith(keyPrefix))
        .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
  }
}
