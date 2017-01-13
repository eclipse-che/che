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
package org.eclipse.che.ide.orion.compare;

/**
 * his object describes options of a file. Two instances of this object construct the core parameters of a compare view.
 *
 * @author Evgen Vidolob
 */
public interface FileOptions {

    /**
     * Content the text contents of the file unit.
     * @param content
     */
    void setContent(String content);

    /**
     * Name the file name.
     * @param name
     */
    void setName(String name);

    /**
     * whether or not the file is in readonly mode.
     * @param readOnly
     */
    void setReadOnly(boolean readOnly);

}
