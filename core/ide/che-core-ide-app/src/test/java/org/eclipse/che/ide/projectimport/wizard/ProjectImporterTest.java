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

import com.google.gwt.event.shared.EventBus;
import com.google.web.bindery.event.shared.Event;

import org.eclipse.che.api.machine.gwt.client.DevMachine;
import org.eclipse.che.api.project.gwt.client.ProjectServiceClient;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.oauth.OAuth2AuthenticatorRegistry;
import org.eclipse.che.ide.api.project.wizard.ImportProjectNotificationSubscriberFactory;
import org.eclipse.che.ide.api.project.wizard.ProjectNotificationSubscriber;
import org.eclipse.che.ide.api.wizard.Wizard;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(MockitoJUnitRunner.class)
public class ProjectImporterTest {
    private static final String PROJECT_NAME = "project";
    private static final String ID           = "id";

    //constructors mocks
    @Mock
    private ProjectServiceClient                       projectServiceClient;
    @Mock
    private CoreLocalizationConstant                   localizationConstant;
    @Mock
    private ImportProjectNotificationSubscriberFactory subscriberFactory;
    @Mock
    private AppContext                                 appContext;
    @Mock
    private DevMachine                                 devMachine;
    @Mock
    private ProjectResolver                            resolver;

    //additional mocks
    @Mock
    private ProjectNotificationSubscriber subscriber;
    @Mock
    private ProjectConfigDto              projectConfig;
    @Mock
    private EventBus                      eventBus;
    @Mock
    private SourceStorageDto              source;
    @Mock
    private Wizard.CompleteCallback       completeCallback;
    @Mock
    private Promise<Void>                 importPromise;
    @Mock
    private OAuth2AuthenticatorRegistry   oAuth2AuthenticatorRegistry;

    @Captor
    private ArgumentCaptor<Operation<Void>> voidOperationCaptor;

    private ProjectImporter importer;

    @Before
    public void setUp() {
        when(appContext.getWorkspaceId()).thenReturn(ID);
        when(appContext.getDevMachine()).thenReturn(devMachine);
        when(devMachine.getWsAgentBaseUrl()).thenReturn("/ext");
        when(projectConfig.getName()).thenReturn(PROJECT_NAME);
        when(projectConfig.getPath()).thenReturn('/' + PROJECT_NAME);
        when(projectConfig.getSource()).thenReturn(source);
        when(subscriberFactory.createSubscriber()).thenReturn(subscriber);
        when(projectServiceClient.importProject(devMachine, '/' + PROJECT_NAME, false, source)).thenReturn(importPromise);
        when(importPromise.then(Matchers.<Operation<Void>>anyObject())).thenReturn(importPromise);
        when(importPromise.catchError(Matchers.<Operation<PromiseError>>anyObject())).thenReturn(importPromise);

        importer = new ProjectImporter(projectServiceClient,
                                       localizationConstant,
                                       subscriberFactory,
                                       appContext,
                                       resolver,
                                       null,
                                       null,
                                       oAuth2AuthenticatorRegistry,
                                       eventBus);
    }

    @Test
    public void importShouldBeSuccessAndProjectStartsResolving() throws OperationException {
        importer.importProject(completeCallback, projectConfig);

        //first time called in abstract importer
        verify(importPromise, times(2)).then(voidOperationCaptor.capture());
        voidOperationCaptor.getAllValues().get(0).apply(null);

        verify(eventBus).fireEvent(Matchers.<Event>anyObject());
        verify(resolver).resolveProject(completeCallback, projectConfig);
        verify(subscriber).onSuccess();
    }
}
