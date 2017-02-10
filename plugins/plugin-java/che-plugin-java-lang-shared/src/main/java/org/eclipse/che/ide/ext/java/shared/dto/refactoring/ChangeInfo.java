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
package org.eclipse.che.ide.ext.java.shared.dto.refactoring;

import org.eclipse.che.dto.shared.DTO;

/**
 * DTO represents the information about performed refactoring change.
 *
 * @author Valeriy Svydenko
 */
@DTO
public interface ChangeInfo {
    /** @return name of the change. */
    ChangeName getName();

    void setName(ChangeName name);

    /** @return path of the resource before applying changes. */
    String getOldPath();

    void setOldPath(String path);

    /** @return path of the resource after applying changes. */
    String getPath();

    void setPath(String path);

    enum ChangeName {
        RENAME_COMPILATION_UNIT,
        RENAME_PACKAGE,
        UPDATE,
        MOVE
    }
}
