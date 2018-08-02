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
package org.eclipse.che.ide.ext.help.client.about;

import org.eclipse.che.ide.api.mvp.View;

/**
 * View for displaying About Codenvy information.
 *
 * @author Ann Shumilova
 */
public interface AboutView extends View<AboutView.ActionDelegate> {

  interface ActionDelegate {

    /** Performs any actions appropriate in response to the user having pressed the OK button */
    void onOkClicked();

    /** Shows build details information window. */
    void onShowBuildDetailsClicked();
  }

  /** Close view. */
  void close();

  /** Show About dialog. */
  void showDialog();

  /**
   * Set application's version value.
   *
   * @param version
   */
  void setVersion(String version);
}
