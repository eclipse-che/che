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
package org.eclipse.che.ide.ext.java.client.action;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.organizeimports.OrganizeImportsPresenter;

/**
 * Organizes the imports of a compilation unit.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class OrganizeImportsAction extends JavaEditorAction implements ProposalAction {
    public final static String JAVA_ORGANIZE_IMPORT_ID = "javaOrganizeImports";

    private final EditorAgent              editorAgent;
    private final OrganizeImportsPresenter organizeImportsPresenter;

    @Inject
    public OrganizeImportsAction(JavaLocalizationConstant locale,
                                 EditorAgent editorAgent,
                                 FileTypeRegistry fileTypeRegistry,
                                 OrganizeImportsPresenter organizeImportsPresenter) {
        super(locale.organizeImportsName(), locale.organizeImportsDescription(), null, editorAgent, fileTypeRegistry);
        this.editorAgent = editorAgent;
        this.organizeImportsPresenter = organizeImportsPresenter;
    }

    @Override
    public void performAsProposal() {
        actionPerformed(null);
    }

    @Override
    public String getId() {
        return JAVA_ORGANIZE_IMPORT_ID;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final EditorPartPresenter editor = editorAgent.getActiveEditor();
        organizeImportsPresenter.organizeImports(editor);
    }

}