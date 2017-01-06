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
package org.eclipse.che.ide.api.editor.texteditor;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;

/** Resources interface for the editor. */
public interface EditorResources extends ClientBundle {

    /** CssResource for the editor. */
    interface EditorCss extends CssResource {

        /** Style added to warnings. */
        String lineWarning();

        /** Style added to errors. */
        String lineError();

        /** Style added to the current breakpoint line. */
        String debugLine();
    }

    @Source({"Editor.css", "org/eclipse/che/ide/api/ui/style.css"})
    EditorCss editorCss();

}
