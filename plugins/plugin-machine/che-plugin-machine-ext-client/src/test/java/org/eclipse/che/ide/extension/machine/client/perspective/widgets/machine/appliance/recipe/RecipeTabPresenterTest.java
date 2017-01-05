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
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.machine.appliance.recipe;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.machine.MachineEntity;
import org.eclipse.che.ide.extension.machine.client.RecipeScriptDownloadServiceClient;
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
    private MachineEntity                           machine;
    @Mock
    private RecipeScriptDownloadServiceClient recipeScriptClient;

    @Mock
    private Promise<String> recipePromise;

    @Mock
    private PromiseError promiseError;

    @Captor
    private ArgumentCaptor<Operation<String>>       argumentCaptor;
    @Captor
    private ArgumentCaptor<Operation<PromiseError>> errorArgumentCaptor;

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

//    @Test
    public void tabSetDockerfileScriptContent() throws Exception {
        when(recipeScriptClient.getRecipeScript(any(MachineEntity.class))).thenReturn(recipePromise);
        when(recipePromise.then(any(Operation.class))).thenReturn(recipePromise);

        presenter.updateInfo(machine);

        verify(recipePromise).then(argumentCaptor.capture());
        argumentCaptor.getValue().apply("test content");
        verify(view).setScript("test content");
    }

//    @Test
    public void tabSetImageLocation() throws Exception {

        presenter.updateInfo(machine);

        verify(view).setScript("Image location: localhost:5000/image:latest");
    }

//    @Test
    public void tabSetErrorMessageWhenRecipeTypeIsNull() throws Exception {
        when(machine.getId()).thenReturn("machine123");

        presenter.updateInfo(machine);

        verify(view).setScript("Recipe type is null for machine 'machine123'");
    }

//    @Test
    public void tabSetErrorMessageWhenFailedToFetchScript() throws Exception {
        when(recipeScriptClient.getRecipeScript(any(MachineEntity.class))).thenReturn(recipePromise);
        when(recipePromise.then(any(Operation.class))).thenReturn(recipePromise);
        when(machine.getId()).thenReturn("machine123");

        presenter.updateInfo(machine);

        verify(recipePromise).catchError(errorArgumentCaptor.capture());
        errorArgumentCaptor.getValue().apply(promiseError);
        verify(view).setScript("Failed to get recipe script for machine 'machine123'");
    }
}
