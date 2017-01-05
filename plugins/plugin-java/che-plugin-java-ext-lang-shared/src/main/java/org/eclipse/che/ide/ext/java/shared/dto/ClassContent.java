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
 * Represents content of *.class file.
 * if jar has attached sources contains source of class
 *
 * @author Evgen Vidolob
 */
@DTO
public interface ClassContent {

    /**
     * @return the content of class file
     */
    String getContent();

    void setContent(String content);

    /**
     * @return true if content of class generated(decompiled) from .class file byte code, false if jar has attached sources
     */
    boolean isGenerated();

    void setGenerated(boolean generated);
}
