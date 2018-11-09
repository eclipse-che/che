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
package org.eclipse.che.core.tracing;

import com.google.inject.Inject;
import io.opentracing.Scope;
import io.opentracing.Tracer;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.WeakHashMap;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.eclipse.che.commons.annotation.Traced;

public class TracingInterceptor implements MethodInterceptor {

  private Tracer tracer;
  private WeakHashMap<Class<?>, WeakHashMap<Method, String>> spanNames = new WeakHashMap<>();

  @Inject
  public void init(Tracer tracer) {
    this.tracer = tracer;
  }

  @Override
  public Object invoke(MethodInvocation invocation) throws Throwable {
    String spanName = getSpanName(invocation);
    try (Scope scope =
        tracer.buildSpan(spanName).asChildOf(tracer.activeSpan()).startActive(true)) {

      Traced.TagsStack.push();

      try {
        return invocation.proceed();
      } finally {
        for (Map.Entry<String, String> e : Traced.TagsStack.pop().entrySet()) {
          scope.span().setTag(e.getKey(), e.getValue());
        }
      }
    }
  }

  private String getSpanName(MethodInvocation invocation) {
    Class<?> objectType = invocation.getThis().getClass();
    Method method = invocation.getMethod();

    String ret = spanNames.computeIfAbsent(objectType, __ -> new WeakHashMap<>(4)).get(method);

    if (ret != null) {
      return ret;
    }

    Traced annotation = method.getAnnotation(Traced.class);

    if (annotation == null) {
      throw new IllegalStateException(
          "Misconfigured Guice interception. Tracing interceptor called on method "
              + method
              + " that is not annotated with @Traced.");
    }

    String name = annotation.name();

    if (name.isEmpty()) {
      name = cleanName(objectType) + "#" + method.getName();
    }

    spanNames.get(objectType).put(method, name);

    return name;
  }

  private static String cleanName(Class<?> type) {
    String simpleName = type.getSimpleName();

    // if there is '$$' in the class name, it is most probably a marker of a Guice or Hibernate
    // generated class... Let's just not pollute the name with generated subclass name garbage
    int $$idx = simpleName.indexOf("$$");
    if ($$idx > 0) {
      simpleName = simpleName.substring(0, $$idx);
    }

    return simpleName;
  }
}
