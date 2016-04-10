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

import com.google.inject.*;
import com.google.inject.name.Names;
import com.google.inject.util.Modules;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.lang.Pair;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import javax.inject.Named;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Properties;

/**
 * Tests injection of configuration parameters.
 *
 * @author andrew00x
 */
public class ConfigurationTest {

    Injector injector;

    @BeforeTest
    public void init() {
        final Properties props = new Properties();
        props.put("test_int", "123");
        props.put("test_bool", "true");
        props.put("test_uri", "file:/a/b/c");
        props.put("test_url", "http://localhost");
        props.put("test_file", "/a/b/c");
        props.put("test_strings", "a, b, c");
        props.put("test_pair_of_strings", "a=b");
        props.put("test_pair_of_strings2", "a");
        props.put("test_pair_of_strings3", "a=");
        props.put("test_pair_array", "a=b,c=d");
        props.put("some.dir.in_tmp_dir", "${java.io.tmpdir}/some_dir");
        props.put("suffixed.PATH", "${PATH}" + java.io.File.pathSeparator + "some_path");
        props.put("nullable", "NULL");
        injector = Guice.createInjector(
                new URIConverter(),
                new URLConverter(),
                new FileConverter(),
                new StringArrayConverter(),
                new PairConverter(),
                new PairArrayConverter(),
                new CodenvyBootstrap.ExtConfiguration(),
                new CodenvyBootstrap.AbstractConfigurationModule() {
                    @Override
                    protected void configure() {
                        bindProperties(props);
                    }
                },
                new MyModule());
    }

    @Test
    public void testNullable() {
        Assert.assertEquals(injector.getInstance(TestComponent.class).nullable, null);
    }

    @Test
    public void testConvertInt() {
        Assert.assertEquals(injector.getInstance(TestComponent.class).parameter_int, 123);
    }

    @Test
    public void testConvertLong() {
        Assert.assertEquals(injector.getInstance(TestComponent.class).parameter_long, 123);
    }

    @Test
    public void testConvertBoolean() {
        Assert.assertEquals(injector.getInstance(TestComponent.class).parameter_bool, true);
    }

    @Test
    public void testConvertUri() {
        Assert.assertEquals(injector.getInstance(TestComponent.class).parameter_uri, URI.create("file:/a/b/c"));
    }

    @Test
    public void testConvertUrl() throws MalformedURLException {
        Assert.assertEquals(injector.getInstance(TestComponent.class).parameter_url, new URL("http://localhost"));
    }

    @Test
    public void testConvertFile() {
        Assert.assertEquals(injector.getInstance(TestComponent.class).parameter_file, new java.io.File("/a/b/c"));
    }

    @Test
    public void testConvertStrings() {
        Assert.assertEquals(injector.getInstance(TestComponent.class).parameter_strings, new String[]{"a", "b", "c"});
    }

    @Test
    public void testConvertPairOfStrings() {
        Assert.assertEquals(injector.getInstance(TestComponent.class).parameter_pair, Pair.of("a", "b"));
    }

    @Test
    public void testConvertPairOfStrings2() {
        Assert.assertEquals(injector.getInstance(TestComponent.class).parameter_pair2, Pair.of("a", (String)null));
    }

    @Test
    public void testConvertPairOfStrings3() {
        Assert.assertEquals(injector.getInstance(TestComponent.class).parameter_pair3, Pair.of("a", ""));
    }

    @Test
    public void testConvertPairArray() {
        Assert.assertEquals(injector.getInstance(TestComponent.class).parameter_pair_array,
                            new Pair[]{Pair.of("a", "b"), Pair.of("c", "d")});
    }

    @Test
    public void testGetSystemProperty() {
        Assert.assertEquals(injector.getInstance(TestComponent.class).tmpDir, new java.io.File(System.getProperty("java.io.tmpdir")));
    }

    @Test
    public void testGetEnvironmentVariable() {
        Assert.assertEquals(injector.getInstance(TestComponent.class).path, System.getenv("PATH"));
    }

    @Test
    public void testInjectSystemPropertyInConfiguration() {
        Assert.assertEquals(injector.getInstance(TestComponent.class).someDir,
                            new java.io.File(System.getProperty("java.io.tmpdir"), "/some_dir"));
    }

    @Test
    public void testInjectEnvironmentVariableInConfiguration() {
        Assert.assertEquals(injector.getInstance(TestComponent.class).suffixedPath,
                            System.getenv("PATH") + java.io.File.pathSeparator + "some_path");
    }

    public static class MyModule implements Module {
        @Override
        public void configure(Binder binder) {
            binder.bind(TestComponent.class);
        }
    }

    public static class TestComponent {
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
        java.io.File parameter_file;

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

        @Named("sys.java.io.tmpdir")
        @Inject
        java.io.File tmpDir;

        @Named("env.PATH")
        @Inject
        String path;

        @Named("some.dir.in_tmp_dir")
        @Inject
        java.io.File someDir;

        @Named("suffixed.PATH")
        @Inject
        String suffixedPath;

        @Named("nullable")
        @Inject
        @Nullable
        String nullable;
    }

    @Test
    public void testConfigurationOverride() {
        final Properties props1 = new Properties();
        props1.put("test_a", "bar");
        props1.put("test_b", "foo");
        Module module1 = new CodenvyBootstrap.AbstractConfigurationModule() {
            @Override
            protected void configure() {
                bindProperties(props1);
            }
        };

        final Properties props2 = new Properties();
        props2.put("test_a", "overridden bar");
        Module module2 = new CodenvyBootstrap.AbstractConfigurationModule() {
            @Override
            protected void configure() {
                bindProperties(props2);
            }
        };

        Injector myInjector = Guice.createInjector(Modules.override(module1).with(module2));
        Assert.assertEquals(myInjector.getBinding(Key.get(String.class, Names.named("test_a"))).getProvider().get(), "overridden bar");
        Assert.assertEquals(myInjector.getBinding(Key.get(String.class, Names.named("test_b"))).getProvider().get(), "foo");
    }
}
