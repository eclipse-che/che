/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2013 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.swt.graphics;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Instances of this class are graphics which have been prepared for display on a specific device.
 * That is, they are ready to paint using methods such as <code>GC.drawImage()</code> and display on
 * widgets with, for example, <code>Button.setImage()</code>.
 *
 * <p>If loaded from a file format that supports it, an <code>Image</code> may have transparency,
 * meaning that certain pixels are specified as being transparent when drawn. Examples of file
 * formats that support transparency are GIF and PNG.
 *
 * <p>There are two primary ways to use <code>Images</code>. The first is to load a graphic file
 * from disk and create an <code>Image</code> from it. This is done using an <code>Image</code>
 * constructor, for example:
 *
 * <pre>
 *    Image i = new Image(device, "C:\\graphic.bmp");
 * </pre>
 *
 * A graphic file may contain a color table specifying which colors the image was intended to
 * possess. In the above example, these colors will be mapped to the closest available color in SWT.
 * It is possible to get more control over the mapping of colors as the image is being created,
 * using code of the form:
 *
 * <pre>
 *    ImageData data = new ImageData("C:\\graphic.bmp");
 *    RGB[] rgbs = data.getRGBs();
 *    // At this point, rgbs contains specifications of all
 *    // the colors contained within this image. You may
 *    // allocate as many of these colors as you wish by
 *    // using the Color constructor Color(RGB), then
 *    // create the image:
 *    Image i = new Image(device, data);
 * </pre>
 *
 * <p>Applications which require even greater control over the image loading process should use the
 * support provided in class <code>ImageLoader</code>.
 *
 * <p>Application code must explicitly invoke the <code>Image.dispose()</code> method to release the
 * operating system resources managed by each instance when those instances are no longer required.
 *
 * @see Color
 * @see ImageData
 * @see ImageLoader
 * @see <a href="http://www.eclipse.org/swt/snippets/#image">Image snippets</a>
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Examples: GraphicsExample,
 *     ImageAnalyzer</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 */
public class Image {
  private final String img;

  public Image(ImageDescriptor key) {
    img = key.getImage();
  }

  public String getImg() {
    return img;
  }
}
