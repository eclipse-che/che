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
