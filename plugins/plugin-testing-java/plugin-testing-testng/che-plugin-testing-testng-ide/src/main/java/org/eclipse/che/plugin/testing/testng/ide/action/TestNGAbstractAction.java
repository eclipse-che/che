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
package org.eclipse.che.plugin.testing.testng.ide.action;

import com.google.common.base.Optional;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.testing.shared.TestExecutionContext;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.java.client.resource.SourceFolderMarker;
import org.eclipse.che.ide.ext.java.client.util.JavaUtil;
import org.eclipse.che.plugin.testing.ide.TestServiceClient;
import org.eclipse.che.plugin.testing.ide.action.RunDebugTestAbstractAction;
import org.vectomatic.dom.svg.ui.SVGResource;

import javax.validation.constraints.NotNull;
import java.util.List;

import static org.eclipse.che.api.testing.shared.TestExecutionContext.ContextType.PROJECT;
import static org.eclipse.che.ide.api.resources.Resource.FILE;
import static org.eclipse.che.ide.ext.java.client.util.JavaUtil.isJavaFile;

/**
 * Abstract action for run/debug actions for TestNg framework.
 */
public abstract class TestNGAbstractAction extends RunDebugTestAbstractAction {
    public static final String TESTNG_TEST_FRAMEWORK = "testng";

    private AppContext appContext;
    private boolean    isEnable;

    protected TestExecutionContext.ContextType contextType;
    protected String                           selectedNodePath;

    public TestNGAbstractAction(EventBus eventBus,
                                TestServiceClient client,
                                DtoFactory dtoFactory,
                                AppContext appContext,
                                NotificationManager notificationManager,
                                List<String> perspectives,
                                String description,
                                String text,
                                SVGResource icon) {
        super(eventBus, client, dtoFactory, appContext, notificationManager, perspectives, description, text, icon);
        this.appContext = appContext;
    }

    @Override
    public abstract void actionPerformed(ActionEvent e);

    @Override
    public void updateInPerspective(@NotNull ActionEvent e) {
        Presentation presentation = e.getPresentation();
        presentation.setVisible(!isEditorInFocus);
        if (!isEditorInFocus) {
            analyzeProjectTreeSelection(appContext);
        }
        presentation.setEnabled(isEnable);
    }

    private void analyzeProjectTreeSelection(AppContext appContext) {
        Resource[] resources = appContext.getResources();
        if (resources == null || resources.length > 1) {
            isEnable = false;
            return;
        }

        Resource resource = resources[0];
        if (resource.isProject() && JavaUtil.isJavaProject((Project)resource)) {
            contextType = PROJECT;
            isEnable = true;
            return;
        }

        Project project = resource.getProject();
        if (!JavaUtil.isJavaProject(project)) {
            isEnable = false;
            return;
        }

        Optional<Resource> srcFolder = resource.getParentWithMarker(SourceFolderMarker.ID);
        if (!srcFolder.isPresent() || resource.getLocation().equals(srcFolder.get().getLocation())) {
            isEnable = false;
            return;
        }

        if (resource.getResourceType() == FILE && isJavaFile(resource)) {
            contextType = TestExecutionContext.ContextType.FILE;
        } else if (resource instanceof Container) {
            contextType = TestExecutionContext.ContextType.FOLDER;
        }
        selectedNodePath = resource.getLocation().toString();
        isEnable = true;
    }
}
