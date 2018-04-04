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
package org.eclipse.che.plugin.languageserver.ide.util;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.ide.dto.DtoFactory;
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
    TextDocumentPositionParams paramsDTO = dtoFactory.createDto(TextDocumentPositionParams.class);
    TextDocumentIdentifier identifierDTO = dtoFactory.createDto(TextDocumentIdentifier.class);
    identifierDTO.setUri(document.getFile().getLocation().toString());

    Position Position = dtoFactory.createDto(Position.class);
    TextPosition position = document.getPositionFromIndex(cursorOffset);
    Position.setCharacter(position.getCharacter());
    Position.setLine(position.getLine());

    paramsDTO.setUri(document.getFile().getLocation().toString());
    paramsDTO.setTextDocument(identifierDTO);
    paramsDTO.setPosition(Position);
    return paramsDTO;
  }

  public TextDocumentPositionParams createTDPP(Document document, TextPosition position) {
    TextDocumentPositionParams paramsDTO = dtoFactory.createDto(TextDocumentPositionParams.class);
    TextDocumentIdentifier identifierDTO = dtoFactory.createDto(TextDocumentIdentifier.class);
    identifierDTO.setUri(document.getFile().getLocation().toString());

    Position Position = dtoFactory.createDto(Position.class);
    Position.setCharacter(position.getCharacter());
    Position.setLine(position.getLine());

    paramsDTO.setUri(document.getFile().getLocation().toString());
    paramsDTO.setTextDocument(identifierDTO);
    paramsDTO.setPosition(Position);
    return paramsDTO;
  }
}
