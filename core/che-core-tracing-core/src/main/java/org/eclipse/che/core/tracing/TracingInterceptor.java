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
import java.util.WeakHashMap;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.eclipse.che.commons.annotation.Traced;

public class TracingInterceptor implements MethodInterceptor {

  private Tracer tracer;
  private WeakHashMap<Method, String> spanNames = new WeakHashMap<>();

  @Inject
  public void init(Tracer tracer) {
    this.tracer = tracer;
  }

  @Override
  public Object invoke(MethodInvocation invocation) throws Throwable {
    String spanName = getSpanName(invocation.getMethod());
    try (Scope ignored =
        tracer.buildSpan(spanName).asChildOf(tracer.activeSpan()).startActive(true)) {
      return invocation.proceed();
    }
  }

  private String getSpanName(Method method) {
    String ret = spanNames.get(method);

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
      name = method.getDeclaringClass().getCanonicalName() + "#" + method.getName();
    }

    spanNames.put(method, name);

    return name;
  }
}
