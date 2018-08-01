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
package org.eclipse.che.selenium.core.pageobject;

import static java.lang.String.format;

import com.google.inject.Inject;
import com.google.inject.Injector;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Injects fields annotated with {@link InjectPageObject}. All page objects with the same {@link
 * InjectPageObject#driverId()} must share a common {@link SeleniumWebDriver} instance.
 *
 * @author Anatolii Bazko
 */
public abstract class PageObjectsInjector {

  private static final Logger LOG = LoggerFactory.getLogger(PageObjectsInjector.class);

  @Inject private SeleniumWebDriverFactory seleniumWebDriverFactory;

  public void injectMembers(Object testInstance, Injector injector) throws Exception {
    Map<Integer, Set<Field>> toInject = collectFieldsToInject(testInstance);

    for (Integer poIndex : toInject.keySet()) {
      Map<Class<?>, Object> container = new HashMap<>();
      SeleniumWebDriver seleniumWebDriver = seleniumWebDriverFactory.create();

      container.put(SeleniumWebDriver.class, seleniumWebDriver);
      container.putAll(getDependenciesWithWebdriver(seleniumWebDriver));

      for (Field f : toInject.get(poIndex)) {
        try {
          injectField(f, testInstance, container, injector);
        } catch (Exception e) {
          throw new RuntimeException(
              format("Error of injection member '%s' into test '%s'.", f, testInstance), e);
        }
      }
    }
  }

  public abstract Map<Class<?>, Object> getDependenciesWithWebdriver(
      SeleniumWebDriver seleniumWebDriver);

  private void injectField(
      Field field, Object instance, Map<Class<?>, Object> container, Injector injector)
      throws Exception {
    Object object = instantiate(field.getType(), container, injector);
    field.setAccessible(true);
    field.set(instance, object);
  }

  private Object instantiate(Class<?> type, Map<Class<?>, Object> container, Injector injector)
      throws Exception {
    Object obj;

    Optional<Constructor<?>> constructor = findConstructor(type);
    if (!constructor.isPresent()) {
      // interface? get instance from a guice container
      obj = injector.getInstance(type);

    } else {
      Class<?>[] parameterTypes = constructor.get().getParameterTypes();
      Object[] params = new Object[parameterTypes.length];

      for (int i = 0; i < parameterTypes.length; i++) {
        Object pt = container.get(parameterTypes[i]);
        if (pt == null) {
          pt = instantiate(parameterTypes[i], container, injector);
        }
        params[i] = pt;
      }

      obj = constructor.get().newInstance(params);
    }

    container.put(obj.getClass(), obj);
    return obj;
  }

  @Nullable
  private Optional<Constructor<?>> findConstructor(Class<?> type) {
    return Stream.of(type.getConstructors())
        .filter(
            c ->
                c.isAnnotationPresent(com.google.inject.Inject.class)
                    || c.isAnnotationPresent(javax.inject.Inject.class))
        .findAny();
  }

  /** Find class fields annotated with {@link InjectPageObject}. */
  private Map<Integer, Set<Field>> collectFieldsToInject(Object testInstance) {
    Map<Integer, Set<Field>> toInject = new HashMap<>();

    for (Field f : testInstance.getClass().getDeclaredFields()) {
      if (f.isAnnotationPresent(InjectPageObject.class)) {
        InjectPageObject pageObject = f.getAnnotation(InjectPageObject.class);
        int poIndex = pageObject.driverId();

        Set<Field> fields = toInject.computeIfAbsent(poIndex, k -> new HashSet<>());
        fields.add(f);
      }
    }

    return toInject;
  }
}
