/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.languageserver.ide.util;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.languageserver.shared.lsapi.PositionDTO;
import org.eclipse.che.api.languageserver.shared.lsapi.TextDocumentIdentifierDTO;
import org.eclipse.che.api.languageserver.shared.lsapi.TextDocumentPositionParamsDTO;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.ide.dto.DtoFactory;

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

    public TextDocumentPositionParamsDTO createTDPP(Document document, int cursorOffset) {
        TextDocumentPositionParamsDTO paramsDTO = dtoFactory.createDto(TextDocumentPositionParamsDTO.class);
        TextDocumentIdentifierDTO identifierDTO = dtoFactory.createDto(TextDocumentIdentifierDTO.class);
        identifierDTO.setUri(document.getFile().getLocation().toString());

        PositionDTO positionDTO = dtoFactory.createDto(PositionDTO.class);
        TextPosition position = document.getPositionFromIndex(cursorOffset);
        positionDTO.setCharacter(position.getCharacter());
        positionDTO.setLine(position.getLine());

        paramsDTO.setUri(document.getFile().getLocation().toString());
        paramsDTO.setTextDocument(identifierDTO);
        paramsDTO.setPosition(positionDTO);
        return paramsDTO;
    }

    public TextDocumentPositionParamsDTO createTDPP(Document document, TextPosition position) {
        TextDocumentPositionParamsDTO paramsDTO = dtoFactory.createDto(TextDocumentPositionParamsDTO.class);
        TextDocumentIdentifierDTO identifierDTO = dtoFactory.createDto(TextDocumentIdentifierDTO.class);
        identifierDTO.setUri(document.getFile().getLocation().toString());

        PositionDTO positionDTO = dtoFactory.createDto(PositionDTO.class);
        positionDTO.setCharacter(position.getCharacter());
        positionDTO.setLine(position.getLine());

        paramsDTO.setUri(document.getFile().getLocation().toString());
        paramsDTO.setTextDocument(identifierDTO);
        paramsDTO.setPosition(positionDTO);
        return paramsDTO;
    }
}
