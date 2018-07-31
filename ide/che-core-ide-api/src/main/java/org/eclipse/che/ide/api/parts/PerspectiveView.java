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
package org.eclipse.che.ide.api.parts;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import org.eclipse.che.ide.api.mvp.View;

/**
 * Perspective View contains abstract containers for PartStack
 *
 * @author Nikolay Zamosenchuk
 */
public interface PerspectiveView<T> extends View<T> {
  /**
   * Returns central panel.
   *
   * @return
   */
  AcceptsOneWidget getEditorPanel();

  /**
   * Returns left panel.
   *
   * @return
   */
  AcceptsOneWidget getNavigationPanel();

  /**
   * Returns bottom panel.
   *
   * @return
   */
  AcceptsOneWidget getInformationPanel();

  /**
   * Returns right panel.
   *
   * @return
   */
  AcceptsOneWidget getToolPanel();

  /** Handle View events */
  interface ActionDelegate {

    void onResize(int width, int height);
  }
}
