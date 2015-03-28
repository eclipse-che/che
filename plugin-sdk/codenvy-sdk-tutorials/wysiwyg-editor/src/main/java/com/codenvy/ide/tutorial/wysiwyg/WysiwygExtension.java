/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.tutorial.wysiwyg;

import org.eclipse.che.ide.api.editor.EditorRegistry;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import com.codenvy.ide.tutorial.wysiwyg.part.TutorialHowToPresenter;
import com.google.gwt.resources.client.ClientBundle;
import com.google.inject.Inject;

import org.vectomatic.dom.svg.ui.SVGResource;

import static org.eclipse.che.ide.api.parts.PartStackType.EDITING;

/**
 * @author <a href="mailto:evidolob@codenvy.com">Evgen Vidolob</a>
 */
@Extension(title = "WYSIWYG Editor Extension", version = "1.0")
public class WysiwygExtension {
    public interface WysiwygResource extends ClientBundle {
        @Source("org/eclipse/che/ide/tutorial/wysiwyg/html.svg")
        SVGResource htmlFile();
    }

    @Inject
    public WysiwygExtension(FileTypeRegistry fileTypeRegistry, WysiwygResource res, WysiwygEditorProvider editorProvider,
                            EditorRegistry editorRegistry,
                            WorkspaceAgent workspaceAgent, TutorialHowToPresenter howToPresenter) {
        FileType htmlFileType = new FileType(res.htmlFile(), "text/htm", "htm");
        fileTypeRegistry.registerFileType(htmlFileType);
        editorRegistry.registerDefaultEditor(htmlFileType, editorProvider);


        workspaceAgent.openPart(howToPresenter, EDITING);
    }
}
