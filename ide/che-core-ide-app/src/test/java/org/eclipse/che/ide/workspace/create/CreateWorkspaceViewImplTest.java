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
package org.eclipse.che.ide.workspace.create;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwtmockito.GwtMockitoTestRunner;

import org.eclipse.che.api.machine.shared.dto.recipe.RecipeDescriptor;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.workspace.WorkspaceWidgetFactory;
import org.eclipse.che.ide.workspace.create.CreateWorkspaceView.ActionDelegate;
import org.eclipse.che.ide.workspace.create.CreateWorkspaceView.HidePopupCallBack;
import org.eclipse.che.ide.workspace.create.recipewidget.RecipeWidget;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class CreateWorkspaceViewImplTest {

    //constructor mocks
    @Mock
    private CoreLocalizationConstant      locale;
    @Mock(answer = RETURNS_DEEP_STUBS)
    private org.eclipse.che.ide.Resources resources;
    @Mock
    private FlowPanel                     tagsPanel;
    @Mock
    private WorkspaceWidgetFactory        tagFactory;
    @Mock
    private PopupPanel                    popupPanel;

    //additional mocks
    @Mock
    private RecipeDescriptor descriptor;
    @Mock
    private RecipeWidget     tag;
    @Mock
    private ActionDelegate   delegate;
    @Mock
    private KeyUpEvent       keyUpEvent;
    @Mock
    private ClickEvent       clickEvent;

    @InjectMocks
    private CreateWorkspaceViewImpl view;

    @Before
    public void setUp() {
        view.setDelegate(delegate);
    }

    @Test
    public void popupPanelShouldBeSettingUp() {
        verify(resources.coreCss()).createWsTagsPopup();
    }

    @Test
    public void nameShouldBeSet() {
        verify(view.wsName).setText(anyString());
        verify(locale).createWsDefaultName();
    }

    @Test
    public void placeholdersShouldBeSet() {
        verify(locale).placeholderChoosePredefined();
        verify(locale).placeholderInputRecipeUrl();
        verify(locale).placeholderFindByTags();
    }

    @Test
    public void workspaceNameShouldBeSet() {
        view.setWorkspaceName("test");

        verify(view.wsName).setText("test");
    }

    @Test
    public void recipeUrlShouldBeReturned() {
        view.getRecipeUrl();

        verify(view.recipeURL).getText();
    }

    @Test
    public void tagsShouldBeReturned() {
        when(view.tags.getValue()).thenReturn("test test ");

        List<String> tags = view.getTags();

        assertThat("test", is(equalTo(tags.get(0))));
    }

    @Test
    public void workspaceNameShouldBeReturned() {
        view.getWorkspaceName();

        verify(view.wsName).getText();
    }

    @Test
    public void recipesShouldBeShown() {
        when(tagFactory.create(descriptor)).thenReturn(tag);

        view.showFoundByTagRecipes(Arrays.asList(descriptor));

        verify(tagsPanel).clear();

        verify(tagFactory).create(descriptor);

        verify(tag).setDelegate(view);

        verify(view.tags).getAbsoluteLeft();
        verify(view.tags).getAbsoluteTop();
        verify(view.tags).getOffsetHeight();
    }

    @Test
    public void predefinedRecipesShouldBeShown() {
        when(tagFactory.create(descriptor)).thenReturn(tag);

        view.showPredefinedRecipes(Arrays.asList(descriptor));

        verify(view.predefinedRecipes).getAbsoluteLeft();
        verify(view.predefinedRecipes).getAbsoluteTop();
        verify(view.predefinedRecipes).getOffsetHeight();
    }

    @Test
    public void predefinedRecipeShouldBeSelected() {
        when(tag.getRecipeUrl()).thenReturn("url");
        when(tag.getTagName()).thenReturn("tag_name");

        view.onPredefineRecipesClicked(clickEvent);

        view.onTagClicked(tag);

        verify(tag).getRecipeUrl();

        verify(view.recipeURL).setText("url");
        verify(view.predefinedRecipes).setText("tag_name");

        verify(view.tags).setText("");

        verify(delegate).onRecipeUrlChanged();
    }

    @Test
    public void recipeFoundViaTagShouldBeSelected() {
        when(tag.getRecipeUrl()).thenReturn("url");
        when(tag.getTagName()).thenReturn("tag_name");

        view.onTagsChanged(keyUpEvent);

        view.onTagClicked(tag);

        verify(tag).getRecipeUrl();

        verify(view.recipeURL).setText("url");
        verify(view.predefinedRecipes).setText("");

        verify(view.tags).setText("");

        verify(delegate).onRecipeUrlChanged();
    }

    @Test
    public void urlErrorVisibilityShouldBeChanged() {
        view.setVisibleUrlError(true);

        verify(view.recipeUrlError).setVisible(true);
    }

    @Test
    public void tagsErrorVisibilityShouldBeChanged() {
        view.setVisibleTagsError(true);

        verify(view.tagsError).setVisible(true);
    }

    @Test
    public void nameErrorShouldBeShown() {
        view.showValidationNameError("error");

        verify(view.nameError).setVisible(true);
        verify(view.nameError).setText("error");
    }

    @Test
    public void nameErrorShouldNotBeShown() {
        view.showValidationNameError("");

        verify(view.nameError).setVisible(false);
        verify(view.nameError).setText("");
    }

    @Test
    public void tagsShouldBeChanged() {
        when(view.tags.getText()).thenReturn("test");

        view.onTagsChanged(keyUpEvent);

        verify(view.tags).getText();

        verify(view.tagsError).setVisible(true);

        verify(delegate).onTagsChanged(Matchers.<HidePopupCallBack>anyObject());
    }

    @Test
    public void onTagsTextBoxShouldBeClicked() {
        when(view.tags.getText()).thenReturn("test");

        view.onTagsClicked(clickEvent);

        verify(view.tags).getText();

        verify(view.tagsError).setVisible(true);

        verify(delegate).onTagsChanged(Matchers.<HidePopupCallBack>anyObject());
    }

    @Test
    public void recipeUrlShouldBeChanged() {
        view.onRecipeUrlChanged(keyUpEvent);

        verify(delegate).onRecipeUrlChanged();
    }

    @Test
    public void workspaceNameShouldBeChanged() {
        view.onWorkspaceNameChanged(keyUpEvent);

        verify(delegate).onNameChanged();
    }

    @Test
    public void predefinedRecipeShouldBeClicked() {
        view.onPredefineRecipesClicked(clickEvent);

        verify(delegate).onPredefinedRecipesClicked();
    }
}