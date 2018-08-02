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
package org.eclipse.che.providers;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.io.Resources;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.stringtemplate.v4.ST;

/**
 * Generate implementation for {@link DynaProvider}. Find all types annotated with {@link
 * DynaObject} and add {@link com.google.inject.Provider} for them to {@link DynaProvider}
 * implementation.
 *
 * @author Evgen Vidolob
 */
public class DynaProviderGenerator {

  private static final String TEMPLATE_PATH =
      "/"
          .concat(DynaProviderGenerator.class.getPackage().getName().replace(".", "/"))
          .concat("/DynaProvider.st");

  /** String template instance used */
  private ST st;

  private final String packageName;
  private final String className;
  private final List<String> classpath;
  private List<ClassModel> dynaClasses;

  public DynaProviderGenerator(String packageName, String className, List<String> classpath) {
    this.packageName = packageName;
    this.className = className;
    this.classpath = classpath;
  }

  public String generate() throws IOException {
    dynaClasses = new ArrayList<>();
    findDynaObjects();

    ST st = getTemplate();
    st.add("packageName", packageName);
    st.add("className", className);
    st.add("classes", dynaClasses);
    return st.render();
  }

  private void findDynaObjects() throws IOException {
    ConfigurationBuilder configuration = new ConfigurationBuilder();

    List<URL> urls = new ArrayList<>();
    for (String element : classpath) {
      urls.add(new File(element).toURI().toURL());
    }

    ClassLoader contextClassLoader =
        URLClassLoader.newInstance(
            urls.toArray(new URL[urls.size()]), Thread.currentThread().getContextClassLoader());
    Thread.currentThread().setContextClassLoader(contextClassLoader);
    configuration.setUrls(ClasspathHelper.forClassLoader(contextClassLoader));
    configuration.setScanners(new SubTypesScanner(), new TypeAnnotationsScanner());
    Reflections reflection = new Reflections(configuration);

    Set<Class<?>> classes = reflection.getTypesAnnotatedWith(DynaObject.class);
    for (Class clazz : classes) {
      // accept only classes
      if (clazz.isEnum() || clazz.isInterface() || clazz.isAnnotation()) {
        continue;
      }
      dynaClasses.add(new ClassModel(clazz));
      System.out.println(String.format("New Dyna Object Found: %s", clazz.getCanonicalName()));
    }
    System.out.println(String.format("Found: %d Dyna Objects", dynaClasses.size()));
  }

  /**
   * Get the template for provider
   *
   * @return the String Template
   */
  protected ST getTemplate() {
    if (st == null) {
      URL url = Resources.getResource(DynaProviderGenerator.class, TEMPLATE_PATH);
      try {
        st = new ST(Resources.toString(url, UTF_8));
      } catch (IOException e) {
        throw new IllegalArgumentException("Unable to read template", e);
      }
    }
    return st;
  }
}
