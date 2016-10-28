/**
 * Copyright (c) 2016 TypeFox GmbH (http://www.typefox.io) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.che.api.languageserver.shared.lsapi;

import org.eclipse.che.api.languageserver.shared.model.LanguageDescription;
import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/**
 * @author Sven Efftinge
 */
@DTO
public interface LanguageDescriptionDTO extends LanguageDescription {
	/**
     * The language id.
     */
    void setLanguageId(String id);
    
    /**
     * The optional content types this language is associated with.
     */
   void setMimeTypes(List<String> mimeTypes);
    
    /**
     * The fileExtension this language is associated with. At least one extension must be provided.
     */
    void setFileExtensions(List<String> fileExtension);
    
    /**
     * The optional highlighting configuration to support client side syntax highlighting.
     * The format is client (editor) dependent.
     */
    void setHighlightingConfiguration(String grammar);
}
