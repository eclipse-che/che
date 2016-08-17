/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.inject;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.name.Names;
import com.google.inject.servlet.ServletModule;
import com.google.inject.util.Modules;
import com.google.inject.util.Providers;

import org.eclipse.che.inject.lifecycle.DestroyErrorHandler;
import org.eclipse.che.inject.lifecycle.DestroyModule;
import org.eclipse.che.inject.lifecycle.Destroyer;
import org.eclipse.che.inject.lifecycle.InitModule;
import org.everrest.guice.servlet.EverrestGuiceContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CheBootstrap is entry point of Che application implemented as ServletContextListener.
 * <ul>
 * <li>Initializes Guice Injector</li>
 * <li>Automatically binds all the subclasses of com.google.inject.Module annotated with &#064DynaModule</li>
 * <li>Loads configuration from .properties and .xml files located in <i>/WEB-INF/classes/che</i> directory</li>
 * <li>Overrides it with external configuration located in directory pointed by <i>CHE_LOCAL_CONF_DIR</i> env variable (if any)</li>
 * <li>Binds all environment variables (visible as prefixed with "env.") and system properties (visible as prefixed with "sys.")</li>
 * <li>Thanks to Everrest integration injects all the properly annotated (see Everrest docs) REST Resources. Providers and ExceptionMappers
 * and inject necessary dependencies</li>
 * </ul>
 * <p/>
 * Configuration properties are bound as a {@code &#064Named}. For example:
 * Following entry in the .property file:
 * {@code myProp=value}
 * may be injected into constructor (other options are valid too of course) as following:
 * <pre>
 * &#064Inject
 * public MyClass(&#064Named("myProp") String my) {
 * }
 * </pre>
 * <p/>
 * It's possible to use system properties or environment variables in .properties files.
 * <pre>
 * my_app.input_dir=${root_data}/input/
 * my_app.output_dir=${root_data}/output/
 * </pre>
 * NOTE: System property always takes preference on environment variable with the same name.
 * <p/>
 * <table>
 * <tr><th>Value</th><th>System property</th><th>Environment variable</th><th>Result</th></tr>
 * <tr><td>${root_data}/input/</td><td>/home/andrew/temp</td><td>&nbsp;</td><td>/home/andrew/temp/input/</td></tr>
 * <tr><td>${root_data}/input/</td><td>&nbsp;</td><td>/usr/local</td><td>/usr/local/input/</td></tr>
 * <tr><td>${root_data}/input/</td><td>/home/andrew/temp</td><td>/usr/local</td><td>/home/andrew/temp/input/</td></tr>
 * <tr><td>${root_data}/input/</td><td>&nbsp;</td><td>&nbsp;</td><td>${root_data}/input/</td></tr>
 * </table>
 *
 * @author gazarenkov
 * @author andrew00x
 * @author Florent Benoit
 */
public class CheBootstrap extends EverrestGuiceContextListener {

    /**
     * Path to the internal folder that is expected in WEB-INF/classes
     */
    private static final String WEB_INF_RESOURCES = "che";

    /**
     * Backward compliant path to the internal folder that is expected in WEB-INF/classes
     */
    private static final String COMPLIANT_WEB_INF_RESOURCES = "codenvy";

    private static final Logger LOG = LoggerFactory.getLogger(CheBootstrap.class);

    /**
     * Environment variable that is used to override some Che settings properties.
     */
    public static final String CHE_LOCAL_CONF_DIR = "CHE_LOCAL_CONF_DIR";


    private final List<Module> modules = new ArrayList<>();

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
        // based on logic that getServletModule() is called BEFORE getModules() in the EverrestGuiceContextListener
        modules.add(new InitModule(PostConstruct.class));
        modules.add(new DestroyModule(PreDestroy.class, DestroyErrorHandler.DUMMY));
        modules.add(new URIConverter());
        modules.add(new URLConverter());
        modules.add(new FileConverter());
        modules.add(new PathConverter());
        modules.add(new StringArrayConverter());
        modules.add(new PairConverter());
        modules.add(new PairArrayConverter());
        modules.addAll(ModuleScanner.findModules());
        modules.add(Modules.override(new WebInfConfiguration()).with(new ExtConfiguration()));
        return modules;
    }

    /** see http://google-guice.googlecode.com/git/javadoc/com/google/inject/servlet/ServletModule.html */
    @Override
    protected ServletModule getServletModule() {
        // Servlets and other web components may be configured with custom Modules.
        return null;
    }

    /** ConfigurationModule binding configuration located in <i>/WEB-INF/classes/che</i> directory */
    static class WebInfConfiguration extends AbstractConfigurationModule {
        protected void configure() {
            URL compliantWebInfConf = this.getClass().getClassLoader().getResource(COMPLIANT_WEB_INF_RESOURCES);
            if (compliantWebInfConf != null) {
                bindConf(new File(compliantWebInfConf.getFile()));
            }
            URL webInfConf = this.getClass().getClassLoader().getResource(WEB_INF_RESOURCES);
            if (webInfConf != null) {
                bindConf(new File(webInfConf.getFile()));
            }
        }
    }

    /**
     * ConfigurationModule binding environment variables, system properties and configuration in directory pointed by
     * <i>CHE_LOCAL_CONF_DIR</i> Env variable.
     */
    static class ExtConfiguration extends AbstractConfigurationModule {
        @Override
        protected void configure() {
            // binds environment variables visible as prefixed with "env."
            bindEnvProperties("env.", System.getenv());
            // binds system properties visible as prefixed with "sys."
            bindProperties("sys.", System.getProperties());
            String extConfig = System.getenv(CHE_LOCAL_CONF_DIR);
            if (extConfig != null) {
                bindConf(new File(extConfig));
            }
        }
    }

    private static final Pattern PATTERN = Pattern.compile("\\$\\{[^\\}^\\$\\{]+\\}");

    static abstract class AbstractConfigurationModule extends AbstractModule {
        protected void bindConf(File confDir) {
            final File[] files = confDir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (!f.isDirectory()) {
                        if ("properties".equals(ext(f.getName()))) {
                            Properties properties = new Properties();
                            try (Reader reader = Files.newBufferedReader(f.toPath(), Charset.forName("UTF-8"))) {
                                properties.load(reader);
                            } catch (IOException e) {
                                throw new IllegalStateException(String.format("Unable to read configuration file %s", f), e);
                            }
                            bindProperties(properties);
                        }
                    }
                }
            }
        }

        private String ext(String fileName) {
            String extension = "";
            int i = fileName.lastIndexOf('.');
            if (i > 0) {
                extension = fileName.substring(i + 1);
            }
            return extension;
        }

        protected void bindProperties(Properties properties) {
            bindProperties(null, properties.entrySet());
        }

        protected void bindProperties(String prefix, Properties properties) {
            bindProperties(prefix, properties.entrySet());
        }

        protected void bindEnvProperties(String prefix, Map<String, String> properties) {
            bindProperties(prefix, properties.entrySet(), true);
        }

        protected <K, V> void bindProperties(String prefix, Iterable<Map.Entry<K, V>> properties) {
           bindProperties(prefix, properties, false);
        }

        protected <K, V> void bindProperties(String prefix, Iterable<Map.Entry<K, V>> properties, boolean skipUnresolved) {
            StringBuilder buf = null;
            for (Map.Entry<K, V> e : properties) {
                String pValue = (String)e.getValue();
                if ("NULL".equals(pValue)) {
                    bind(String.class).annotatedWith(Names.named(prefix == null ? (String)e.getKey() : (prefix + e.getKey())))
                                      .toProvider(Providers.<String>of(null));
                } else {
                    final Matcher matcher = PATTERN.matcher(pValue);
                    if (matcher.find()) {
                        int start = 0;
                        if (buf == null) {
                            buf = new StringBuilder();
                        } else {
                            buf.setLength(0);
                        }
                        do {
                            final int i = matcher.start();
                            final int j = matcher.end();
                            buf.append(pValue.substring(start, i));
                            final String name = pValue.substring(i + 2, j - 1);
                            String actual = System.getProperty(name);
                            if (actual == null) {
                                actual = System.getenv(name);
                            }
                            if (actual != null) {
                                buf.append(actual);
                            } else if (skipUnresolved) {
                                buf.append(pValue.substring(i, j));
                                LOG.warn("Environment variable " + name + " cannot be resolved, leaving as is.");
                            } else {
                                throw new ConfigurationException(
                                        "Property " + name + " is not found as system property or environment variable.");
                            }

                            start = matcher.end();
                        } while (matcher.find());
                        buf.append(pValue.substring(start));
                        pValue = buf.toString();
                    }
                    bindConstant().annotatedWith(Names.named(prefix == null ? (String)e.getKey() : (prefix + e.getKey()))).to(pValue);
                }
            }
        }
    }
}
