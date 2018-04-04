/**
 * ***************************************************************************** Copyright (c)
 * 2012-2015 Red Hat, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Red Hat, Inc. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.viewsupport;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

/** @author Evgen Vidolob */
public class ImageDescriptorRegistry {
  private Map<ImageDescriptor, Image> fRegistry =
      Collections.synchronizedMap(new HashMap<ImageDescriptor, Image>(10));

  /**
   * Returns the image associated with the given image descriptor.
   *
   * @param descriptor the image descriptor for which the registry manages an image, or <code>null
   *     </code> for a missing image descriptor
   * @return the image associated with the image descriptor or <code>null</code> if the image
   *     descriptor can't create the requested image.
   */
  public Image get(ImageDescriptor descriptor) {
    if (descriptor == null) descriptor = ImageDescriptor.getMissingImageDescriptor();

    Image result = fRegistry.get(descriptor);
    if (result != null) return result;

    result = new Image(descriptor);
    if (result != null) fRegistry.put(descriptor, result);
    return result;
  }
}
