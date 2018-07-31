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
package org.eclipse.che.api.project.server.impl;

import static org.eclipse.che.api.project.shared.Constants.ZIP_IMPORTER_ID;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.function.Supplier;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.config.SourceStorage;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.fs.server.FsManager;
import org.eclipse.che.api.project.server.ProjectImporter;

/** @author Vitaly Parfonov */
@Singleton
public class ZipProjectImporter implements ProjectImporter {

  private final FsManager fsManager;

  @Inject
  public ZipProjectImporter(FsManager fsManager) {
    this.fsManager = fsManager;
  }

  @Override
  public String getId() {
    return ZIP_IMPORTER_ID;
  }

  @Override
  public boolean isInternal() {
    return false;
  }

  @Override
  public String getDescription() {
    return "Import project from ZIP archive under a public URL.";
  }

  @Override
  public void doImport(SourceStorage src, String dst)
      throws ForbiddenException, ConflictException, IOException, ServerException,
          NotFoundException {
    doImport(src, dst, null);
  }

  @Override
  public void doImport(SourceStorage src, String dst, Supplier<LineConsumer> supplier)
      throws ForbiddenException, NotFoundException, ConflictException, IOException,
          ServerException {

    URL url;
    String location = src.getLocation();
    if (location.startsWith("http://") || location.startsWith("https://")) {
      url = new URL(location);
    } else {
      url = Thread.currentThread().getContextClassLoader().getResource(location);
      if (url == null) {
        File file = new File(location);
        if (file.exists()) {
          url = file.toURI().toURL();
        }
      }
    }

    if (url == null) {
      throw new IOException(String.format("Can't find %s", location));
    }

    try (InputStream zip = url.openStream()) {
      boolean skipFirstLevel = false;
      Map<String, String> parameters = src.getParameters();
      if (parameters != null && parameters.containsKey("skipFirstLevel")) {
        skipFirstLevel = Boolean.parseBoolean(parameters.get("skipFirstLevel"));
      }

      fsManager.unzip(dst, zip, skipFirstLevel);
    }
  }

  @Override
  public SourceCategory getSourceCategory() {
    return SourceCategory.ARCHIVE;
  }
}
