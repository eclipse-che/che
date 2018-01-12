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
package org.eclipse.che.plugin.languageserver.ide.location;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.plugin.languageserver.ide.service.TextDocumentServiceClient;
import org.eclipse.lsp4j.Location;

public class LanguageServerFile implements VirtualFile {
  private final Location location;
  private final Path path;
  private final TextDocumentServiceClient textDocumentService;

  public LanguageServerFile(TextDocumentServiceClient textDocumentService, Location location) {
    this.textDocumentService = textDocumentService;
    this.location = location;
    this.path = new Path(location.getUri().substring("file://".length()));
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
    return textDocumentService.getFileContent(location.getUri());
  }

  @Override
  public Promise<Void> updateContent(String content) {
    throw new UnsupportedOperationException();
  }
}
