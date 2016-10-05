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
package org.eclipse.che.ide.projectimport.wizard;

import org.eclipse.che.api.core.model.project.ProjectConfig;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.project.MutableProjectConfig;
import org.eclipse.che.ide.api.project.wizard.ImportProjectNotificationSubscriberFactory;
import org.eclipse.che.ide.api.project.wizard.ProjectNotificationSubscriber;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.wizard.Wizard;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Dmitry Shnurenko
 * @author Vlad Zhukovskyi
 */
@RunWith(MockitoJUnitRunner.class)
public class ProjectImporterTest {

    //constructors mocks
    @Mock
    private CoreLocalizationConstant                   localizationConstant;
    @Mock
    private ImportProjectNotificationSubscriberFactory subscriberFactory;
    @Mock
    private AppContext                                 appContext;
    @Mock
    private ProjectResolver                            resolver;

    //additional mocks
    @Mock
    private ProjectNotificationSubscriber             subscriber;
    @Mock
    private MutableProjectConfig                      projectConfig;
    @Mock
    private MutableProjectConfig.MutableSourceStorage source;
    @Mock
    private Wizard.CompleteCallback                   completeCallback;
    @Mock
    private Container                                 workspaceRoot;
    @Mock
    private Project.ProjectRequest                    importRequest;
    @Mock
    private Promise<Project>                          importPromise;
    @Mock
    private Project                                   importedProject;

    @Captor
    private ArgumentCaptor<Function<Project, Promise<Project>>>            importProjectCaptor;

    private ProjectImporter importer;

    @Before
    public void setUp() {
        when(appContext.getWorkspaceRoot()).thenReturn(workspaceRoot);
        when(workspaceRoot.importProject()).thenReturn(importRequest);
        when(importRequest.withBody(any(ProjectConfig.class))).thenReturn(importRequest);
        when(importRequest.send()).thenReturn(importPromise);
        when(importPromise.thenPromise(any(Function.class))).thenReturn(importPromise);
        when(projectConfig.getPath()).thenReturn("/foo");
        when(projectConfig.getSource()).thenReturn(source);
        when(subscriberFactory.createSubscriber()).thenReturn(subscriber);
        when(projectConfig.getSource()).thenReturn(source);
        when(importPromise.catchErrorPromise(any(Function.class))).thenReturn(importPromise);
        when(importPromise.then(any(Function.class))).thenReturn(importPromise);
        when(importPromise.then(any(Operation.class))).thenReturn(importPromise);

        importer = new ProjectImporter(localizationConstant,
                                       subscriberFactory,
                                       appContext,
                                       resolver,
                                       null,
                                       null,
                                       null);
    }

    @Test
    public void importShouldBeSuccessAndProjectStartsResolving() throws Exception {
        importer.importProject(completeCallback, projectConfig);

        verify(importPromise).thenPromise(importProjectCaptor.capture());
        importProjectCaptor.getValue().apply(importedProject);

        verify(subscriber).onSuccess();
        verify(resolver).resolve(eq(importedProject));
    }
}
