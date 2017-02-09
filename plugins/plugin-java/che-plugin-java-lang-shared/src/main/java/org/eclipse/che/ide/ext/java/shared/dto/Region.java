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
package org.eclipse.che.ide.ext.java.shared.dto;

import org.eclipse.che.dto.shared.DTO;

/**
 * DTO represents some region(text range) in editor document.
 *
 * @author Evgen Vidolob
 */
@DTO
public interface Region {
    /**
     * Returns the length of the region.
     *
     * @return the length of the region
     */
    int getLength();

    void setLength(int length);
    /**
     * Returns the offset of the region.
     *
     * @return the offset of the region
     */
    int getOffset();

    void setOffset(int offset);

    Region withOffset(int offset);

    Region withLength(int length);
}
