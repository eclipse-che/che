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
package org.eclipse.che.ide.newresource;

import com.google.gwtmockito.GwtMockitoTestRunner;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Resource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the {@link AbstractNewResourceAction}.
 *
 * @author Vlad Zhukovskyi
 */
@RunWith(GwtMockitoTestRunner.class)
public class AbstractNewResourceActionTest {

    @Mock
    DialogFactory dialogFactory;
    @Mock
    CoreLocalizationConstant coreLocalizationConstant;
    @Mock
    EventBus eventBus;
    @Mock
    AppContext appContext;
    @Mock
    NotificationManager notificationManager;

    @Mock
    Resource file;
    @Mock
    Container parent;

    @Mock
    Promise<File> filePromise;

    private AbstractNewResourceAction action;

    @Before
    public void setUp() throws Exception {
        action = new AbstractNewResourceAction("",
                                               "",
                                               null,
                                               dialogFactory,
                                               coreLocalizationConstant,
                                               eventBus,
                                               appContext,
                                               notificationManager) {
            //
        };
    }

    @Test
    public void testShouldCreateFileIfSelectedFile() throws Exception {
        when(file.getParent()).thenReturn(parent);
        when(appContext.getResource()).thenReturn(file);
        when(parent.newFile(anyString(), anyString())).thenReturn(filePromise);
        when(filePromise.then(any(Operation.class))).thenReturn(filePromise);
        when(filePromise.catchError(any(Operation.class))).thenReturn(filePromise);

        action.createFile("name");

        verify(parent).newFile(eq("name"), eq(""));
    }

    @Test
    public void testShouldCreateFileIfSelectedContainer() throws Exception {
        when(appContext.getResource()).thenReturn(parent);

        when(parent.newFile(anyString(), anyString())).thenReturn(filePromise);
        when(filePromise.then(any(Operation.class))).thenReturn(filePromise);
        when(filePromise.catchError(any(Operation.class))).thenReturn(filePromise);

        action.createFile("name");

        verify(parent).newFile(eq("name"), eq(""));
    }

    @Test(expected = IllegalStateException.class)
    public void testShouldThrowExceptionIfFileDoesNotContainParent() throws Exception {
        when(appContext.getResource()).thenReturn(file);
        when(file.getParent()).thenReturn(null);

        action.createFile("name");
    }

}