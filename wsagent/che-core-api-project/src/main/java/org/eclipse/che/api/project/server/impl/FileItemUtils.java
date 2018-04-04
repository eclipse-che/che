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
package org.eclipse.che.api.project.server.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.fileupload.FileItem;
import org.eclipse.che.api.core.ServerException;

class FileItemUtils {

  static FileItemParsed parseFile(Iterator<FileItem> iterator) throws ServerException {
    List<FileItem> fileItems = new LinkedList<>();
    iterator.forEachRemaining(fileItems::add);

    List<FileItem> fileContents =
        fileItems.stream().filter(it -> !it.isFormField()).collect(Collectors.toList());

    if (fileContents.size() > 1) {
      throw new ServerException("Expected no more than one file to upload");
    }

    if (fileContents.size() < 1) {
      throw new ServerException("Can't find file for upload");
    }

    FileItem content = fileContents.iterator().next();

    InputStream inputStream;
    try {
      inputStream = content.getInputStream();
    } catch (IOException e) {
      throw new ServerException(e);
    }

    Optional<String> name =
        fileItems
            .stream()
            .filter(FileItem::isFormField)
            .filter(it -> "name".equals(it.getFieldName()))
            .map(FileItem::getString)
            .map(String::trim)
            .filter(Objects::nonNull)
            .filter(it -> !it.isEmpty())
            .findAny();

    Optional<Boolean> overwrite =
        fileItems
            .stream()
            .filter(FileItem::isFormField)
            .filter(it -> "overwrite".equals(it.getFieldName()))
            .map(FileItem::getString)
            .map(String::trim)
            .map(Boolean::parseBoolean)
            .findAny();

    return new FileItemParsed() {
      @Override
      public String getName() {
        return name.orElse(content.getName());
      }

      @Override
      public InputStream getContent() {
        return inputStream;
      }

      @Override
      public boolean getOverwrite() {
        return overwrite.orElse(false);
      }
    };
  }

  static FileItemParsed parseDir(Iterator<FileItem> iterator) throws ServerException {
    List<FileItem> fileItems = new LinkedList<>();
    iterator.forEachRemaining(fileItems::add);

    List<FileItem> fileContents =
        fileItems.stream().filter(it -> !it.isFormField()).collect(Collectors.toList());

    if (fileContents.size() > 1) {
      throw new ServerException("Expected no more than one file to upload");
    }

    if (fileContents.size() < 1) {
      throw new ServerException("Can't find file for upload");
    }

    FileItem content = fileContents.iterator().next();

    InputStream inputStream;
    try {
      inputStream = content.getInputStream();
    } catch (IOException e) {
      throw new ServerException(e);
    }

    return new FileItemParsed() {
      @Override
      public String getName() {
        throw new UnsupportedOperationException("Not supported for directories");
      }

      @Override
      public InputStream getContent() {
        return inputStream;
      }

      @Override
      public boolean getOverwrite() {
        throw new UnsupportedOperationException("Not supported for directories");
      }
    };
  }
}
