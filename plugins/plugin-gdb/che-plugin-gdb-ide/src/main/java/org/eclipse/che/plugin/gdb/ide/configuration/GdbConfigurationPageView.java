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
package org.eclipse.che.plugin.gdb.ide.configuration;

import java.util.Map;
import org.eclipse.che.ide.api.mvp.View;

/**
 * The view of {@link GdbConfigurationPagePresenter}.
 *
 * @author Artem Zatsarynnyi
 */
public interface GdbConfigurationPageView extends View<GdbConfigurationPageView.ActionDelegate> {

  /** Returns host. */
  String getHost();

  /** Sets host. */
  void setHost(String host);

  /** Returns port. */
  int getPort();

  /** Sets port. */
  void setPort(int port);

  /** Returns path to the binary. */
  String getBinaryPath();

  /** Sets path to the binary. */
  void setBinaryPath(String path);

  /** Sets 'dev machine' flag state. */
  void setDevHost(boolean value);

  /**
   * Sets the list of hosts to help user to choose an appropriate one.
   *
   * @param hosts the hosts list to set into the view
   */
  void setHostsList(Map<String, String> hosts);

  /** Sets enable state for host text box. */
  void setHostEnableState(boolean enable);

  /** Sets enable state for port text box. */
  void setPortEnableState(boolean enable);

  /** Action handler for the view's controls. */
  interface ActionDelegate {

    /** Called when 'Host' has been changed. */
    void onHostChanged();

    /** Called when 'Port' has been changed. */
    void onPortChanged();

    /** Called when 'Binary Path' has been changed. */
    void onBinaryPathChanged();

    /** Called when 'dev machine' flag has been changed. */
    void onDevHostChanged(boolean value);
  }
}
