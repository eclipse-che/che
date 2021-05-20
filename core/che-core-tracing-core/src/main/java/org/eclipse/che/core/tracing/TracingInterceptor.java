/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
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

import com.google.common.annotations.Beta;
import com.google.inject.Inject;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Supplier;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.eclipse.che.commons.annotation.Traced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Guice interceptor that interprets the {@link Traced @Traced} annotations on methods and creates
 * tracing spans for the annotated method calls. It also captures the {@link Traced.Tags} and adds
 * them to the created spans.
 */
@Beta
public class TracingInterceptor implements MethodInterceptor {

  private static final Logger LOG = LoggerFactory.getLogger(TracingInterceptor.class);

  private Tracer tracer;
  private WeakHashMap<Class<?>, WeakHashMap<Method, String>> spanNames = new WeakHashMap<>();

  @Inject
  public void init(Tracer tracer) {
    this.tracer = tracer;
  }

  @Override
  public Object invoke(MethodInvocation invocation) throws Throwable {
    String spanName = getSpanName(invocation);

    Span span =
        tracer
            .buildSpan(spanName)
            .asChildOf(tracer.activeSpan())
            .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER)
            .start();
    try (Scope scope = tracer.scopeManager().activate(span)) {
      Traced.TagsStack.push();

      return invocation.proceed();
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      span.log(e.getMessage());
      throw e;
    } finally {
      for (Map.Entry<String, Supplier<?>> e : Traced.TagsStack.pop().entrySet()) {
        Object val;
        try {
          val = e.getValue().get();
        } catch (Exception ex) {
          if (LOG.isDebugEnabled()) {
            LOG.debug(
                "Could not get the value for a tag called {} when tracing {}.",
                e.getKey(),
                spanName,
                ex);
          }
          continue;
        }

        if (val instanceof String) {
          span.setTag(e.getKey(), (String) val);
        } else if (val instanceof Boolean) {
          span.setTag(e.getKey(), (Boolean) val);
        } else if (val instanceof Number) {
          span.setTag(e.getKey(), (Number) val);
        }
      }
      span.finish();
    }
  }

  private String getSpanName(MethodInvocation invocation) {
    Class<?> objectType = invocation.getThis().getClass();
    Method method = invocation.getMethod();

    // we assume that there won't be more than 4 traced methods on a type. If there are, we're
    // adding a little bit of runtime overhead of enlarging the hashmap's capacity, but in the usual
    // case we're saving 12 entries in the map (16 is the default capacity).
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
    int doubleDollarIdx = simpleName.indexOf("$$");
    if (doubleDollarIdx > 0) {
      simpleName = simpleName.substring(0, doubleDollarIdx);
    }

    return simpleName;
  }
}
