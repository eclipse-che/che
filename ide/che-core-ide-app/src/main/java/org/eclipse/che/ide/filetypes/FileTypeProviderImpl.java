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
package org.eclipse.che.ide.filetypes;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.stream.Collectors.toSet;

import com.google.gwt.regexp.shared.RegExp;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import java.util.Optional;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry.FileTypeProvider;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Implementation of {@link FileTypeProvider}
 *
 * @author Roman Nikitenko
 */
@Singleton
public class FileTypeProviderImpl implements FileTypeProvider {
  private Resources resources;
  private FileTypeRegistry fileTypeRegistry;
  private FileType unknownFileType;

  @Inject
  public FileTypeProviderImpl(
      Resources resources,
      FileTypeRegistry fileTypeRegistry,
      @Named("defaultFileType") FileType unknownFileType) {
    this.resources = resources;
    this.fileTypeRegistry = fileTypeRegistry;
    this.unknownFileType = unknownFileType;
  }

  public FileType get(
      @Nullable SVGResource image, @NotNull String extension, @Nullable String namePattern) {
    if (isNullOrEmpty(namePattern)) {
      return getByExtension(image, extension);
    }

    checkArgument(!isNullOrEmpty(extension), "Can not register File Type without extension");

    Optional<FileType> fileTypeOptional =
        fileTypeRegistry
            .getFileTypes()
            .stream()
            .filter(candidate -> extension.equals(candidate.getExtension()))
            .filter(candidate -> canBeMergedByNamePattern(namePattern, candidate))
            .findAny();

    FileType fileType =
        fileTypeOptional.orElseGet(() -> registerNewFileType(image, extension, null));

    fileType.addNamePattern(namePattern);

    return fileType;
  }

  public FileType getByExtension(@Nullable SVGResource image, @NotNull String extension) {
    checkArgument(!isNullOrEmpty(extension), "Can not register File Type without extension");

    FileType duplicate = fileTypeRegistry.getFileTypeByExtension(extension);
    if (duplicate != unknownFileType) {
      return duplicate;
    }

    return registerNewFileType(image, extension, null);
  }

  public Set<FileType> getByNamePattern(@Nullable SVGResource image, @NotNull String namePattern) {
    checkArgument(!isNullOrEmpty(namePattern), "Can not register File Type without name pattern");

    Set<FileType> result =
        fileTypeRegistry
            .getFileTypes()
            .stream()
            .filter(candidate -> canBeMergedByNamePattern(namePattern, candidate))
            .peek(candidate -> candidate.addNamePattern(namePattern))
            .collect(toSet());

    if (result.isEmpty()) {
      result.add(registerNewFileType(image, null, namePattern));
    }

    return result;
  }

  private FileType registerNewFileType(SVGResource image, String extension, String namePattern) {
    FileType newFileType =
        new FileType(image == null ? resources.defaultImage() : image, extension, namePattern);

    fileTypeRegistry.registerFileType(newFileType);

    return newFileType;
  }

  private boolean canBeMergedByNamePattern(String namePattern, FileType fileTypeCandidate) {
    String extensionCandidate = fileTypeCandidate.getExtension();
    if (!isNullOrEmpty(extensionCandidate)
        && RegExp.compile(namePattern).test('.' + extensionCandidate)) {
      return true;
    }

    return fileTypeCandidate
        .getNamePatterns()
        .stream()
        .anyMatch(
            patternCandidate ->
                namePattern.equals(patternCandidate)
                    || RegExp.quote(namePattern).equals(patternCandidate));
  }
}
