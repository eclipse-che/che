/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */

package org.eclipse.che.ide.js.api.resources;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsType;

/** @author Yevhen Vydolob */
@JsType
public class VirtualFile {

  @JsIgnore private final org.eclipse.che.ide.api.resources.VirtualFile virtualFile;

  @JsIgnore
  public VirtualFile(org.eclipse.che.ide.api.resources.VirtualFile virtualFile) {
    this.virtualFile = virtualFile;
  }

  /**
   * Returns path for the virtual file. Path may in various representation based on implementation.
   * Usually it something like physical file or folder path, e.g. `/path/to/som/file`. Path should
   * always be non-null or non-empty.
   *
   * @return non-null unique path.
   */
  public String getLocation() {
    return virtualFile.getLocation().toString();
  }

  /**
   * Returns name for the virtual file. Name should always be non-null but it may be empty.
   *
   * @return non-null name.
   */
  public String getName() {
    return virtualFile.getName();
  }

  /**
   * Returns display name for the virtual file. Display name usually uses to display file name in
   * editor tab or other places. Value should not be a {@code null}.
   *
   * @return non-null display name.
   */
  public String getDisplayName() {
    return virtualFile.getDisplayName();
  }

  /**
   * Returns {@code true} in case if virtual file doesn't have ability to be updated.
   *
   * @return {@code true} if file is read only, otherwise false.
   */
  public boolean isReadOnly() {
    return virtualFile.isReadOnly();
  }

  /**
   * Returns url string where file content may be fetched. Some file type can't represent their
   * content as string. So virtual file provide url where it content. For example if this virtual
   * file represent image, image viewer may use this URL as src for {@link
   * com.google.gwt.user.client.ui.Image}.
   *
   * @return url or null if content url doesn't exists.
   */
  public String getContentUrl() {
    return virtualFile.getContentUrl();
  }

  //  /**
  //   * Get content of the file. Promise object should not be a null value. If implementation
  // doesn't
  //   * have ability to load string content of the current file, then preferable to throw {@code
  //   * UnsupportedOperationException} in promise.
  //   *
  //   * @return {@code Promise} with string representation of file content.
  //   */
  //  Promise<String> getContent();
  //
  //  /**
  //   * Update content of the file. Some implementations may not support updating their content, so
  // in
  //   * this case preferable to throw {@code UnsupportedOperationException} in promise.
  //   *
  //   * @param content new content of the file
  //   * @return {@code Promise} with success operation if content has been updated.
  //   */
  //  Promise<Void> updateContent(String content);
}
