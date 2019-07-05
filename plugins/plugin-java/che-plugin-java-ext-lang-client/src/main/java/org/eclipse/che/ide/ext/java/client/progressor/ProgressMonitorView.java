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
package org.eclipse.che.ide.ext.java.client.progressor;

import com.google.inject.ImplementedBy;

/**
 * View of {@link ProgressMonitorPresenter}.
 *
 * @author Valeriy Svydenko
 */
@ImplementedBy(ProgressMonitorViewImpl.class)
public interface ProgressMonitorView {

  /** Shows the widget. */
  void showDialog();

  /** Hides the widget. */
  void close();

  /**
   * Adds the widget of new progress to the progress monitor.
   *
   * @param progressView widget of the progress
   */
  void add(ProgressView progressView);

  /**
   * Removes the widget of progress from the progress monitor.
   *
   * @param progressView widget of the progress
   */
  void remove(ProgressView progressView);
}
