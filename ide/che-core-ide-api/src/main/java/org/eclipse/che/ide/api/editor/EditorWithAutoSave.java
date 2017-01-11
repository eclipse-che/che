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
package org.eclipse.che.ide.api.editor;

/**
 * Editor auto save functionality.
 * It's supports enable/disable auto save.
 *
 * @author Evgen Vidolob
 */
public interface EditorWithAutoSave {

    /**
     * Return true if auto save is enabled, false otherwise.
     */
    boolean isAutoSaveEnabled();

    /**
     * Enable auto save. If editor doesn't support auto save do nothing.
     */
    void enableAutoSave();

    /**
     * Disable auto save. If editor doesn't support auto save do nothing.
     */
    void disableAutoSave();
}
