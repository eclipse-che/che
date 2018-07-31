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
package org.eclipse.che.plugin.java.server;

/** */
public class CodeAssistantException extends Exception {
  /** */
  private static final long serialVersionUID = -2413708596186268688L;

  private int status;

  public CodeAssistantException(int status, String message) {
    super(message);
    this.setStatus(status);
  }

  public int getStatus() {
    return status;
  }

  /** @param status the status to set */
  public void setStatus(int status) {
    this.status = status;
  }
}
