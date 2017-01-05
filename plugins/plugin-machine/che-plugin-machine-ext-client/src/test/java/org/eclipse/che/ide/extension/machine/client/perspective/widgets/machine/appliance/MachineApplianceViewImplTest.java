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
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.machine.appliance;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwtmockito.GwtMockitoTestRunner;

import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.MachineResources;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.recipe.container.RecipesContainerView;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab.container.TabContainerView;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Matchers;
import org.mockito.Mock;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class MachineApplianceViewImplTest {

    private static final String SOME_TEXT = "someText";

    //constructor mocks
    @Mock
    private TabContainerView            tabContainerView;
    @Mock
    private Label                       unavailableLabel;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MachineResources            resources;
    @Mock
    private MachineLocalizationConstant locale;

    //additional mocks
    @Mock
    private RecipesContainerView recipesView;
    @Mock
    private Widget               widget1;
    @Mock
    private Widget               widget2;
    @Mock
    private Widget               unavailable;

    private MachineApplianceViewImpl view;

    @Before
    public void setUp() {
        when(tabContainerView.asWidget()).thenReturn(widget1);
        when(recipesView.asWidget()).thenReturn(widget2);
        when(unavailableLabel.asWidget()).thenReturn(unavailable);

        when(resources.getCss().unavailableLabel()).thenReturn(SOME_TEXT);
        when(locale.unavailableMachineInfo()).thenReturn(SOME_TEXT);

        view = new MachineApplianceViewImpl(resources, unavailableLabel);
    }

    @Test
    public void constructorShouldBeVerified() {
        verify(resources.getCss()).unavailableLabel();
        verify(unavailableLabel).addStyleName(SOME_TEXT);

        verify(view.mainContainer).add(Matchers.<IsWidget>anyObject());
    }

    @Test
    public void tabContainerShouldBeAdded() {
        view.addContainer(tabContainerView);

        verify(view.mainContainer).add(tabContainerView);
    }

    @Test
    public void tabContainerShouldBeAddedOnlyOnce() {
        view.addContainer(tabContainerView);
        view.addContainer(tabContainerView);
        view.addContainer(tabContainerView);

        verify(view.mainContainer).add(tabContainerView);
    }

    @Test
    public void stubShouldBeShown() {
        view.addContainer(tabContainerView);
        view.addContainer(recipesView);

        view.showStub("stub");

        verifyHideAll();

        verify(unavailableLabel).setText("stub");
        verify(unavailableLabel).setVisible(true);
    }

    private void verifyHideAll() {
        verify(widget1).setVisible(false);
        verify(widget2).setVisible(false);
        verify(unavailable).setVisible(false);
    }

    @Test
    public void containerShouldBeShown() {
        view.addContainer(tabContainerView);
        view.addContainer(recipesView);

        view.showContainer(tabContainerView);

        verifyHideAll();

        verify(widget1).setVisible(true);
    }
}