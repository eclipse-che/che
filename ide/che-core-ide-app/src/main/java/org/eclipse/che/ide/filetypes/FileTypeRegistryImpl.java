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

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.stream.Collectors.toSet;
import static org.eclipse.che.ide.util.NameUtils.getFileExtension;

import com.google.gwt.regexp.shared.RegExp;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.api.resources.VirtualFile;

/**
 * Implementation of {@link FileTypeRegistry}
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class FileTypeRegistryImpl implements FileTypeRegistry {
  private final FileType unknownFileType;
  private final Set<FileType> fileTypes = new HashSet<>();

  @Inject
  public FileTypeRegistryImpl(@Named("defaultFileType") FileType unknownFileType) {
    this.unknownFileType = unknownFileType;
  }

  @Override
  public void registerFileType(FileType candidate) {
    if (candidate == null) {
      throw new IllegalArgumentException("Can not register Illegal File Type");
    }

    String extension = candidate.getExtension();
    FileType duplicate = getFileTypeByExtension(extension);
    if (duplicate != unknownFileType && duplicate != candidate) {
      throw new IllegalStateException(
          "File Type with extension " + extension + " is already registered");
    }

    fileTypes.add(candidate);
  }

  @Override
  public List<FileType> getRegisteredFileTypes() {
    return new ArrayList<>(fileTypes);
  }

  @Override
  public Set<FileType> getFileTypes() {
    return new HashSet<>(fileTypes);
  }

  @Override
  public FileType getFileTypeByFile(VirtualFile file) {
    String fileName = file.getName();
    String fileExtension = getFileExtension(fileName);

    FileType fileType = getFileTypeByFileName(fileName);
    if (fileType == unknownFileType) {
      fileType = getFileTypeByExtension(fileExtension);
    }
    return fileType != null ? fileType : unknownFileType;
  }

  @Override
  public FileType getFileTypeByExtension(String extension) {
    if (isNullOrEmpty(extension)) {
      return unknownFileType;
    }

    Set<FileType> typesByExtension =
        fileTypes.stream().filter(type -> extension.equals(type.getExtension())).collect(toSet());
    if (typesByExtension.isEmpty()) {
      return unknownFileType;
    }

    String nameToTest = '.' + extension;
    Optional<FileType> fileType =
        typesByExtension
            .stream()
            .filter(type -> doesFileNameMatchType(nameToTest, type))
            .findFirst();
    if (fileType.isPresent()) {
      return fileType.get();
    }

    fileType =
        typesByExtension.stream().filter(type -> type.getNamePatterns().isEmpty()).findFirst();
    return fileType.orElseGet(() -> typesByExtension.iterator().next());
  }

  @Override
  public FileType getFileTypeByFileName(String name) {
    if (isNullOrEmpty(name)) {
      return unknownFileType;
    }

    Set<FileType> typesByNamePattern =
        fileTypes.stream().filter(type -> doesFileNameMatchType(name, type)).collect(toSet());

    if (typesByNamePattern.isEmpty()) {
      return unknownFileType;
    }

    if (typesByNamePattern.size() == 1) {
      return typesByNamePattern.iterator().next();
    }

    String fileExtension = getFileExtension(name);
    if (isNullOrEmpty(fileExtension)) {
      return typesByNamePattern.iterator().next();
    }

    Optional<FileType> fileType =
        typesByNamePattern
            .stream()
            .filter(type -> fileExtension.equals(type.getExtension()))
            .findFirst();
    return fileType.orElseGet(() -> typesByNamePattern.iterator().next());
  }

  private boolean doesFileNameMatchType(String nameToTest, FileType fileType) {
    return fileType
        .getNamePatterns()
        .stream()
        .anyMatch(
            namePattern -> {
              RegExp regExp = RegExp.compile(namePattern);
              return regExp.test(nameToTest);
            });
  }
}
