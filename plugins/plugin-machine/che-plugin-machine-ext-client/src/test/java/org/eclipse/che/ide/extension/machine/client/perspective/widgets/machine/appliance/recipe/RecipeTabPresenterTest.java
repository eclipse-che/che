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
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.machine.appliance.recipe;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.extension.machine.client.RecipeScriptDownloadServiceClient;
import org.eclipse.che.ide.extension.machine.client.machine.Machine;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Valeriy Svydenko
 */
@RunWith(MockitoJUnitRunner.class)
public class RecipeTabPresenterTest {
    @Mock
    private RecipeView                        view;
    @Mock
    private Machine                           machine;
    @Mock
    private RecipeScriptDownloadServiceClient recipeScriptClient;

    @Mock
    private Promise<String> recipePromise;

    @Captor
    private ArgumentCaptor<Operation<String>> argumentCaptor;

    @InjectMocks
    private RecipeTabPresenter presenter;

    @Test
    public void viewShouldBeReturned() throws Exception {
        assertEquals(view, presenter.getView());
    }

    @Test
    public void tabShouldBeHidden() throws Exception {
        presenter.setVisible(false);

        verify(view).setVisible(false);
    }

    @Test
    public void tabShouldBeVisible() throws Exception {
        presenter.setVisible(true);

        verify(view).setVisible(true);
    }

    @Test
    public void tabGetScriptAsContent() throws Exception {
        when(recipeScriptClient.getRecipeScript(any(Machine.class))).thenReturn(recipePromise);
        when(recipePromise.then(any(Operation.class))).thenReturn(recipePromise);

        presenter.updateInfo(machine);

        verify(recipePromise).then(argumentCaptor.capture());
        argumentCaptor.getValue().apply("test content");
        verify(view).setScript("test content");
    }


}
