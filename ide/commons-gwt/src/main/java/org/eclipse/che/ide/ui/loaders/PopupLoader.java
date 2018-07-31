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
package org.eclipse.che.ide.ui.loaders;

/**
 * Loader interface
 *
 * @author Vitaliy Guliy
 */
public interface PopupLoader {

  /** Marks operation successful. */
  void setSuccess();

  /** Marks operation failed. */
  void setError();

  /** Shows a button to download logs. */
  void showDownloadButton();

  /**
   * Sets an action delegate to handle user actions.
   *
   * @param actionDelegate action delegate
   */
  void setDelegate(ActionDelegate actionDelegate);

  interface ActionDelegate {

    void onDownloadLogs();
  }
}
