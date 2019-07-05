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
 * Common Zend debug message interface.
 *
 * @author Bartlomiej Laczkowski
 */
public interface IDbgMessage {

  // UTF-8 only for now (preference in the future?)
  public static final String ENCODING = "UTF-8";

  /**
   * Return unique type id of this debug message
   *
   * @return message type
   */
  public int getType();

  /**
   * Sets the debug transfer encoding for this message
   *
   * @param String transfer encoding
   */
  public void setTransferEncoding(String encoding);

  /**
   * Returns current debug transfer encoding for this message
   *
   * @return String transfer encoding
   */
  public String getTransferEncoding();
}
