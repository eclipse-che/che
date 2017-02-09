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
 * DTO represents the information about the proposal change.
 *
 * @author Evgen Vidolob
 * @author Valeriy Svydenko
 */
@DTO
public interface Change {

    /** Returns the offset of the change. */
    int getOffset();

    void setOffset(int offset);

    Change withOffset(int offset);

    /** Returns length of the text change. */
    int getLength();

    void setLength(int length);

    Change withLength(int length);

    /** Returns text of the change. */
    String getText();

    void setText(String text);

    Change withText(String text);
}
