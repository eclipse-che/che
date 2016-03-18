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

/**
 * Configuration for replacement. Contains part of text to search and another to replace one.
 *
 * @author Sergii Kabashniuk
 */
@DTO
public interface Variable {
    String getFind();

    void setFind(String find);

    Variable withFind(String find);

    String getReplace();

    void setReplace(String replace);

    Variable withReplace(String replace);

    String getReplacemode();

    void setReplacemode(String replacemode);

    Variable withReplacemode(String replacemode);

}
