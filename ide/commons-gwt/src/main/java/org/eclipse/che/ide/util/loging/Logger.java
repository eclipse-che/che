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
