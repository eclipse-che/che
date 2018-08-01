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

import com.google.inject.ImplementedBy;
import org.eclipse.che.ide.api.mvp.View;

/**
 * View of {@link ResolveDependencyPresenter}.
 *
 * @author Valeriy Svydenko
 */
@ImplementedBy(ResolveDependencyViewImpl.class)
public interface ResolveDependencyView extends View<ResolveDependencyView.ActionDelegate> {

  /** Shows the widget. */
  void showDialog();

  /** Hides the widget. */
  void close();

  /**
   * Set label into loader which describes current state of loader.
   *
   * @param text message of the status
   */
  void setOperationLabel(String text);

  /**
   * Change the value of resolved modules of the project.
   *
   * @param percentage value of resolved modules
   */
  void updateProgressBar(int percentage);

  interface ActionDelegate {}
}
