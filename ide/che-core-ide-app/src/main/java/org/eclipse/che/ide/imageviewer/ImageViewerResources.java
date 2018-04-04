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
