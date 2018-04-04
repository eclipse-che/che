/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
