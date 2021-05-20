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
package org.eclipse.che.inject;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.eclipse.che.inject.lifecycle.DestroyErrorHandler.LOG_HANDLER;

import com.google.common.base.Splitter;
import com.google.common.io.Files;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.name.Names;
import com.google.inject.servlet.ServletModule;
import com.google.inject.util.Modules;
import com.google.inject.util.Providers;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import org.eclipse.che.commons.lang.concurrent.LoggingUncaughtExceptionHandler;
import org.eclipse.che.inject.lifecycle.DestroyModule;
import org.eclipse.che.inject.lifecycle.Destroyer;
import org.eclipse.che.inject.lifecycle.InitModule;
import org.everrest.guice.servlet.EverrestGuiceContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CheBootstrap is entry point of Che application implemented as ServletContextListener.
 *
 * <ul>
 *   <li>Initializes Guice Injector
 *   <li>Automatically binds all the subclasses of com.google.inject.Module annotated with
 *       &#064DynaModule
 *   <li>Loads configuration from .properties and .xml files located in <i>/WEB-INF/classes/che</i>
 *       directory
 *   <li>Overrides it with external configuration located in directory pointed by
 *       <i>CHE_LOCAL_CONF_DIR</i> env variable (if any)
 *   <li>Binds all environment variables (visible as prefixed with "env.") and system properties
 *       (visible as prefixed with "sys.")
 *   <li>Thanks to Everrest integration injects all the properly annotated (see Everrest docs) REST
 *       Resources. Providers and ExceptionMappers and inject necessary dependencies
 * </ul>
 *
 * <p>Configuration properties are bound as a {@code &#064Named}. For example: Following entry in
 * the .property file: {@code myProp=value} may be injected into constructor (other options are
 * valid too of course) as following:
 *
 * <pre>
 * &#064Inject
 * public MyClass(&#064Named("myProp") String my) {
 * }
 * </pre>
 *
 * <p>It's possible to use system properties or environment variables in .properties files.
 *
 * <pre>
 * my_app.input_dir=${root_data}/input/
 * my_app.output_dir=${root_data}/output/
 * </pre>
 *
 * NOTE: System property always takes preference on environment variable with the same name.
 *
 * <p>
 *
 * <table>
 * <tr><th>Value</th><th>System property</th><th>Environment variable</th><th>Result</th></tr>
 * <tr><td>${root_data}/input/</td><td>/home/andrew/temp</td><td>&nbsp;</td><td>/home/andrew/temp/input/</td></tr>
 * <tr><td>${root_data}/input/</td><td>&nbsp;</td><td>/usr/local</td><td>/usr/local/input/</td></tr>
 * <tr><td>${root_data}/input/</td><td>/home/andrew/temp</td><td>/usr/local</td><td>/home/andrew/temp/input/</td></tr>
 * <tr><td>${root_data}/input/</td><td>&nbsp;</td><td>&nbsp;</td><td>${root_data}/input/</td></tr>
 * </table>
 *
 * During code evolution might be the case when someone will want to rename some property. This
 * brings a couple of problems like support of old property name in external plugins and support old
 * configuration values in code with the new property name. To cover these cases there is a file
 * che_aliases.properties that contains old names of all existed properties. It has such format
 * current_name =old_name, very_old_name. In this case will be such binding.
 *
 * <p>Always current_name = current_value if old_name property exist it will be binded to old_value
 * and current_name = old_value and very_old_name = old_value if very_old_name property exist it
 * will be binded to very_old_value, and current_name = very_old_value and old_name = very_old_value
 *
 * <p>NOTE: it's prohibited to use a different name for same property on the same level. From the
 * example above - you can use environment property CHE_CURRENT_NAME and CHE_OLD_NAME. But you can
 * use it on a different level, for instance, environment property and system property.
 *
 * @author gazarenkov
 * @author andrew00x
 * @author Florent Benoit
 * @author Sergii Kabashniuk
 */
public class CheBootstrap extends EverrestGuiceContextListener {
  private static final Logger LOG = LoggerFactory.getLogger(CheBootstrap.class);

  /** Environment variable that is used to override some Che settings properties. */
  public static final String CHE_LOCAL_CONF_DIR = "CHE_LOCAL_CONF_DIR";

  public static final String PROPERTIES_ALIASES_CONFIG_FILE = "che_aliases.properties";

  /** Path to the internal folder that is expected in WEB-INF/classes */
  private static final String WEB_INF_RESOURCES = "che";

  /** Backward compliant path to the internal folder that is expected in WEB-INF/classes */
  private static final String COMPLIANT_WEB_INF_RESOURCES = "codenvy";

  private static final String NULL = "NULL";

  private final List<Module> modules = new ArrayList<>();

  static {
    Thread.setDefaultUncaughtExceptionHandler(LoggingUncaughtExceptionHandler.getInstance());
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    final ServletContext ctx = sce.getServletContext();
    final Injector injector = getInjector(ctx);
    if (injector != null) {
      injector.getInstance(Destroyer.class).destroy();
    }
    super.contextDestroyed(sce);
  }

  @Override
  protected List<Module> getModules() {
    // based on logic that getServletModule() is called BEFORE getModules() in the
    // EverrestGuiceContextListener
    modules.add(new InitModule(PostConstruct.class));
    modules.add(new DestroyModule(PreDestroy.class, LOG_HANDLER));
    modules.add(new URIConverter());
    modules.add(new URLConverter());
    modules.add(new FileConverter());
    modules.add(new PathConverter());
    modules.add(new StringArrayConverter());
    modules.add(new PairConverter());
    modules.add(new PairArrayConverter());
    modules.addAll(ModuleScanner.findModules());
    Map<String, Set<String>> aliases = readConfigurationAliases();
    Module firstConfigurationPermutation =
        Modules.override(new WebInfConfiguration(aliases)).with(new ExtConfiguration(aliases));
    Module secondConfigurationPermutation =
        Modules.override(firstConfigurationPermutation)
            .with(new CheSystemPropertiesConfigurationModule(aliases));
    Module lastConfigurationPermutation =
        Modules.override(secondConfigurationPermutation)
            .with(new CheEnvironmentVariablesConfigurationModule(aliases));
    modules.add(lastConfigurationPermutation);
    return modules;
  }

  private Map<String, Set<String>> readConfigurationAliases() {
    URL aliasesResource = getClass().getClassLoader().getResource(PROPERTIES_ALIASES_CONFIG_FILE);
    Map<String, Set<String>> aliases = new HashMap<>();
    if (aliasesResource != null) {
      Properties properties = new Properties();
      File aliasesFile = new File(aliasesResource.getFile());
      try (Reader reader = Files.newReader(aliasesFile, Charset.forName("UTF-8"))) {
        properties.load(reader);
      } catch (IOException e) {
        throw new IllegalStateException(
            format("Unable to read configuration aliases from file %s", aliasesFile), e);
      }
      for (Map.Entry<Object, Object> entry : properties.entrySet()) {
        String value = (String) entry.getValue();
        aliases.put(
            (String) entry.getKey(),
            Splitter.on(',').splitToList(value).stream().map(String::trim).collect(toSet()));
      }
    }
    return aliases;
  }

  /**
   * see http://google-guice.googlecode.com/git/javadoc/com/google/inject/servlet/ServletModule.html
   */
  @Override
  protected ServletModule getServletModule() {
    // Servlets and other web components may be configured with custom Modules.
    return null;
  }

  /** ConfigurationModule binding configuration located in <i>/WEB-INF/classes/che</i> directory */
  static class WebInfConfiguration extends AbstractConfigurationModule {
    WebInfConfiguration(Map<String, Set<String>> aliases) {
      super(aliases);
    }

    protected void configure() {
      URL compliantWebInfConf =
          getClass().getClassLoader().getResource(COMPLIANT_WEB_INF_RESOURCES);
      if (compliantWebInfConf != null) {
        bindConf(new File(compliantWebInfConf.getFile()));
      }
      URL webInfConf = getClass().getClassLoader().getResource(WEB_INF_RESOURCES);
      if (webInfConf != null) {
        bindConf(new File(webInfConf.getFile()));
      }
    }
  }

  /**
   * ConfigurationModule binding environment variables, system properties and configuration in
   * directory pointed by <i>CHE_LOCAL_CONF_DIR</i> Env variable.
   */
  static class ExtConfiguration extends AbstractConfigurationModule {
    ExtConfiguration(Map<String, Set<String>> aliases) {
      super(aliases);
    }

    @Override
    protected void configure() {
      bindProperties("env.", System.getenv());
      bindProperties("sys.", System.getProperties());
      String extConfig = System.getenv(CHE_LOCAL_CONF_DIR);
      if (extConfig != null) {
        bindConf(new File(extConfig));
      }
    }
  }

  static class CheSystemPropertiesConfigurationModule extends AbstractConfigurationModule {
    CheSystemPropertiesConfigurationModule(Map<String, Set<String>> aliases) {
      super(aliases);
    }

    @Override
    protected void configure() {
      Iterable<Map.Entry<Object, Object>> cheProperties =
          System.getProperties()
              .entrySet()
              .stream()
              .filter(new PropertyNamePrefixPredicate<>("che.", "codenvy."))
              .collect(toList());
      bindProperties(null, cheProperties);
    }
  }

  static class CheEnvironmentVariablesConfigurationModule extends AbstractConfigurationModule {
    CheEnvironmentVariablesConfigurationModule(Map<String, Set<String>> aliases) {
      super(aliases);
    }

    @Override
    protected void configure() {
      Iterable<Map.Entry<String, String>> cheProperties =
          System.getenv()
              .entrySet()
              .stream()
              .filter(new PropertyNamePrefixPredicate<>("CHE_", "CODENVY_"))
              .map(new EnvironmentVariableToSystemPropertyFormatNameConverter())
              .collect(toList());
      bindProperties(null, cheProperties);
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

  static class PropertyNamePrefixRemover<K, V>
      implements Function<Map.Entry<K, V>, Map.Entry<String, V>> {
    final int prefixLength;

    PropertyNamePrefixRemover(int prefixLength) {
      this.prefixLength = prefixLength;
    }

    @Override
    public Map.Entry<String, V> apply(Map.Entry<K, V> entry) {
      return new SimpleEntry<>(((String) entry.getKey()).substring(prefixLength), entry.getValue());
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
      return new SimpleEntry<>(name, entry.getValue());
    }
  }

  private static final Pattern PROPERTIES_PLACE_HOLDER_PATTERN =
      Pattern.compile("\\$\\{[^\\}^\\$\\{]+\\}");

  abstract static class AbstractConfigurationModule extends AbstractModule {
    final Map<String, Set<String>> bindMap;

    AbstractConfigurationModule(Map<String, Set<String>> aliases) {
      this.bindMap = new HashMap<>(aliases);
      for (Entry<String, Set<String>> entry : aliases.entrySet()) {
        for (String alias : entry.getValue()) {
          Set<String> newAliases = new HashSet<>(entry.getValue());
          newAliases.remove(alias);
          newAliases.add(entry.getKey());
          bindMap.put(alias, newAliases);
        }
      }
    }

    protected void bindConf(File confDir) {
      final File[] files = confDir.listFiles();
      if (files != null) {
        for (File file : files) {
          if (!file.isDirectory()) {
            if ("properties".equals(Files.getFileExtension(file.getName()))) {
              Properties properties = new Properties();
              try (Reader reader = Files.newReader(file, Charset.forName("UTF-8"))) {
                properties.load(reader);
              } catch (IOException e) {
                throw new IllegalStateException(
                    format("Unable to read configuration file %s", file), e);
              }
              bindProperties(properties);
            }
          }
        }
      }
    }

    protected void bindProperties(Properties properties) {
      bindProperties(null, properties.entrySet());
    }

    protected void bindProperties(String prefix, Properties properties) {
      bindProperties(prefix, properties.entrySet());
    }

    protected void bindProperties(String prefix, Map<String, String> properties) {
      bindProperties(prefix, properties.entrySet(), true);
    }

    protected <K, V> void bindProperties(String prefix, Iterable<Map.Entry<K, V>> properties) {
      bindProperties(prefix, properties, false);
    }

    protected <K, V> void bindProperties(
        String prefix, Iterable<Map.Entry<K, V>> properties, boolean skipUnresolved) {
      StringBuilder buf = null;
      for (Map.Entry<K, V> e : properties) {
        String name = (String) e.getKey();
        String value = (String) e.getValue();
        if (NULL.equals(value)) {
          bindProperty(prefix, name, null);
        } else {
          final Matcher matcher = PROPERTIES_PLACE_HOLDER_PATTERN.matcher(value);
          if (matcher.find()) {
            int start = 0;
            if (buf == null) {
              buf = new StringBuilder();
            } else {
              buf.setLength(0);
            }
            do {
              buf.append(value.substring(start, matcher.start()));
              final String placeholder = value.substring(matcher.start(), matcher.end());
              final String placeholderName = removePlaceholderFormatting(placeholder);
              String resolvedPlaceholder = resolvePlaceholder(placeholderName);
              if (resolvedPlaceholder != null) {
                buf.append(resolvedPlaceholder);
              } else if (skipUnresolved) {
                buf.append(placeholder);
                LOG.warn(
                    "Placeholder {} cannot be resolved neither from environment variable nor from system property, "
                        + "leaving as is.",
                    placeholderName);
              } else {
                throw new ConfigurationException(
                    format(
                        "%s is not a system property or environment variable.", placeholderName));
              }

              start = matcher.end();
            } while (matcher.find());
            buf.append(value.substring(start));
            value = buf.toString();
          }
          bindProperty(prefix, name, value);
        }
      }
    }

    private void bindProperty(String prefix, String name, String value) {
      String key = prefix == null ? name : (prefix + name);
      Set<String> aliasesForName = bindMap.get(name);
      if (value == null) {
        LOG.debug("Binding `{}` to `null`", key);
        bind(String.class).annotatedWith(Names.named(key)).toProvider(Providers.<String>of(null));
        if (aliasesForName != null) {
          for (String alias : aliasesForName) {
            String bindKey = prefix == null ? alias : prefix + alias;
            LOG.debug("Binding `{}` to `null`", bindKey);
            bind(String.class)
                .annotatedWith(Names.named(bindKey))
                .toProvider(Providers.<String>of(null));
          }
        }
      } else {
        LOG.debug("Binding `{}` to `{}`", key, value);
        bindConstant().annotatedWith(Names.named(key)).to(value);
        if (aliasesForName != null) {
          for (String alias : aliasesForName) {
            String bindKey = prefix == null ? alias : prefix + alias;
            LOG.debug("Binding `{}` to `{}`", bindKey, value);
            bindConstant().annotatedWith(Names.named(bindKey)).to(value);
          }
        }
      }
    }

    private String removePlaceholderFormatting(String placeholder) {
      return placeholder.substring(2, placeholder.length() - 1);
    }

    private String resolvePlaceholder(String placeholderName) {
      String resolved = System.getProperty(placeholderName);
      if (resolved == null) {
        resolved = System.getenv(placeholderName);
      }
      LOG.debug("Resolving `{}` to `{}`", placeholderName, resolved);
      return resolved;
    }
  }
}
