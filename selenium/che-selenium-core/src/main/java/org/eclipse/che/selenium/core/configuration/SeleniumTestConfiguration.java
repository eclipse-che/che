/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.core.configuration;

import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.eclipse.che.inject.CheBootstrap;

/**
 * Represents in memory based storage of the test configuration.
 *
 * <p>Values of configuration are loaded in the following sequence:<br>
 * 1. Default configuration from target/conf/selenium.properties file.<br>
 * 2. All properties from CHE_LOCAL_CONF_DIR directory.<br>
 * 3. From java system properties (will be given prefix "sys.").<br>
 * 4. From environment variables (will be given prefix "env."). Symbol "_" is replaced by "." and
 * "__" is replaced by "_" in names of environment variables.<br>
 * Variables which start from "che." or "codenvy." don't have an additional prefix "sys." or "env.".
 *
 * @author Sergii Kabashniuk
 */
@Singleton
public class SeleniumTestConfiguration extends InMemoryTestConfiguration {

  @Inject
  public SeleniumTestConfiguration() {
    super(
        new DefaultConfiguration(),
        new PropertiesConfiguration(),
        new SystemPropertiesConfiguration(),
        new EnvironmentVariablesConfiguration());
  }

  static class SystemPropertiesConfiguration extends InMemoryTestConfiguration {

    SystemPropertiesConfiguration() {
      super();
      addAll(
          System.getProperties()
              .entrySet()
              .stream()
              .filter(new PropertyNamePrefixPredicate<>("che.", "codenvy."))
              .map(e -> new AbstractMap.SimpleEntry<>((String) e.getKey(), ((String) e.getValue())))
              .collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue)));
      addAll(
          System.getProperties()
              .entrySet()
              .stream()
              .map(e -> new AbstractMap.SimpleEntry<>("sys." + e.getKey(), ((String) e.getValue())))
              .collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue)));
    }
  }

  static class EnvironmentVariablesConfiguration extends InMemoryTestConfiguration {

    EnvironmentVariablesConfiguration() {
      super();
      addAll(
          System.getenv()
              .entrySet()
              .stream()
              .filter(new PropertyNamePrefixPredicate<>("CHE_", "CODENVY_"))
              .map(new EnvironmentVariableToSystemPropertyFormatNameConverter())
              .collect(Collectors.toMap(Entry::getKey, Entry::getValue)));
      addAll(
          System.getenv()
              .entrySet()
              .stream()
              .map(new EnvironmentVariableToSystemPropertyFormatNameConverter())
              .map(e -> new AbstractMap.SimpleEntry<>("env." + e.getKey(), e.getValue()))
              .collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue)));
    }
  }

  static class PropertyNamePrefixPredicate<K, V> implements Predicate<Map.Entry<K, V>> {
    final String[] prefixes;

    PropertyNamePrefixPredicate(String... prefix) {
      this.prefixes = prefix;
    }

    @Override
    public boolean test(Map.Entry<K, V> entry) {
      for (String prefix : prefixes) {
        if (((String) entry.getKey()).startsWith(prefix)) {
          return true;
        }
      }
      return false;
    }
  }

  static class EnvironmentVariableToSystemPropertyFormatNameConverter
      implements Function<Map.Entry<String, String>, Map.Entry<String, String>> {
    @Override
    public Map.Entry<String, String> apply(Map.Entry<String, String> entry) {
      String name = entry.getKey();
      name = name.toLowerCase();
      // replace single underscore with dot and double underscores with single underscore
      // at first replace double underscores with equal sign which is forbidden in env variable name
      // then replace single underscores
      // then recover underscore from equal sign
      name = name.replace("__", "=");
      name = name.replace('_', '.');
      name = name.replace("=", "_");

      // convert value of CHE_INFRASTRUCTURE to upper case to comply with Infrastructure
      // enumeration;
      if (name.equals("che.infrastructure")) {
        return new AbstractMap.SimpleEntry<>(name, entry.getValue().toUpperCase());
      }

      return new AbstractMap.SimpleEntry<>(name, entry.getValue());
    }
  }

  static class DefaultConfiguration extends PropertiesConfiguration {
    DefaultConfiguration() {
      URL defaultConfig = getClass().getClassLoader().getResource("conf/selenium.properties");
      if (defaultConfig != null) {
        addFile(new File(defaultConfig.getFile()));
      }
    }
  }

  /** Implementation of TestConfiguration based on folder with properties files inside. */
  static class PropertiesConfiguration extends InMemoryTestConfiguration {

    PropertiesConfiguration() {
      String extConfig = System.getenv(CheBootstrap.CHE_LOCAL_CONF_DIR);
      if (extConfig != null) {
        File extConfigFile = new File(extConfig);
        if (extConfigFile.isDirectory() && extConfigFile.exists()) {
          final File[] files = extConfigFile.listFiles();
          if (files != null) {
            for (File file : files) {
              addFile(file);
            }
          }
        }
      }
    }

    void addFile(File file) {
      if (!file.isDirectory()) {
        if ("properties".equals(Files.getFileExtension(file.getName()))) {
          Properties properties = new Properties();
          try (Reader reader = Files.newReader(file, Charset.forName("UTF-8"))) {
            properties.load(reader);
          } catch (IOException e) {
            throw new IllegalStateException(
                String.format("Unable to read configuration file %s", file), e);
          }
          addAll(Maps.fromProperties(properties));
        }
      }
    }
  }
}
