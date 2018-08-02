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
package org.eclipse.che.api.debug.shared.model;

/** @author Anatoliy Bazko */
public interface Location {
  /** The target, e.g.: file, fqn, memory address etc. */
  String getTarget();

  /** The line number in a file or in a class. */
  int getLineNumber();

  /** Returns true if breakpoint resource is external resource, or false otherwise. */
  boolean isExternalResource();

  /** Returns external resource id in case if {@link #isExternalResource()} return true. */
  int getExternalResourceId();

  /** Returns project path, for resource which we are debugging now. */
  String getResourceProjectPath();

  /** Returns the method is being executed. */
  Method getMethod();

  /** Returns thread id. */
  long getThreadId();
}
