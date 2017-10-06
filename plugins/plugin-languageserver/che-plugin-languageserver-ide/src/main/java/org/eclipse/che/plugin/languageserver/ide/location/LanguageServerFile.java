/*
 * Copyright (c) 2012-2017 Red Hat, Inc. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies
 * this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.languageserver.ide.location;

import org.eclipse.che.api.languageserver.shared.dto.DtoClientImpls.FileContentParametersDto;
import org.eclipse.che.api.languageserver.shared.model.ExtendedLocation;
import org.eclipse.che.api.languageserver.shared.model.FileContentParameters;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.plugin.languageserver.ide.service.TextDocumentServiceClient;

public class LanguageServerFile implements VirtualFile {
  private final ExtendedLocation location;
  private final Path path;
  private final TextDocumentServiceClient textDocumentService;

  public LanguageServerFile(
      TextDocumentServiceClient textDocumentService, ExtendedLocation location) {
    this.textDocumentService = textDocumentService;
    this.location = location;
    this.path = new Path(location.getLocation().getUri().substring("file://".length()));
  }

  @Override
  public Path getLocation() {
    return path;
  }

  @Override
  public String getName() {
    return path.lastSegment();
  }

  @Override
  public String getDisplayName() {
    return path.lastSegment();
  }

  @Override
  public boolean isReadOnly() {
    return true;
  }

  @Override
  public String getContentUrl() {
    return null;
  }

  @Override
  public Promise<String> getContent() {
    return textDocumentService.getFileContent(
        new FileContentParametersDto(
            new FileContentParameters(
                location.getLanguageServerId(), location.getLocation().getUri())));
  }

  @Override
  public Promise<Void> updateContent(String content) {
    throw new UnsupportedOperationException();
  }
}
