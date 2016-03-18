/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.vfs.shared.dto;

import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/** Set of replacement variables, that contains list of files to find variables and replace by specified values. */
@DTO
public interface ReplacementSet {
    /**
     * @return - list of files to make replacement
     */
    List<String> getFiles();

    void setFiles(List<String> files);

    ReplacementSet withFiles(List<String> files);

    /**
     * @return - list of replacement
     */
    List<Variable> getEntries();

    void setEntries(List<Variable> entries);

    ReplacementSet withEntries(List<Variable> entries);

}
