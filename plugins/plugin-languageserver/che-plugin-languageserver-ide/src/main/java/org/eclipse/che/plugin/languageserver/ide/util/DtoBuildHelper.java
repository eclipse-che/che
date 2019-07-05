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
package org.eclipse.che.plugin.languageserver.ide.util;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.plugin.languageserver.ide.location.HasURI;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentPositionParams;

/**
 * Helps to create LS DTO objects
 *
 * @author Evgen Vidolob
 */
@Singleton
public class DtoBuildHelper {
  private final DtoFactory dtoFactory;

  @Inject
  public DtoBuildHelper(DtoFactory dtoFactory) {
    this.dtoFactory = dtoFactory;
  }

  public TextDocumentPositionParams createTDPP(Document document, int cursorOffset) {
    TextPosition position = document.getPositionFromIndex(cursorOffset);
    return createTDPP(document.getFile(), position);
  }

  public TextDocumentIdentifier createTDI(VirtualFile file) {
    TextDocumentIdentifier identifierDTO = dtoFactory.createDto(TextDocumentIdentifier.class);
    identifierDTO.setUri(getUri(file));
    return identifierDTO;
  }

  public String getUri(VirtualFile file) {
    if (file instanceof HasURI) {
      return ((HasURI) file).getURI();
    } else {
      return file.getLocation().toString();
    }
  }

  public TextDocumentPositionParams createTDPP(Document document, TextPosition position) {
    VirtualFile file = document.getFile();
    return createTDPP(file, position);
  }

  private TextDocumentPositionParams createTDPP(VirtualFile file, TextPosition position) {
    TextDocumentPositionParams paramsDTO = dtoFactory.createDto(TextDocumentPositionParams.class);
    TextDocumentIdentifier identifierDTO = createTDI(file);
    Position Position = dtoFactory.createDto(Position.class);
    Position.setCharacter(position.getCharacter());
    Position.setLine(position.getLine());

    paramsDTO.setUri(identifierDTO.getUri());
    paramsDTO.setTextDocument(identifierDTO);
    paramsDTO.setPosition(Position);
    return paramsDTO;
  }
}
