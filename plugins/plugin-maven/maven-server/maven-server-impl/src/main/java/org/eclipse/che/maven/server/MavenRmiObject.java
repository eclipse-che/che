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
package org.eclipse.che.maven.server;

import org.eclipse.che.rmi.RmiObject;

/** @author Evgen Vidolob */
public class MavenRmiObject extends RmiObject {

  @Override
  protected boolean isWellKnownException(Throwable t) {
    return t.getClass().getName().startsWith(getClass().getPackage().getName());
  }

  public RuntimeException getRuntimeException(Throwable t) {
    Throwable wrapped = wrapException(t);
    if (wrapped instanceof RuntimeException) {
      return (RuntimeException) wrapped;
    } else {
      return new RuntimeException(wrapped);
    }
  }
}
