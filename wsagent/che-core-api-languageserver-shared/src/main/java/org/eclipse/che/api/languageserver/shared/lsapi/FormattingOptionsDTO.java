/**
 * Copyright (c) 2016 TypeFox GmbH (http://www.typefox.io) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.che.api.languageserver.shared.lsapi;

import io.typefox.lsapi.FormattingOptions;

import org.eclipse.che.dto.shared.DTO;

import java.util.Map;

/**
 * @author Sven Efftinge
 */
@DTO
public interface FormattingOptionsDTO extends FormattingOptions {
    /**
     * Size of a tab in spaces.
     */
    void setTabSize(final int tabSize);

    /**
     * Prefer spaces over tabs.
     */
    void setInsertSpaces(final boolean insertSpaces);

    /**
     * Signature for further properties.
     */
    void setProperties(final Map<String, String> properties);
}
