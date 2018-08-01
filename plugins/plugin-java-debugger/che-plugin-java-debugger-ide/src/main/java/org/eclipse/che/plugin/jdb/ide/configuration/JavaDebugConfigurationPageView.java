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
package org.eclipse.che.plugin.jdb.ide.configuration;

import java.util.List;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.util.Pair;

/**
 * The view of {@link JavaDebugConfigurationPagePresenter}.
 *
 * @author Artem Zatsarynnyi
 */
public interface JavaDebugConfigurationPageView
    extends View<JavaDebugConfigurationPageView.ActionDelegate> {

  /** Returns host. */
  String getHost();

  /** Sets host. */
  void setHost(String host);

  /** Returns port. */
  int getPort();

  /** Sets port. */
  void setPort(int port);

  void setHostEnableState(boolean enable);

  /**
   * Sets the list of ports to help user to choose an appropriate one.
   *
   * @param ports the ports list to set to the view
   */
  void setPortsList(List<Pair<String, String>> ports);

  /** Sets 'dev machine' flag state. */
  void setDevHost(boolean value);

  /** Action handler for the view's controls. */
  interface ActionDelegate {

    /** Called when 'Host' has been changed. */
    void onHostChanged();

    /** Called when 'Port' has been changed. */
    void onPortChanged();

    /** Called when 'dev machine' flag has been changed. */
    void onDevHostChanged(boolean value);
  }
}
