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
package org.eclipse.che.ide.ext.git.client.add;

import org.eclipse.che.api.git.shared.Status;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.ext.git.client.BaseTest;
import org.eclipse.che.ide.resource.Path;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;

import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testing {@link AddToIndexPresenter} functionality.
 *
 * @author Andrey Plotnikov
 * @author Vlad Zhukovskyi
 */
public class AddToIndexPresenterTest extends BaseTest {
    public static final String   MESSAGE       = "message";

    @Mock
    private AddToIndexView           view;
    @Mock
    private Status                   statusResponse;

    private AddToIndexPresenter presenter;

    @Override
    public void disarm() {
        super.disarm();

        presenter = new AddToIndexPresenter(view,
                                            appContext,
                                            constant,
                                            gitOutputConsoleFactory,
                                            processesPanelPresenter,
                                            service,
                                            notificationManager);
    }

    @Test
    public void testShowDialogWhenRootFolderIsSelected() throws Exception {
        when(service.getStatus(anyObject(), any(Path.class))).thenReturn(statusPromise);
        when(statusPromise.then(any(Operation.class))).thenReturn(statusPromise);
        when(statusPromise.catchError(any(Operation.class))).thenReturn(statusPromise);
        when(appContext.getResources()).thenReturn(new Resource[]{mock(Resource.class)});
        when(constant.addToIndexAllChanges()).thenReturn(MESSAGE);

        presenter.showDialog(project);

        verify(statusPromise).then(statusPromiseCaptor.capture());
        statusPromiseCaptor.getValue().apply(statusResponse);

        verify(constant).addToIndexAllChanges();
        verify(view).setMessage(eq(MESSAGE), Matchers.<List<String>>eq(null));
        verify(view).setUpdated(anyBoolean());
        verify(view).showDialog();
    }

    @Test
    public void testOnCancelClicked() throws Exception {
        presenter.onCancelClicked();

        verify(view).close();
    }
}
