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
package org.eclipse.che.ide.ext.java.testing.client;

import com.google.inject.Inject;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.ext.java.client.action.JavaEditorAction;
//import org.eclipse.che.ide.ext.java.testing.client.view.TestRunnerPresenter;
//import org.eclipse.che.ide.ext.java.client.projecttree.JavaSourceFolderUtil;
//import org.eclipse.che.ide.ext.java.client.action.JavaEditorAction;

public class TestAction extends JavaEditorAction {

    private final NotificationManager notificationManager;
    private final EditorAgent editorAgent;
//    private TestRunnerPresenter presenter;

    @Inject
    public TestAction(TestResources resources, NotificationManager notificationManager, EditorAgent editorAgent,
                      FileTypeRegistry fileTypeRegistry) {
        super("Open Test Runner...", "Opens the test runner GUI", resources.TestIcon(), editorAgent, fileTypeRegistry);
        this.notificationManager = notificationManager;
        this.editorAgent = editorAgent;
//        this.presenter =  presenter;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
//        presenter.showDialog();
    }
}
