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
 * Empty implementation of the {@link org.eclipse.che.ide.api.editor.EditorAgent.OpenEditorCallback}
 *
 * @author Alexander Andrienko
 */
public class OpenEditorCallbackImpl implements EditorAgent.OpenEditorCallback {

    @Override
    public void onEditorOpened(EditorPartPresenter editor) {
    }

    @Override
    public void onEditorActivated(EditorPartPresenter editor) {
    }

    @Override
    public void onInitializationFailed() {
    }
}
