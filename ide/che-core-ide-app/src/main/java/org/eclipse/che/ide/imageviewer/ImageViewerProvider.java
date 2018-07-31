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
package org.eclipse.che.ide.imageviewer;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.EditorProvider;

/**
 * Provider for image editor(only displaying images).
 *
 * @author Ann Shumilova
 */
public class ImageViewerProvider implements EditorProvider {
  private Provider<ImageViewer> editorProvider;

  @Inject
  public ImageViewerProvider(Provider<ImageViewer> editorProvider) {
    super();
    this.editorProvider = editorProvider;
  }

  @Override
  public String getId() {
    return "codenvyImageViewer";
  }

  @Override
  public String getDescription() {
    return "Codenvy Image Viewer";
  }

  @Override
  public EditorPartPresenter getEditor() {
    return editorProvider.get();
  }
}
