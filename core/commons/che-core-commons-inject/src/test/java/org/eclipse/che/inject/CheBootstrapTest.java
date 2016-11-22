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

import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.Injector;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.commons.test.SystemPropertiesHelper;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.inject.Named;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Properties;

import static java.io.File.pathSeparator;
import static java.util.Collections.emptyEnumeration;
import static org.eclipse.che.commons.test.SystemPropertiesHelper.overrideSystemProperties;
import static org.eclipse.che.inject.CheBootstrap.CHE_LOCAL_CONF_DIR;
import static org.eclipse.che.inject.CheBootstrap.PROPERTIES_ALIASES_CONFIG_FILE;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class CheBootstrapTest {
    private CheBootstrap           cheBootstrap;
    private ServletContext         servletContext;
    private File                   che;
    private File                   userCongDir;
    private SystemPropertiesHelper systemPropertiesHelper;

    @BeforeMethod
    public void setUp() throws Exception {
        systemPropertiesHelper = overrideSystemProperties();
        cheBootstrap = new CheBootstrap();
        servletContext = mock(ServletContext.class);

        URL classesDirUrl = Thread.currentThread().getContextClassLoader().getResource(".");
        File classesDir = new File(classesDirUrl.toURI());
        che = new File(classesDir, "che");
        che.mkdir();
        userCongDir = new File(System.getenv(CHE_LOCAL_CONF_DIR));
        userCongDir.mkdirs();

        mockServletContext();
    }

    @AfterMethod
    public void tearDown() throws Exception {
        try {
            cheBootstrap.contextDestroyed(new ServletContextEvent(servletContext));
        } catch (Throwable ignored) {
        }
        systemPropertiesHelper.restoreFromBackup();
        IoUtil.deleteRecursive(che);
        IoUtil.deleteRecursive(userCongDir);
        File aliases = new File(che.getParent(), PROPERTIES_ALIASES_CONFIG_FILE);
        if (aliases.exists()) {
            aliases.delete();
        }
        ModuleScanner.modules.clear();
    }

    private Properties createTestProperties() {
        final Properties properties = new Properties();
        properties.put("test_int", "123");
        properties.put("test_bool", "true");
        properties.put("test_uri", "file:/a/b/c");
        properties.put("test_url", "http://localhost");
        properties.put("test_file", "/a/b/c");
        properties.put("test_strings", "a, b, c");
        properties.put("test_pair_of_strings", "a=b");
        properties.put("test_pair_of_strings2", "a");
        properties.put("test_pair_of_strings3", "a=");
        properties.put("test_pair_array", "a=b,c=d");
        properties.put("some.dir.in_tmp_dir", "${java.io.tmpdir}/some_dir");
        properties.put("suffixed.PATH", "${PATH}" + pathSeparator + "some_path");
        properties.put("nullable", "NULL");
        return properties;
    }

    @Test
    public void readsConfigurationPropertiesFromCheDirectory() throws Exception {
        writePropertiesFile(che, "che.properties", createTestProperties());

        ModuleScanner.modules.add(binder -> binder.bind(TestChePropertiesComponent.class));

        cheBootstrap.contextInitialized(new ServletContextEvent(servletContext));

        Injector injector = retrieveComponentFromServletContext(Injector.class);

        TestChePropertiesComponent testComponent = injector.getInstance(TestChePropertiesComponent.class);
        assertEquals(testComponent.parameter_pair, Pair.of("a", "b"));
        assertEquals(testComponent.parameter_pair2, Pair.of("a", (String)null));
        assertEquals(testComponent.parameter_pair3, Pair.of("a", ""));
        assertEquals(testComponent.parameter_pair_array, new Pair[]{Pair.of("a", "b"), Pair.of("c", "d")});
        assertEquals(testComponent.nullable, null);
        assertEquals(testComponent.parameter_uri, new URI("file:/a/b/c"));
        assertEquals(testComponent.parameter_url, new URL("http://localhost"));
        assertEquals(testComponent.parameter_file, new File("/a/b/c"));
        assertEquals(testComponent.parameter_strings, new String[]{"a", "b", "c"});
        assertEquals(testComponent.parameter_int, 123);
        assertEquals(testComponent.parameter_long, 123);
        assertEquals(testComponent.parameter_bool, true);
        assertEquals(testComponent.someDir, new File(System.getProperty("java.io.tmpdir"), "/some_dir"));
        assertEquals(testComponent.suffixedPath, System.getenv("PATH") + pathSeparator + "some_path");
    }

    @Test
    public void readsConfigurationPropertiesFromSystemProperties() throws Exception {
        setSystemProperties(createTestProperties());

        ModuleScanner.modules.add(binder -> binder.bind(TestSystemPropertiesComponent.class));

        cheBootstrap.contextInitialized(new ServletContextEvent(servletContext));

        Injector injector = retrieveComponentFromServletContext(Injector.class);

        TestSystemPropertiesComponent testComponent = injector.getInstance(TestSystemPropertiesComponent.class);
        assertEquals(testComponent.parameter_pair, Pair.of("a", "b"));
        assertEquals(testComponent.parameter_pair2, Pair.of("a", (String)null));
        assertEquals(testComponent.parameter_pair3, Pair.of("a", ""));
        assertEquals(testComponent.parameter_pair_array, new Pair[]{Pair.of("a", "b"), Pair.of("c", "d")});
        assertEquals(testComponent.nullable, null);
        assertEquals(testComponent.parameter_uri, new URI("file:/a/b/c"));
        assertEquals(testComponent.parameter_url, new URL("http://localhost"));
        assertEquals(testComponent.parameter_file, new File("/a/b/c"));
        assertEquals(testComponent.parameter_strings, new String[]{"a", "b", "c"});
        assertEquals(testComponent.parameter_int, 123);
        assertEquals(testComponent.parameter_long, 123);
        assertEquals(testComponent.parameter_bool, true);
        assertEquals(testComponent.someDir, new File(System.getProperty("java.io.tmpdir"), "/some_dir"));
        assertEquals(testComponent.suffixedPath, System.getenv("PATH") + pathSeparator + "some_path");
    }

    @Test
    public void readsConfigurationPropertiesFromEnvProperties() throws Exception {
        ModuleScanner.modules.add(binder -> binder.bind(TestEnvPropertiesComponent.class));

        cheBootstrap.contextInitialized(new ServletContextEvent(servletContext));

        Injector injector = retrieveComponentFromServletContext(Injector.class);

        TestEnvPropertiesComponent testComponent = injector.getInstance(TestEnvPropertiesComponent.class);
        assertEquals(testComponent.string, System.getenv("PATH"));
    }

    @Test
    public void propertiesFromUserSpecifiedLocationOverrideCheProperties() throws Exception {
        systemPropertiesHelper.property(CHE_LOCAL_CONF_DIR, userCongDir.getAbsolutePath());

        Properties cheProperties = new Properties();
        cheProperties.put("che.some.name", "che_value");
        writePropertiesFile(che, "che.properties", cheProperties);

        Properties userProperties = new Properties();
        userProperties.put("che.some.name", "user_value");
        writePropertiesFile(userCongDir, "user.properties", userProperties);

        ModuleScanner.modules.add(binder -> binder.bind(TestConfOverrideComponent.class));

        cheBootstrap.contextInitialized(new ServletContextEvent(servletContext));

        Injector injector = retrieveComponentFromServletContext(Injector.class);

        TestConfOverrideComponent testComponent = injector.getInstance(TestConfOverrideComponent.class);
        assertEquals(testComponent.string, "user_value");
    }

    @Test
    public void system_properties_prefixed_with_che_dot_override_user_specified_and_che_properties() throws Exception {
        Properties cheProperties = new Properties();
        cheProperties.put("che.some.name", "che_value");
        cheProperties.put("che.some.other.name", "NULL");
        writePropertiesFile(che, "che.properties", cheProperties);

        Properties userProperties = new Properties();
        userProperties.put("che.some.name", "user_value");
        writePropertiesFile(userCongDir, "user.properties", userProperties);

        systemPropertiesHelper.property("che.some.name", "che_dot_system_property_value");

        ModuleScanner.modules.add(binder -> binder.bind(TestConfOverrideComponent.class));

        cheBootstrap.contextInitialized(new ServletContextEvent(servletContext));

        Injector injector = retrieveComponentFromServletContext(Injector.class);

        TestConfOverrideComponent testComponent = injector.getInstance(TestConfOverrideComponent.class);
        assertEquals(testComponent.string, "che_dot_system_property_value");
    }

    @Test
    public void environment_variables_prefixed_with_che_underscore_override_che_dot_prefixed_system_and_user_specified_and_che_properties() throws Exception {
        Properties cheProperties = new Properties();
        cheProperties.put("che.some.other.name", "che_value");
        cheProperties.put("che.some.name", "NULL");
        writePropertiesFile(che, "che.properties", cheProperties);

        Properties userProperties = new Properties();
        userProperties.put("che.some.other.name", "user_value");
        writePropertiesFile(userCongDir, "user.properties", userProperties);

        systemPropertiesHelper.property("che.some.other.name", "che_dot_system_property_value");

        ModuleScanner.modules.add(binder -> binder.bind(TestConfOverrideComponent.class));

        cheBootstrap.contextInitialized(new ServletContextEvent(servletContext));

        Injector injector = retrieveComponentFromServletContext(Injector.class);

        TestConfOverrideComponent testComponent = injector.getInstance(TestConfOverrideComponent.class);
        assertEquals(testComponent.otherString, System.getenv("CHE_SOME_OTHER_NAME"));
    }

    @Test
    public void environment_variables_prefixed_with_che_underscore_convert_double_underscores_into_one_underscore_in_variable_name()
            throws Exception {
        Properties cheProperties = new Properties();
        cheProperties.put("che.some.other.name_with_underscores", "che_value");
        cheProperties.put("che.some.name", "NULL");
        writePropertiesFile(che, "che.properties", cheProperties);

        Properties userProperties = new Properties();
        userProperties.put("che.some.other.name_with_underscores", "user_value");
        writePropertiesFile(userCongDir, "user.properties", userProperties);

        systemPropertiesHelper.property("che.some.other.name_with_underscores", "che_dot_system_property_value");

        ModuleScanner.modules.add(binder -> binder.bind(TestConfOverrideWithUnderscoresComponent.class));

        cheBootstrap.contextInitialized(new ServletContextEvent(servletContext));

        Injector injector = retrieveComponentFromServletContext(Injector.class);

        TestConfOverrideWithUnderscoresComponent testComponent =
                injector.getInstance(TestConfOverrideWithUnderscoresComponent.class);
        assertEquals(testComponent.otherString, System.getenv("CHE_SOME_OTHER_NAME__WITH__UNDERSCORES"));
    }

    @Test
    public void processesPropertyAliases() throws Exception {
        Properties cheProperties = new Properties();
        cheProperties.put("very.new.some.name", "some_value");
        writePropertiesFile(che, "che.properties", cheProperties);

        Properties aliases = new Properties();
        aliases.put("very.new.some.name", "new.some.name, che.some.name");
        writePropertiesFile(che.getParentFile(), PROPERTIES_ALIASES_CONFIG_FILE, aliases);

        ModuleScanner.modules.add(binder -> binder.bind(TestConfAliasComponent.class));

        cheBootstrap.contextInitialized(new ServletContextEvent(servletContext));

        Injector injector = retrieveComponentFromServletContext(Injector.class);

        TestConfAliasComponent testComponent = injector.getInstance(TestConfAliasComponent.class);
        assertEquals(testComponent.string, "some_value");
        assertEquals(testComponent.otherString, "some_value");
        assertEquals(testComponent.otherOtherString, "some_value");
    }

    static class TestChePropertiesComponent {
        @Named("test_int")
        @Inject
        int parameter_int;

        @Named("test_int")
        @Inject
        int parameter_long;

        @Named("test_bool")
        @Inject
        boolean parameter_bool;

        @Named("test_uri")
        @Inject
        URI parameter_uri;

        @Named("test_url")
        @Inject
        URL parameter_url;

        @Named("test_file")
        @Inject
        File parameter_file;

        @Named("test_strings")
        @Inject
        String[] parameter_strings;

        @Named("test_pair_of_strings")
        @Inject
        Pair<String, String> parameter_pair;

        @Named("test_pair_of_strings2")
        @Inject
        Pair<String, String> parameter_pair2;

        @Named("test_pair_of_strings3")
        @Inject
        Pair<String, String> parameter_pair3;

        @Named("test_pair_array")
        @Inject
        Pair<String, String>[] parameter_pair_array;

        @Named("some.dir.in_tmp_dir")
        @Inject
        File someDir;

        @Named("suffixed.PATH")
        @Inject
        String suffixedPath;

        @Named("nullable")
        @Inject
        @Nullable
        String nullable;
    }

    static class TestSystemPropertiesComponent {
        @Named("sys.test_int")
        @Inject
        int parameter_int;

        @Named("sys.test_int")
        @Inject
        int parameter_long;

        @Named("sys.test_bool")
        @Inject
        boolean parameter_bool;

        @Named("sys.test_uri")
        @Inject
        URI parameter_uri;

        @Named("sys.test_url")
        @Inject
        URL parameter_url;

        @Named("sys.test_file")
        @Inject
        File parameter_file;

        @Named("sys.test_strings")
        @Inject
        String[] parameter_strings;

        @Named("sys.test_pair_of_strings")
        @Inject
        Pair<String, String> parameter_pair;

        @Named("sys.test_pair_of_strings2")
        @Inject
        Pair<String, String> parameter_pair2;

        @Named("sys.test_pair_of_strings3")
        @Inject
        Pair<String, String> parameter_pair3;

        @Named("sys.test_pair_array")
        @Inject
        Pair<String, String>[] parameter_pair_array;

        @Named("sys.some.dir.in_tmp_dir")
        @Inject
        File someDir;

        @Named("sys.suffixed.PATH")
        @Inject
        String suffixedPath;

        @Named("sys.nullable")
        @Inject
        @Nullable
        String nullable;
    }

    static class TestEnvPropertiesComponent {
        @Named("env.PATH")
        @Inject
        String string;
    }

    static class TestConfOverrideComponent {
        @Named("che.some.name")
        @Inject
        @Nullable
        String string;

        @Named("che.some.other.name")
        @Inject
        @Nullable
        String otherString;
    }

    static class TestConfOverrideWithUnderscoresComponent {
        @Named("che.some.name")
        @Inject
        @Nullable
        String string;

        @Named("che.some.other.name_with_underscores")
        @Inject
        @Nullable
        String otherString;
    }

    static class TestConfAliasComponent {
        @Named("che.some.name")
        @Inject
        String string;

        @Named("new.some.name")
        @Inject
        String otherString;

        @Named("very.new.some.name")
        @Inject
        String otherOtherString;
    }

    private void writePropertiesFile(File parent, String name, Properties properties) throws IOException {
        File propertiesFile = new File(parent, name);
        try (Writer writer = Files.newWriter(propertiesFile, Charset.forName("UTF-8"))) {
            properties.store(writer, null);
        }
    }

    private void setSystemProperties(Properties properties) throws IOException {
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            systemPropertiesHelper.property((String)entry.getKey(), (String)entry.getValue());
        }
    }


    private void mockServletContext() {
        servletContext = mock(ServletContext.class);
        when(servletContext.getInitParameterNames()).thenReturn(emptyEnumeration());
        when(servletContext.getAttribute(Injector.class.getName())).thenAnswer(
                invocation -> retrieveComponentFromServletContext(Injector.class));
    }

    private <T> T retrieveComponentFromServletContext(Class<T> componentType) {
        ArgumentCaptor<T> argumentCaptor = ArgumentCaptor.forClass(componentType);
        verify(servletContext, atLeastOnce()).setAttribute(eq(componentType.getName()), argumentCaptor.capture());
        return argumentCaptor.getValue();
    }
}
