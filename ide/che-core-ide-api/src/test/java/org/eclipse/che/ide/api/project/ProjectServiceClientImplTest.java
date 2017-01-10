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
package org.eclipse.che.ide.api.project;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.DevMachine;
import org.eclipse.che.ide.api.machine.WsAgentStateController;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.rest.AsyncRequest;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.AsyncRequestLoader;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link ProjectServiceClientImpl}.
 *
 * @author Vlad Zhukovskyi
 */
@RunWith(MockitoJUnitRunner.class)
public class ProjectServiceClientImplTest {

    @Mock
    private WsAgentStateController wsAgentStateController;
    @Mock
    private LoaderFactory          loaderFactory;
    @Mock
    private AsyncRequestFactory    asyncRequestFactory;
    @Mock
    private DtoFactory             dtoFactory;
    @Mock
    private DtoUnmarshallerFactory dtoUnmarshallerFactory;
    @Mock
    private AppContext             appContext;

    private ProjectServiceClientImpl projectServiceClient;

    @Before
    public void setUp() throws Exception {
        projectServiceClient = new ProjectServiceClientImpl(wsAgentStateController,
                                                            loaderFactory,
                                                            asyncRequestFactory,
                                                            dtoFactory,
                                                            dtoUnmarshallerFactory,
                                                            appContext);
        DevMachine devMachine = mock(DevMachine.class);
        when(devMachine.getWsAgentBaseUrl()).thenReturn("");

        when(appContext.getDevMachine()).thenReturn(devMachine);
    }

    @Test
    public void testShouldNotSetupLoaderForTheGetTreeMethod() throws Exception {
        AsyncRequest asyncRequest = mock(AsyncRequest.class);

        when(asyncRequestFactory.createGetRequest(anyString())).thenReturn(asyncRequest);
        when(asyncRequest.header(anyString(), anyString())).thenReturn(asyncRequest);

        projectServiceClient.getTree(Path.EMPTY, 1, true);

        verify(asyncRequest, never()).loader(any(AsyncRequestLoader.class)); //see CHE-3467
    }
}