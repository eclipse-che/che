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
package org.eclipse.che.inject.lifecycle;

import com.google.inject.ProvisionException;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/** @author andrew00x */
public final class InitModule extends LifecycleModule {
  private final Class<? extends Annotation> annotationType;

  public InitModule(Class<? extends Annotation> annotationType) {
    this.annotationType = annotationType;
  }

  @Override
  protected void configure() {
    bindListener(
        Matchers.any(),
        new TypeListener() {
          @Override
          public <T> void hear(TypeLiteral<T> type, TypeEncounter<T> encounter) {
            encounter.register(
                new InjectionListener<T>() {
                  @Override
                  public void afterInjection(T injectee) {
                    final Method[] methods = get(injectee.getClass(), annotationType);
                    if (methods.length > 0) {
                      for (Method method : methods) {
                        try {
                          method.invoke(injectee);
                        } catch (IllegalArgumentException e) {
                          // method MUST NOT have any parameters
                          throw new ProvisionException(e.getMessage(), e);
                        } catch (IllegalAccessException e) {
                          throw new ProvisionException(
                              String.format("Failed access to %s on %s", method, injectee), e);
                        } catch (InvocationTargetException e) {
                          final Throwable cause = e.getTargetException();
                          throw new ProvisionException(
                              String.format(
                                  "Invocation error of method %s on %s", method, injectee),
                              cause);
                        }
                      }
                    }
                  }
                });
          }
        });
  }
}
