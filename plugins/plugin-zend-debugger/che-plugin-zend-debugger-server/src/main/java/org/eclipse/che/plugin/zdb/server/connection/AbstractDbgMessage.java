/*
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.zdb.server.connection;

/**
 * Abstract Zend debug message (common for client and engine messages).
 *
 * @author Bartlomiej Laczkowski
 */
public abstract class AbstractDbgMessage implements IDbgMessage {

  @Override
  public String getTransferEncoding() {
    return ENCODING;
  }

  @Override
  public void setTransferEncoding(String encoding) {
    // TODO - support user preferred encoding
  }

  @Override
  public String toString() {
    return new StringBuilder(this.getClass().getSimpleName())
        .append(" [ID=")
        .append(getType())
        .append(']')
        .toString();
  }
}
