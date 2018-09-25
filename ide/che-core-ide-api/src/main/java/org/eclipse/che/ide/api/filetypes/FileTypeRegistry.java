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
package org.eclipse.che.ide.api.filetypes;

import java.util.List;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Registry allows to register new {@link FileType} and get the registered one.
 *
 * @author Artem Zatsarynnyi
 */
public interface FileTypeRegistry {

  /** Provider allows to get registered {@link FileType} and avoid File Type collision. */
  interface FileTypeProvider {
    /**
     * Allows to get {@link FileType} by extension and name pattern.
     *
     * <p>Returns:
     * <li>merged File Type if the File Type Registry contains type which match given {@code
     *     extension} and {@code namePattern}
     * <li>newly created and registered File Type if the File Type Registry does not contain
     *     corresponding type to merge
     *
     * @param image associated with the File Type image, optional parameter: default image will be
     *     used when {@code image} is not provided
     * @param extension associated with the File Type extension, mandatory parameter: {@link
     *     IllegalArgumentException} will be thrown when {@code extension} is not provided
     * @param namePattern name pattern describing the File Type, optional parameter
     * @throws IllegalArgumentException will be thrown when {@code extension} is not provided
     * @return registered File Type
     */
    FileType get(
        @Nullable SVGResource image, @NotNull String extension, @Nullable String namePattern);

    /**
     * Allows to get {@link FileType} by extension.
     *
     * <p>Returns:
     * <li>existing File Type if the File Type Registry contains type with such extension
     * <li>newly created and registered File Type if the File Type Registry does not contain type
     *     with such extension
     *
     * @param image associated with the File Type image, optional parameter: default image will be
     *     used when {@code image} is not provided
     * @param extension associated with the File Type extension, mandatory parameter: {@link
     *     IllegalArgumentException} will be thrown when {@code extension} is not provided
     * @throws IllegalArgumentException will be thrown when {@code extension} is not provided
     * @return registered File Type
     */
    FileType getByExtension(@Nullable SVGResource image, @NotNull String extension);

    /**
     * Allows to get the set of {@link FileType}s which match given {@code namePattern}.
     *
     * <p>Returns:
     * <li>set of existing File Types if the File Type Registry contains types which match given
     *     {@code namePattern}
     * <li>newly created and registered by name pattern File Type if the File Type Registry does not
     *     contain types which match given {@code namePattern}
     *
     * @param image associated with the File Type image, optional parameter: default image will be
     *     used when {@code image} is not provided
     * @param namePattern name pattern describing the File Type, mandatory parameter: {@link
     *     IllegalArgumentException} will be thrown when {@code namePattern} is not provided
     * @throws IllegalArgumentException will be thrown when {@code namePattern} is not provided
     * @return set of registered File Types
     */
    Set<FileType> getByNamePattern(@Nullable SVGResource image, @NotNull String namePattern);
  }

  /** Returns the set of all registered file types. */
  Set<FileType> getFileTypes();

  /**
   * Registers the specified File Type.
   *
   * <p>Note: {@link IllegalStateException} will be thrown when given File Type can not be
   * registered, so when the collision by file extension is detected. Use {@link FileTypeProvider}
   * to register File Type and avoid collision.
   *
   * @param fileType file type to register
   * @throws IllegalArgumentException when given {@code fileType} is {@code null}
   * @throws IllegalStateException when given File Type can not be registered
   */
  void registerFileType(FileType fileType);

  /**
   * Returns the {@link List} of all registered file types.
   *
   * @return {@link List} of all registered file types
   * @deprecated use {@link #getFileTypes()} instead
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
