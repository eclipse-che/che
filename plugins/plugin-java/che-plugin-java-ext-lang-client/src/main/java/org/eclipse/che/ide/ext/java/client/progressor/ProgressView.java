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

import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.ImplementedBy;
import org.eclipse.che.jdt.ls.extension.api.dto.ProgressReport;

/**
 * The widget which describes one progress from the progress monitor.
 *
 * @author Valeriy Svydenko
 */
@ImplementedBy(ProgressViewImpl.class)
public interface ProgressView extends IsWidget {
  /**
   * Change the value progress bar.
   *
   * @param progress information about progress
   */
  void updateProgressBar(ProgressReport progress);
}
