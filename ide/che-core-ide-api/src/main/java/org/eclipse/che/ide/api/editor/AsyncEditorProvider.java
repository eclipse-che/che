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
package org.eclipse.che.ide.api.editor;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.resources.VirtualFile;

/**
 * Extension interface for {@link EditorProvider}
 * Add ability to create editor asynchronously.
 * {@link EditorAgent} should use this interface to crate editor instance
 *
 * @author Evgen Vidolob
 */
public interface AsyncEditorProvider {

    /**
     * Create promise for creating new editor instance.
     *
     * @param file
     *         the file for which editor should crated
     * @return
     *      promise
     *
     */
    Promise<EditorPartPresenter> createEditor(VirtualFile file);
}
