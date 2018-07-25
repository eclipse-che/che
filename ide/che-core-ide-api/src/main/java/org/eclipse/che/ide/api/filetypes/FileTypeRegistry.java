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
package org.eclipse.che.ide.api.filetypes;

import java.util.List;
import org.eclipse.che.ide.api.resources.VirtualFile;

/**
 * Registry allows to register new {@link FileType} and get the registered one.
 *
 * @author Artem Zatsarynnyi
 */
public interface FileTypeRegistry {
  /**
   * Register the specified file type.
   *
   * @param fileType file type to register
   */
  void registerFileType(FileType fileType);

  /**
   * Returns the {@link List} of all registered file types.
   *
   * @return {@link List} of all registered file types
   */
  List<FileType> getRegisteredFileTypes();

  /**
   * Returns the file type of the specified file.
   *
   * @param file file for which type need to find
   * @return file type or default file type if no file type found
   */
  FileType getFileTypeByFile(VirtualFile file);

  /**
   * Returns the file type for the specified file extension.
   *
   * @param extension extension for which file type need to find
   * @return file type or default file type if no file type found
   */
  FileType getFileTypeByExtension(String extension);

  /**
   * Returns the file type which pattern matches the specified file name.
   *
   * @param name file name
   * @return file type or default file type if no file type's name pattern matches the given file
   *     name
   */
  FileType getFileTypeByFileName(String name);
}
