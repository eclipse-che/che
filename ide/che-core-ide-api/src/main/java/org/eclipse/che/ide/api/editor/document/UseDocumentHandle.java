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
package org.eclipse.che.ide.api.editor.document;

/**
 * Interface for components that use a document handle.<br>
 * note: this is for component that use, not produce handles.
 */
public interface UseDocumentHandle {

    /**
     * Set the document handle.
     * @param handle the handle
     */
    void setDocumentHandle(DocumentHandle handle);

    /**
     * Returns the document handle.
     * @return the handle
     */
    DocumentHandle getDocumentHandle();
}
