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
package org.eclipse.che.plugin.maven.client.comunnication.progressor;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Presenter of the window which describes information about resolving dependencies more detailed.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class ResolveDependencyPresenter implements ResolveDependencyView.ActionDelegate {

  private final ResolveDependencyView view;

  @Inject
  public ResolveDependencyPresenter(ResolveDependencyView view) {
    this.view = view;
  }

  /** Shows the widget. */
  public void show() {
    view.showDialog();
  }

  /**
   * Set label into loader which describes current state of loader.
   *
   * @param text message of the status
   */
  public void setProgressLabel(String text) {
    view.setOperationLabel(text);
  }

  /**
   * Change the value of resolved modules of the project.
   *
   * @param percentage value of resolved modules
   */
  public void updateProgressBar(int percentage) {
    view.updateProgressBar(percentage);
  }

  /** Hides the widget. */
  public void hide() {
    view.close();
  }
}
