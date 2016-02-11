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
package org.eclipse.che.ide.jseditor.client.editoradapter;

import org.eclipse.che.ide.jseditor.client.texteditor.ConfigurableTextEditor;
import org.eclipse.che.ide.jseditor.client.texteditor.TextEditor;

/**
 * Interface for an adapter that allows to look at a presenter that exposes an editor as if it were an editor.
 */
public interface EditorAdapter extends TextEditor, ConfigurableTextEditor, HasEditor {

    /**
     * Sets the nested presenter.
     * @param nestedPresenter the presenter
     */
    void setPresenter(NestablePresenter nestedPresenter);
}
