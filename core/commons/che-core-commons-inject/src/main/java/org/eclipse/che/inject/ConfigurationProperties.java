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
package org.eclipse.che.inject;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * Helper for getting set of configuration properties by name's pattern.
 *
 * <pre>{@code
 * public class Service {
 *     private Map<String, String> configuration;
 *     &#0064;Inject
 *     Service(ConfigurationProperties configurationProperties) {
 *         configuration = configurationProperties.getProperties("test.*");
 *     }
 *     ...
 * }
 * }</pre>
 *
 * @author andrew00x
 */
@Singleton
public class ConfigurationProperties {
  private final Provider<Injector> injectorProvider;

  @Inject
  ConfigurationProperties(Provider<Injector> injectorProvider) {
    this.injectorProvider = injectorProvider;
  }

  public Map<String, String> getProperties(String namePattern) {
    final Pattern pattern = Pattern.compile(namePattern);
    final Map<String, String> result = new HashMap<>();
    for (Map.Entry<Key<?>, Binding<?>> keyBindingEntry :
        injectorProvider.get().getAllBindings().entrySet()) {
      final Key<?> key = keyBindingEntry.getKey();
      final Annotation annotation = key.getAnnotation();
      if (annotation instanceof com.google.inject.name.Named
          && key.getTypeLiteral().getRawType() == String.class) {
        final String name = ((com.google.inject.name.Named) annotation).value();
        if (name != null && pattern.matcher(name).matches()) {
          final String value = (String) keyBindingEntry.getValue().getProvider().get();
          result.put(name, value);
        }
      }
    }
    return result;
  }
}
