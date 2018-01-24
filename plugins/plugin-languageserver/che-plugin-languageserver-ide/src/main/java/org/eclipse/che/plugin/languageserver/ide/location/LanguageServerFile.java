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
import org.eclipse.che.ide.rest.UrlBuilder;
import org.eclipse.che.plugin.languageserver.ide.service.TextDocumentServiceClient;

public class LanguageServerFile implements VirtualFile, HasURI {
  private final String uri;
  private final Path path;
  private final TextDocumentServiceClient textDocumentService;
  private String name;

  public LanguageServerFile(TextDocumentServiceClient textDocumentService, String uri) {
    this.textDocumentService = textDocumentService;
    this.uri = uri;
    this.path = new Path(uri);
    this.name = extractName(uri);
  }

  private String extractName(String uri) {
    String path = new UrlBuilder(uri).getPath();
    return new Path(path).lastSegment();
  }

  @Override
  public Path getLocation() {
    return path;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getDisplayName() {
    return name;
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
    return textDocumentService.getFileContent(uri);
  }

  public String getURI() {
    return uri;
  }

  @Override
  public Promise<Void> updateContent(String content) {
    throw new UnsupportedOperationException();
  }
}
