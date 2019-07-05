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
 * Zend debug session settings container.
 *
 * @author Bartlomiej Laczkowski
 */
public class ZendDbgSettings {

  private final int debugPort;
  private final String clientHostIP;
  private final boolean breakAtFirstLine;
  private final boolean useSsslEncryption;

  public ZendDbgSettings(
      int debugPort, String clientHostIP, boolean breakAtFirstLine, boolean useSsslEncryption) {
    super();
    this.debugPort = debugPort;
    this.clientHostIP = clientHostIP;
    this.breakAtFirstLine = breakAtFirstLine;
    this.useSsslEncryption = useSsslEncryption;
  }

  /**
   * Returns debug port.
   *
   * @return debug port
   */
  public int getDebugPort() {
    return debugPort;
  }

  /**
   * Returns client host/IP.
   *
   * @return client host/IP
   */
  public String getClientHostIP() {
    return clientHostIP;
  }

  /**
   * Returns value of 'Break at first line' option.
   *
   * @return value of 'Break at first line' option
   */
  public boolean isBreakAtFirstLine() {
    return breakAtFirstLine;
  }

  /**
   * Returns value of 'Use SSL encoding' option.
   *
   * @return value of 'Use SSL encoding' option
   */
  public boolean isUseSsslEncryption() {
    return useSsslEncryption;
  }
}
