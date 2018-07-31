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
package org.eclipse.che.commons.env;

import org.eclipse.che.commons.lang.concurrent.ThreadLocalPropagateContext;
import org.eclipse.che.commons.subject.Subject;

/**
 * Defines a component that holds variables of type {@link ThreadLocal} whose value is required by
 * the component to work normally and cannot be recovered. This component is mainly used when we
 * want to do a task asynchronously, in that case to ensure that the task will be executed in the
 * same conditions as if it would be executed synchronously we need to transfer the thread context
 * from the original thread to the executor thread.
 */
public class EnvironmentContext {

  /** ThreadLocal keeper for EnvironmentContext. */
  private static ThreadLocal<EnvironmentContext> current =
      ThreadLocal.withInitial(EnvironmentContext::new);

  static {
    ThreadLocalPropagateContext.addThreadLocal(current);
  }

  public static EnvironmentContext getCurrent() {
    return current.get();
  }

  public static void setCurrent(EnvironmentContext environment) {
    current.set(environment);
  }

  public static void reset() {
    current.remove();
  }

  private Subject subject;

  public EnvironmentContext() {}

  public EnvironmentContext(EnvironmentContext other) {
    setSubject(other.getSubject());
  }

  /** Returns subject or {@link Subject#ANONYMOUS} in case when subject is null. */
  public Subject getSubject() {
    return subject == null ? Subject.ANONYMOUS : subject;
  }

  /** Sets subject. */
  public void setSubject(Subject subject) {
    this.subject = subject;
  }
}
