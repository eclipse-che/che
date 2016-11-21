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
package org.eclipse.che.ide.ext.git.client.remote.add;

import com.google.gwt.user.client.rpc.AsyncCallback;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.ext.git.client.BaseTest;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testing {@link AddRemoteRepositoryPresenter} functionality.
 *
 * @author Andrey Plotnikov
 */
public class AddRemoteRepositoryPresenterTest extends BaseTest {
    @Mock
    private AddRemoteRepositoryView      view;
    @Mock
    private AsyncCallback<Void>          callback;
    private AddRemoteRepositoryPresenter presenter;

    @Override
    public void disarm() {
        super.disarm();

        presenter = new AddRemoteRepositoryPresenter(view, service, appContext);

        when(view.getName()).thenReturn(REMOTE_NAME);
        when(view.getUrl()).thenReturn(REMOTE_URI);
    }

    @Test
    public void testShowDialog() throws Exception {
        presenter.showDialog(callback);

        verify(view).setUrl(eq(EMPTY_TEXT));
        verify(view).setName(eq(EMPTY_TEXT));
        verify(view).setEnableOkButton(eq(DISABLE_BUTTON));
        verify(view).showDialog();
    }

    @Test
    public void testOnCancelClicked() throws Exception {
        presenter.onCancelClicked();

        verify(view).close();
    }

    @Test
    public void testOnValueChangedEnableButton() throws Exception {
        presenter.onValueChanged();

        verify(view).setEnableOkButton(eq(ENABLE_BUTTON));
    }

    @Test
    public void testOnValueChangedDisableButton() throws Exception {
        when(view.getName()).thenReturn(EMPTY_TEXT);
        when(view.getUrl()).thenReturn(EMPTY_TEXT);

        presenter.onValueChanged();

        verify(view).setEnableOkButton(eq(DISABLE_BUTTON));
    }

    @Test
    public void testUrlWithLeadingAndTrailingSpaces() throws Exception {
        when(appContext.getRootProject()).thenReturn(mock(Project.class));
        when(voidPromise.then(any(Operation.class))).thenReturn(voidPromise);
        when(voidPromise.catchError(any(Operation.class))).thenReturn(voidPromise);
        when(service.remoteAdd(anyObject(), anyObject(), anyString(), anyString())).thenReturn(voidPromise);
        when(view.getUrl()).thenReturn(" " + REMOTE_URI + " ");

        presenter.onOkClicked();

        verify(service).remoteAdd(anyObject(), anyObject(), anyString(), eq(REMOTE_URI));
    }
}
