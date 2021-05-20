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
package org.eclipse.che.integration;

import static java.lang.String.format;

import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.google.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Named;
import org.eclipse.che.commons.schedule.ScheduleDelay;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.MethodParameterScanner;
import org.testng.Assert;
import org.testng.annotations.Test;

public class IntegrityConfigurationTest {
  static final Set<String> KNOWN_UNDECLARED_PROPERTIES = new HashSet<>();

  static {
    // created during war build time in buildinfo.properties
    KNOWN_UNDECLARED_PROPERTIES.add("che.product.build_info");
    // Defined in MachineAuthModule
    KNOWN_UNDECLARED_PROPERTIES.add("che.auth.signature_key_size");
    // Defined in KubernetesInfraModule
    KNOWN_UNDECLARED_PROPERTIES.add("kubernetesBasedEnvironments");
    // Defined in KubernetesInfraModule
    KNOWN_UNDECLARED_PROPERTIES.add("infra.kubernetes.ingress.annotations");
    // Defined in DocsModule
    KNOWN_UNDECLARED_PROPERTIES.add("che.json.ignored_classes");
    // Aliased to che.infra.kubernetes.trusted_ca.mount_path
    KNOWN_UNDECLARED_PROPERTIES.add("che.infra.openshift.trusted_ca_bundles_mount_path");
    // Defined in OpenShiftInfraModule
    KNOWN_UNDECLARED_PROPERTIES.add("multihost-exposer");
    // Defined in KubernetesInfraModule
    KNOWN_UNDECLARED_PROPERTIES.add("allowedEnvironmentTypeUpgrades");
    // Defined in WsMasterModule
    KNOWN_UNDECLARED_PROPERTIES.add("system.domain.actions");
    // Defined in ReplicationModule
    KNOWN_UNDECLARED_PROPERTIES.add("jgroups.config.file");
    // Defined in UserDevfileApiPermissionsModule and WorkspaceApiPermissionsModule
    KNOWN_UNDECLARED_PROPERTIES.add("system.super_privileged_domains");
    // Defined in KubernetesInfraModule
    KNOWN_UNDECLARED_PROPERTIES.add("kubernetesBasedComponents");
    // Defined in WsMasterModule
    KNOWN_UNDECLARED_PROPERTIES.add("che.agents.auth_enabled");
    // Defined in che server deployment.
    KNOWN_UNDECLARED_PROPERTIES.add("che.host");
    // Defined in MachineAuthModule
    KNOWN_UNDECLARED_PROPERTIES.add("che.auth.signature_key_algorithm");
    // Defined in che server deployment.
    KNOWN_UNDECLARED_PROPERTIES.add("env.KUBERNETES_NAMESPACE");
    // Defined in che server deployment.
    KNOWN_UNDECLARED_PROPERTIES.add("env.POD_NAMESPACE");
    // Defined in WsMasterModule
    KNOWN_UNDECLARED_PROPERTIES.add("jndi.datasource.name");
  }

  @Test
  public void shouldHaveNamedDefaults() {
    Reflections reflections =
        new Reflections(
            "org.eclipse.che", new MethodParameterScanner(), new FieldAnnotationsScanner());
    Map<String, String> configuration =
        getProperties(new File(Resources.getResource("che").getPath()));
    for (Constructor constructor : reflections.getConstructorsWithAnyParamAnnotated(Named.class)) {
      for (Parameter p : constructor.getParameters()) {
        if (p.isAnnotationPresent(Named.class)) {
          String key = p.getAnnotation(Named.class).value();
          Assert.assertTrue(
              configuration.containsKey(key) || KNOWN_UNDECLARED_PROPERTIES.contains(key),
              "constructor `"
                  + constructor.getName()
                  + "` do not have default value `"
                  + key
                  + "` for the parameter `"
                  + p.getName()
                  + "` in configuration");
        }
      }
    }
    for (Field field : reflections.getFieldsAnnotatedWith(Named.class)) {
      if (field.isAnnotationPresent(Named.class)) {
        if (field.isAnnotationPresent(Inject.class)
            && field.getAnnotation(Inject.class).optional()) {
          continue;
        }
        String key = field.getAnnotation(Named.class).value();
        Assert.assertTrue(
            configuration.containsKey(key) || KNOWN_UNDECLARED_PROPERTIES.contains(key),
            "Field `"
                + field.getName()
                + "` in class "
                + field.getDeclaringClass().getName()
                + " do not have default value `"
                + key
                + "` for the parameter ` in configuration");
      }
    }
  }

  @Test
  public void shouldNotDeclareUnused() {
    Reflections reflections =
        new Reflections(
            "org.eclipse.che",
            new MethodParameterScanner(),
            new FieldAnnotationsScanner(),
            new MethodAnnotationsScanner());
    Set<String> parameters = new HashSet<>();
    parameters.addAll(
        reflections
            .getConstructorsWithAnyParamAnnotated(Named.class)
            .stream()
            .flatMap(c -> Stream.of(c.getParameters()))
            .filter(p -> p.isAnnotationPresent(Named.class))
            .map(p -> p.getAnnotation(Named.class).value())
            .collect(Collectors.toSet()));

    parameters.addAll(
        reflections
            .getFieldsAnnotatedWith(Named.class)
            .stream()
            .filter(f -> f.isAnnotationPresent(Named.class))
            .map(f -> f.getAnnotation(Named.class).value())
            .collect(Collectors.toSet()));

    parameters.addAll(
        reflections
            .getMethodsAnnotatedWith(ScheduleDelay.class)
            .stream()
            .filter(m -> m.isAnnotationPresent(ScheduleDelay.class))
            .map(m -> m.getAnnotation(ScheduleDelay.class).delayParameterName())
            .collect(Collectors.toSet()));
    parameters.addAll(
        reflections
            .getMethodsAnnotatedWith(ScheduleDelay.class)
            .stream()
            .filter(m -> m.isAnnotationPresent(ScheduleDelay.class))
            .map(m -> m.getAnnotation(ScheduleDelay.class).initialDelayParameterName())
            .collect(Collectors.toSet()));

    Set<String> unusedDeclaredConfigurationParameters =
        getProperties(new File(Resources.getResource("che").getPath()))
            .keySet()
            .stream()
            .filter(k -> !parameters.contains(k))
            .collect(Collectors.toSet());
    Assert.assertTrue(
        unusedDeclaredConfigurationParameters.isEmpty(),
        "Parameters declared but not used. " + unusedDeclaredConfigurationParameters);
  }

  @Test
  public void shouldNotHaveGuiceNamedDefaults() {
    Reflections reflections = new Reflections("org.eclipse.che", new MethodParameterScanner());
    Set<Constructor> guiceNamedClasses =
        reflections.getConstructorsWithAnyParamAnnotated(com.google.inject.name.Named.class);
    Assert.assertTrue(
        guiceNamedClasses.isEmpty(),
        "Found classes that uses com.google.inject.name.Named annotation instead of javax.inject.Named");
  }

  protected Map<String, String> getProperties(File confDir) {
    Map<String, String> values = new HashMap<>();
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
            for (final String name : properties.stringPropertyNames())
              values.put(name, properties.getProperty(name));
          }
        }
      }
    }
    return values;
  }
}
