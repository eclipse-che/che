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
package org.eclipse.che.plugin.svn.ide.export;

import org.eclipse.che.ide.api.project.node.HasStorablePath;
import org.eclipse.che.plugin.svn.ide.common.BaseSubversionPresenterTest;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link org.eclipse.che.plugin.svn.ide.export.ExportPresenter}.
 *
 * @author Vladyslav Zhukovskyi
 */
public class ExportPresenterTest extends BaseSubversionPresenterTest {

    private ExportPresenter presenter;

    @Mock
    ExportView exportView;

    @Mock
    DialogFactory dialogFactory;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        presenter = new ExportPresenter(null, appContext, subversionOutputConsoleFactory, consolesPanelPresenter, projectExplorerPart,
                                        exportView, dtoUnmarshallerFactory, service, notificationManager, constants, statusColors);
    }

    @Test
    public void testExportViewShouldBeShowed() throws Exception {
        presenter.showExport(mock(HasStorablePath.class));

        verify(exportView).onShow();
    }
}
