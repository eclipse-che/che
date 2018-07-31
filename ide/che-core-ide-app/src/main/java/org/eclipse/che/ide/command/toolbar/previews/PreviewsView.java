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
package org.eclipse.che.ide.command.toolbar.previews;

import org.eclipse.che.ide.api.mvp.View;

/** View for displaying the preview URLs. */
public interface PreviewsView extends View<PreviewsView.ActionDelegate> {

  /** Add preview URL to the view. */
  void addUrl(String previewUrl);

  /** Remove preview URL from the view. */
  void removeUrl(String previewUrl);

  /** Remove all preview URLs from the view. */
  void removeAllURLs();

  interface ActionDelegate {

    /** Called when preview URL has been chosen. */
    void onUrlChosen(String previewUrl);
  }
}
