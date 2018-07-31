/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.inject.lifecycle;

import com.google.inject.ConfigurationException;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import com.google.inject.matcher.Matcher;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;
import com.google.inject.spi.InjectionListener;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import org.eclipse.che.commons.schedule.Launcher;
import org.eclipse.che.commons.schedule.ScheduleCron;
import org.eclipse.che.commons.schedule.ScheduleDelay;
import org.eclipse.che.commons.schedule.ScheduleRate;
import org.eclipse.che.commons.schedule.executor.LoggedRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listen guice injections and launch method marked with schedule annotations.
 *
 * @author Sergii Kabashniuk
 */
public class ScheduleInjectionListener<T> extends LifecycleModule implements InjectionListener<T> {
  private static final Logger LOG = LoggerFactory.getLogger(ScheduleInjectionListener.class);

  private final Matcher<AnnotatedElement> javaxSingleton =
      Matchers.annotatedWith(javax.inject.Singleton.class);
  private final Matcher<AnnotatedElement> googleSingleton =
      Matchers.annotatedWith(com.google.inject.Singleton.class);

  private final Provider<Launcher> launcherProvider;
  private final Provider<Injector> injectorProvider;

  public ScheduleInjectionListener(
      Provider<Launcher> launcherProvider, Provider<Injector> injectorProvider) {
    this.launcherProvider = launcherProvider;
    this.injectorProvider = injectorProvider;
  }

  private <T> T getValue(Class<T> configurationType, String configurationKey) {
    try {
      return injectorProvider
          .get()
          .getInstance(Key.get(configurationType, Names.named(configurationKey)));
    } catch (ConfigurationException | ProvisionException e) {
      return null;
    }
  }

  private long getValue(String configurationKey) {
    String stringValue = getValue(String.class, configurationKey);
    if (stringValue != null) {
      long result = Long.parseLong(stringValue);
      if (result == 0) {
        throw new RuntimeException("Invalid value 0 for parameter " + configurationKey);
      }
      return result;
    }
    Long longValue = getValue(Long.class, configurationKey);
    if (longValue != null) {
      long result = longValue.longValue();
      if (result == 0) {
        throw new RuntimeException("Invalid value 0 for parameter " + configurationKey);
      }
      return result;
    }
    Integer intValue = getValue(Integer.class, configurationKey);
    if (intValue != null) {
      long result = intValue.longValue();
      if (result == 0) {
        throw new RuntimeException("Invalid value 0 for parameter " + configurationKey);
      }
      return result;
    }
    throw new RuntimeException("Parameter " + configurationKey + " is not configured");
  }

  private void launch(Object object, Method method, ScheduleCron annotation) {
    Launcher launcher = launcherProvider.get();
    launcher.scheduleCron(
        new LoggedRunnable(object, method),
        annotation.cronParameterName().isEmpty()
            ? annotation.cron()
            : getValue(String.class, annotation.cronParameterName()));
  }

  private void launch(Object object, Method method, ScheduleDelay annotation) {
    if (annotation.delayParameterName().isEmpty() && annotation.delay() == 0) {
      throw new RuntimeException("Delay parameter is not configured");
    }

    Launcher launcher = launcherProvider.get();

    launcher.scheduleWithFixedDelay(
        new LoggedRunnable(object, method),
        annotation.initialDelayParameterName().isEmpty()
            ? annotation.initialDelay()
            : getValue(annotation.initialDelayParameterName()),
        annotation.delayParameterName().isEmpty()
            ? annotation.delay()
            : getValue(annotation.delayParameterName()),
        annotation.unit());
  }

  private void launch(Object object, Method method, ScheduleRate annotation) {
    if (annotation.periodParameterName().isEmpty() && annotation.period() == 0) {
      throw new RuntimeException("Period parameter is not configured");
    }

    Launcher launcher = launcherProvider.get();
    launcher.scheduleAtFixedRate(
        new LoggedRunnable(object, method),
        annotation.initialDelayParameterName().isEmpty()
            ? annotation.initialDelay()
            : getValue(annotation.initialDelayParameterName()),
        annotation.periodParameterName().isEmpty()
            ? annotation.period()
            : getValue(annotation.periodParameterName()),
        annotation.unit());
  }

  private void launch(Object object, Class<? extends Annotation> annotationType) {

    boolean isSingleton =
        javaxSingleton.matches(object.getClass()) || googleSingleton.matches(object.getClass());
    for (Method method : get(object.getClass(), annotationType)) {
      if (!isSingleton) {
        throw new RuntimeException(
            "Scheduled class " + object.getClass() + " should be marked as singleton");
      }
      if (annotationType.equals(ScheduleRate.class)) {
        launch(object, method, method.getAnnotation(ScheduleRate.class));
      } else if (annotationType.equals(ScheduleDelay.class)) {
        launch(object, method, method.getAnnotation(ScheduleDelay.class));
      } else if (annotationType.equals(ScheduleCron.class)) {
        launch(object, method, method.getAnnotation(ScheduleCron.class));
      }
    }
  }

  @Override
  public void afterInjection(T injectee) {
    launch(injectee, ScheduleCron.class);
    launch(injectee, ScheduleRate.class);
    launch(injectee, ScheduleDelay.class);
  }

  @Override
  protected void configure() {}
}
