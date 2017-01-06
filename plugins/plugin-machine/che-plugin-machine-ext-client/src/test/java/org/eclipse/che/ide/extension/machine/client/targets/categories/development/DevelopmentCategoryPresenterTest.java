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
package org.eclipse.che.ide.extension.machine.client.targets.categories.development;

import com.google.gwt.user.client.ui.AcceptsOneWidget;

import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

/** @author Oleksii Orel */
@RunWith(MockitoJUnitRunner.class)
public class DevelopmentCategoryPresenterTest {
    @Mock
    private DevelopmentView developmentView;
    @Mock
    private MachineLocalizationConstant machineLocale;

    private DevelopmentCategoryPresenter arbitraryCategoryPresenter;

    @Before
    public void setUp() {
        arbitraryCategoryPresenter = new DevelopmentCategoryPresenter(developmentView, machineLocale);
    }

    @Test
    public void testGetCategory() throws Exception {
        arbitraryCategoryPresenter.getCategory();

        verify(machineLocale).targetsViewCategoryDevelopment();
    }

    @Test
    public void testGo() throws Exception {
        AcceptsOneWidget container = Mockito.mock(AcceptsOneWidget.class);

        arbitraryCategoryPresenter.go(container);

        verify(container).setWidget(eq(developmentView));
    }
}
