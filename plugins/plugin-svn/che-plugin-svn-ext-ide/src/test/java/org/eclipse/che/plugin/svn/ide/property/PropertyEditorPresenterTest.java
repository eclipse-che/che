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
package org.eclipse.che.plugin.svn.ide.property;

import org.eclipse.che.plugin.svn.ide.common.BaseSubversionPresenterTest;
import org.eclipse.che.plugin.svn.shared.CLIOutputResponse;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link org.eclipse.che.plugin.svn.ide.property.PropertyEditorPresenter}.
 *
 * @author Vladyslav Zhukovskyi
 */
public class PropertyEditorPresenterTest extends BaseSubversionPresenterTest {

    @Captor
    private ArgumentCaptor<AsyncRequestCallback<CLIOutputResponse>> asyncRequestCallbackStatusCaptor;

    private PropertyEditorPresenter presenter;

    @Mock
    PropertyEditorView view;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        presenter =
                new PropertyEditorPresenter(appContext, eventBus, subversionOutputConsolePresenter, workspaceAgent, projectExplorerPart,
                                            view, service,
                                            dtoUnmarshallerFactory, notificationManager, constants);
    }

    @Test
    public void testViewShouldBeShowed() throws Exception {
        presenter.showEditor();

        verify(view).onShow();
    }
}
