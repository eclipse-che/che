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

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;

/** Resources for the image viewer. */
public interface ImageViewerResources extends ClientBundle {

  @Source({"imageViewer.css", "org/eclipse/che/ide/api/ui/style.css"})
  Css imageViewerCss();

  /** Image viewer backgroupd image. */
  @Source("image-viewer-bg.png")
  ImageResource imageViewerBackground();

  /** CssResource for the image viewer. */
  public interface Css extends CssResource {
    /** The style for the image viewer. */
    String imageViewer();
  }
}
