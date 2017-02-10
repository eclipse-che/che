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
package org.eclipse.che.ide.ext.java.client.refactoring.rename.wizard.similarnames;

import com.google.gwtmockito.GwtMockitoTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.mockito.Mockito.verify;

@RunWith(GwtMockitoTestRunner.class)
public class SimilarNamesConfigurationPresenterTest {
    @Mock
    private SimilarNamesConfigurationView view;

    private SimilarNamesConfigurationPresenter presenter;

    @Before
    public void setUp() throws Exception {
        presenter = new SimilarNamesConfigurationPresenter(view);
    }

    @Test
    public void windowShouldBeShow() throws Exception {
        presenter.show();

        verify(view).show();
    }

    @Test
    public void valueOfStrategyShouldBeReturned() throws Exception {
        presenter.getMachStrategy();

        verify(view).getMachStrategy();
    }
}