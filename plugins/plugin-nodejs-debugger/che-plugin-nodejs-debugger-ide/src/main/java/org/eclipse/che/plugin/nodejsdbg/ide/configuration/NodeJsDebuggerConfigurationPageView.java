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
package org.eclipse.che.plugin.nodejsdbg.ide.configuration;

import org.eclipse.che.ide.api.mvp.View;

/**
 * The view of {@link NodeJsDebuggerConfigurationPagePresenter}.
 *
 * @author Anatolii Bazko
 */
public interface NodeJsDebuggerConfigurationPageView
    extends View<NodeJsDebuggerConfigurationPageView.ActionDelegate> {

  /** Returns path to the binary. */
  String getScriptPath();

  /** Sets path to the binary. */
  void setScriptPath(String path);

  /** Action handler for the view's controls. */
  interface ActionDelegate {

    /** Called when 'Binary Path' has been changed. */
    void onScriptPathChanged();
  }
}
