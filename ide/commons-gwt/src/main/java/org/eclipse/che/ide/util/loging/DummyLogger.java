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
package org.eclipse.che.ide.util.loging;

/**
 * @author <a href="mailto:evidolob@exoplatform.com">Evgen Vidolob</a>
 * @version $Id:
 */
class DummyLogger implements Logger {

  /** @see com.codenvy.ide.util.loging.Logger#debug(java.lang.Class, java.lang.Object[]) */
  @Override
  public void debug(Class<?> clazz, Object... args) {}

  /** @see com.codenvy.ide.util.loging.Logger#error(java.lang.Class, java.lang.Object[]) */
  @Override
  public void error(Class<?> clazz, Object... args) {}

  /** @see com.codenvy.ide.util.loging.Logger#info(java.lang.Class, java.lang.Object[]) */
  @Override
  public void info(Class<?> clazz, Object... args) {}

  /** @see com.codenvy.ide.util.loging.Logger#isLoggingEnabled() */
  @Override
  public boolean isLoggingEnabled() {
    return false;
  }

  /** @see com.codenvy.ide.util.loging.Logger#warn(java.lang.Class, java.lang.Object[]) */
  @Override
  public void warn(Class<?> clazz, Object... args) {}
}
