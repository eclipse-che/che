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
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.Folder;
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
 * Unit tests for the {@link NewFolderAction}.
 *
 * @author Vlad Zhukovskyi
 */
@RunWith(GwtMockitoTestRunner.class)
public class NewFolderActionTest {

    @Mock
    CoreLocalizationConstant coreLocalizationConstant;
    @Mock
    Resources resources;
    @Mock
    DialogFactory dialogFactory;
    @Mock
    EventBus eventBus;
    @Mock
    AppContext appContext;
    @Mock
    NotificationManager notificationManager;

    @Mock
    Resource  file;
    @Mock
    Container parent;

    @Mock
    Promise<Folder> folderPromise;

    private NewFolderAction action;

    @Before
    public void setUp() throws Exception {
        action = new NewFolderAction(coreLocalizationConstant,
                                     resources,
                                     dialogFactory,
                                     eventBus,
                                     appContext, notificationManager);
    }

    @Test
    public void testShouldCreateFolderIfSelectedFile() throws Exception {
        when(file.getParent()).thenReturn(parent);
        when(appContext.getResource()).thenReturn(file);
        when(parent.newFolder(anyString())).thenReturn(folderPromise);
        when(folderPromise.then(any(Operation.class))).thenReturn(folderPromise);
        when(folderPromise.catchError(any(Operation.class))).thenReturn(folderPromise);

        action.createFolder("name");

        verify(parent).newFolder(eq("name"));
    }

    @Test
    public void testShouldCreateFolderIfSelectedContainer() throws Exception {
        when(appContext.getResource()).thenReturn(parent);

        when(parent.newFolder(anyString())).thenReturn(folderPromise);
        when(folderPromise.then(any(Operation.class))).thenReturn(folderPromise);
        when(folderPromise.catchError(any(Operation.class))).thenReturn(folderPromise);

        action.createFolder("name");

        verify(parent).newFolder(eq("name"));
    }

    @Test(expected = IllegalStateException.class)
    public void testShouldThrowExceptionIfFileDoesNotContainParent() throws Exception {
        when(appContext.getResource()).thenReturn(file);
        when(file.getParent()).thenReturn(null);

        action.createFolder("name");
    }

}