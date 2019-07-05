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
package org.eclipse.che.plugin.zdb.ide.configuration;

import org.eclipse.che.ide.api.mvp.View;

/**
 * The view of {@link ZendDbgConfigurationPagePresenter}.
 *
 * @author Bartlomiej Laczkowski
 */
public interface ZendDbgConfigurationPageView
    extends View<ZendDbgConfigurationPageView.ActionDelegate> {

  /** Returns 'Break at first line' flag state. */
  boolean getBreakAtFirstLine();

  /**
   * Sets 'Break at first line' flag state.
   *
   * @param value
   */
  void setBreakAtFirstLine(boolean value);

  /**
   * Returns client host/IP.
   *
   * @return client host/IP
   */
  String getClientHostIP();

  /**
   * Sets client host/IP.
   *
   * @param value
   */
  void setClientHostIP(String value);

  /**
   * Returns debug port.
   *
   * @return debug port
   */
  int getDebugPort();

  /**
   * Sets debug port.
   *
   * @param value
   */
  void setDebugPort(int value);

  /**
   * Returns 'use ssl encryption' flag state.
   *
   * @return 'use ssl encryption' flag state
   */
  boolean getUseSslEncryption();

  /**
   * Sets 'use ssl encryption' flag state.
   *
   * @param value
   */
  void setUseSslEncryption(boolean value);

  /** Action handler for the view's controls. */
  interface ActionDelegate {

    /** Called when 'Break at first line' flag has been changed. */
    void onBreakAtFirstLineChanged(boolean value);

    /** Called when 'Client host/IP' has been changed. */
    void onClientHostIPChanged();

    /** Called when 'Debug Port' has been changed. */
    void onDebugPortChanged();

    /** Called when 'Use SSL encryption' flag has been changed. */
    void onUseSslEncryptionChanged(boolean value);
  }
}
