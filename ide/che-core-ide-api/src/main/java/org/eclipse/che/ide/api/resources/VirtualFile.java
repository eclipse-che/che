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
package org.eclipse.che.ide.api.resources;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.resource.Path;

/**
 * Represents a file on the client side. Note, that virtual file instances are created by request,
 * so in this case there can be several instances of virtual file that corresponds to the same file.
 *
 * <p>Virtual file may has link to related {@code ProjectConfig}.
 *
 * @author Evgen Vidolob
 * @author Vlad Zhukovskyi
 */
public interface VirtualFile {

  /**
   * Returns path for the virtual file. Path may in various representation based on implementation.
   * Usually it something like physical file or folder path, e.g. `/path/to/som/file`. Path should
   * always be non-null or non-empty.
   *
   * @return non-null unique path.
   * @since 4.4.0
   */
  Path getLocation();

  /**
   * Returns name for the virtual file. Name should always be non-null but it may be empty.
   *
   * @return non-null name.
   */
  String getName();

  /**
   * Returns display name for the virtual file. Display name usually uses to display file name in
   * editor tab or other places. Value should not be a {@code null}.
   *
   * @return non-null display name.
   */
  String getDisplayName();

  /**
   * Returns {@code true} in case if virtual file doesn't have ability to be updated.
   *
   * @return {@code true} if file is read only, otherwise false.
   */
  boolean isReadOnly();

  /**
   * Returns url string where file content may be fetched. Some file type can't represent their
   * content as string. So virtual file provide url where it content. For example if this virtual
   * file represent image, image viewer may use this URL as src for {@link
   * com.google.gwt.user.client.ui.Image}.
   *
   * @return url or null if content url doesn't exist.
   */
  String getContentUrl();

  /**
   * Get content of the file. Promise object should not be a null value. If implementation doesn't
   * have ability to load string content of the current file, then preferable to throw {@code
   * UnsupportedOperationException} in promise.
   *
   * @return {@code Promise} with string representation of file content.
   */
  Promise<String> getContent();

  /**
   * Update content of the file. Some implementations may not support updating their content, so in
   * this case preferable to throw {@code UnsupportedOperationException} in promise.
   *
   * @param content new content of the file
   * @return {@code Promise} with success operation if content has been updated.
   */
  Promise<Void> updateContent(String content);
}
