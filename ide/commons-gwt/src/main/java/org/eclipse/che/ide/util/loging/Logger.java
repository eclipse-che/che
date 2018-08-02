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
package org.eclipse.che.ide.util.loging;

/**
 * @author <a href="mailto:evidolob@exoplatform.com">Evgen Vidolob</a>
 * @version $Id:
 */
interface Logger {
  void debug(Class<?> clazz, Object... args);

  void error(Class<?> clazz, Object... args);

  void info(Class<?> clazz, Object... args);

  void warn(Class<?> clazz, Object... args);

  boolean isLoggingEnabled();
}
