/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2013 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jface.resource;

/**
 * An image descriptor is an object that knows how to create an SWT image. It does not hold onto
 * images or cache them, but rather just creates them on demand. An image descriptor is intended to
 * be a lightweight representation of an image that can be manipulated even when no SWT display
 * exists.
 *
 * <p>This package defines a concrete image descriptor implementation which reads an image from a
 * file (<code>FileImageDescriptor</code>). It also provides abstract framework classes (this one
 * and <code>CompositeImageDescriptor</code>) which may be subclassed to define news kinds of image
 * descriptors.
 *
 * <p>Using this abstract class involves defining a concrete subclass and providing an
 * implementation for the <code>getImageData</code> method.
 *
 * <p>There are two ways to get an Image from an ImageDescriptor. The method createImage will always
 * return a new Image which must be disposed by the caller. Alternatively, createResource() returns
 * a shared Image. When the caller is done with an image obtained from createResource, they must
 * call destroyResource() rather than disposing the Image directly. The result of createResource()
 * can be safely cast to an Image.
 *
 * @see org.eclipse.swt.graphics.Image
 */
public class ImageDescriptor {
  private String image;
  private static final ImageDescriptor missingImageDescriptor = new ImageDescriptor("missing");

  public ImageDescriptor(String image) {
    this.image = image;
  }

  public static ImageDescriptor getMissingImageDescriptor() {
    return missingImageDescriptor;
  }

  public String getImage() {
    return image;
  }
}
