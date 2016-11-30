/*******************************************************************************
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.languageserver.shared.lsapi;

import io.typefox.lsapi.CompletionList;

import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/**
 * Represents a collection of completion items to be presented in the editor.
 * 
 * @author Kaloyan Raev
 */
@DTO
public interface CompletionListDTO extends CompletionList {

    /**
     * This list it not complete. Further typing should result in recomputing
     * this list.
     */
    void setIncomplete(boolean incomplete);

    /**
     * The completion items. Overridden to return the DTO type.
     */
    List<CompletionItemDTO> getItems();

    /**
     * The completion items.
     */
    void setItems(List<CompletionItemDTO> items);

}
