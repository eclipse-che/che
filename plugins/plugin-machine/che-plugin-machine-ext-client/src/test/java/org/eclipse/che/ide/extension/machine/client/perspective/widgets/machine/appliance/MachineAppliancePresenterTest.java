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
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.machine.appliance;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwtmockito.GwtMockitoTestRunner;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.event.ActivePartChangedEvent;
import org.eclipse.che.ide.api.machine.MachineEntity;
import org.eclipse.che.ide.part.widgets.TabItemFactory;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.inject.factories.EntityFactory;
import org.eclipse.che.ide.extension.machine.client.inject.factories.WidgetsFactory;
import org.eclipse.che.ide.extension.machine.client.perspective.terminal.container.TerminalContainer;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.machine.appliance.recipe.RecipeTabPresenter;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.machine.appliance.server.ServerPresenter;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.machine.appliance.sufficientinfo.MachineInfoPresenter;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.machine.panel.MachinePanelPresenter;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.machine.panel.MachinePanelView;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.recipe.RecipePartPresenter;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.recipe.RecipePartView;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.recipe.container.RecipesContainerPresenter;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab.Tab;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab.container.TabContainerPresenter;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab.container.TabContainerView;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab.container.TabContainerView.TabSelectHandler;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab.header.TabHeader;
import org.eclipse.che.ide.part.PartStackPresenter.PartStackEventHandler;
import org.eclipse.che.ide.part.PartsComparator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Dmitry Shnurenko
 * @author Valeriy Svydenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class MachineAppliancePresenterTest {

    private final static String SOME_TEXT = "someText";

    //constructor mocks
    @Mock
    private EventBus                    eventBus;
    @Mock
    private PartStackEventHandler       partStackEventHandler;
    @Mock
    private MachineApplianceView        view;
    @Mock
    private MachineLocalizationConstant locale;
    @Mock
    private WidgetsFactory              widgetsFactory;
    @Mock
    private EntityFactory               entityFactory;
    @Mock
    private TabItemFactory              tabItemFactory;
    @Mock
    private PartsComparator             comparator;
    @Mock
    private RecipesContainerPresenter   recipesContainerPresenter;
    @Mock
    private RecipeTabPresenter          recipeTabPresenter;

    @Mock
    private TerminalContainer     terminalContainer;
    @Mock
    private MachineInfoPresenter  infoPresenter;
    @Mock
    private ServerPresenter       serverPresenter;
    @Mock
    private TabContainerPresenter tabContainer;

    //additional mocks
    @Mock
    private TabHeader              tabHeader;
    @Mock
    private Tab                    terminalTab;
    @Mock
    private Tab                    infoTab;
    @Mock
    private Tab                    serverTab;
    @Mock
    private Tab                    recipeTab;
    @Mock
    private TabContainerView       tabContainerView;
    @Mock
    private MachineEntity          machine;
    @Mock
    private Widget                 widget;
    @Mock
    private ActivePartChangedEvent event;
    @Mock
    private RecipePartPresenter    recipePartPresenter;
    @Mock
    private MachinePanelPresenter  machinePanelPresenter;
    @Mock
    private MachinePanelView       machinePanelView;
    @Mock
    private RecipePartView         recipePartView;
    @Mock
    private AcceptsOneWidget       container;

    @Captor
    private ArgumentCaptor<TabSelectHandler> handlerCaptor;

    private MachineAppliancePresenter presenter;

    @Before
    public void setUp() {
        when(machine.getId()).thenReturn(SOME_TEXT);
        when(tabContainer.getView()).thenReturn(tabContainerView);
        when(tabContainerView.asWidget()).thenReturn(widget);
        when(recipesContainerPresenter.getView()).thenReturn(recipePartView);

        when(locale.tabTerminal()).thenReturn(SOME_TEXT);
        when(locale.tabInfo()).thenReturn(SOME_TEXT);
        when(locale.tabServer()).thenReturn(SOME_TEXT);
        when(locale.tabRecipe()).thenReturn(SOME_TEXT);

        when(widgetsFactory.createTabHeader(SOME_TEXT)).thenReturn(tabHeader);

        when(entityFactory.createTab(Matchers.<TabHeader>anyObject(),
                                     eq(terminalContainer),
                                     Matchers.<TabSelectHandler>anyObject())).thenReturn(terminalTab);

        when(entityFactory.createTab(Matchers.<TabHeader>anyObject(),
                                     eq(infoPresenter),
                                     Matchers.<TabSelectHandler>anyObject())).thenReturn(infoTab);

        when(entityFactory.createTab(Matchers.<TabHeader>anyObject(),
                                     eq(serverPresenter),
                                     Matchers.<TabSelectHandler>anyObject())).thenReturn(serverTab);

        when(entityFactory.createTab(Matchers.<TabHeader>anyObject(),
                                     eq(recipeTabPresenter),
                                     Matchers.<TabSelectHandler>anyObject())).thenReturn(recipeTab);

        presenter = new MachineAppliancePresenter(eventBus,
                                                  comparator,
                                                  partStackEventHandler,
                                                  view,
                                                  locale,
                                                  widgetsFactory,
                                                  entityFactory,
                                                  tabItemFactory,
                                                  infoPresenter,
                                                  recipesContainerPresenter,
                                                  serverPresenter,
                                                  recipeTabPresenter,
                                                  tabContainer);
    }

    @Test
    public void constructorShouldBeVerified() {
        verify(widgetsFactory, times(2)).createTabHeader(SOME_TEXT);

        verify(entityFactory).createTab(eq(tabHeader), eq(infoPresenter), Matchers.<TabSelectHandler>anyObject());
        verify(entityFactory).createTab(eq(tabHeader), eq(serverPresenter), Matchers.<TabSelectHandler>anyObject());

        verify(locale).tabTerminal();
        verify(locale).tabInfo();
        verify(locale).tabServer();

        verify(tabContainer).addTab(infoTab);
        verify(tabContainer).addTab(serverTab);

        verify(tabContainer).getView();

        verify(view).addContainer(tabContainerView);
        verify(view).addContainer(recipePartView);
    }


    private void callAndVerifyHandler() {
        presenter.showAppliance(machine);

        verify(entityFactory).createTab(eq(tabHeader), eq(infoPresenter), handlerCaptor.capture());
        handlerCaptor.getValue().onTabSelected();

        verify(machine, times(2)).getId();
    }

    @Test
    public void infoHandlerShouldBePerformed() {
        callAndVerifyHandler();

        verify(locale, times(2)).tabInfo();
    }

    @Test
    public void serverHandlerShouldBePerformed() {
        callAndVerifyHandler();

        verify(locale).tabServer();
    }

    @Test
    public void infoShouldBeShown() {
        reset(tabContainer);
        when(tabContainer.getView()).thenReturn(tabContainerView);

        presenter.showAppliance(machine);

        verify(tabContainer).getView();
        verify(view).showContainer(tabContainerView);

        verify(tabContainer).showTab(SOME_TEXT);
//        verify(terminalContainer).addOrShowTerminal(machine);
        verify(infoPresenter).update(machine);
        verify(serverPresenter).updateInfo(machine);
    }

    @Test
    public void viewShouldBeSetToContainer() {
        presenter.go(container);

        verify(container).setWidget(view);
    }

    @Test
    public void recipeEditorShouldBeShowed() throws Exception {
        when(event.getActivePart()).thenReturn(recipePartPresenter);
        when(recipePartPresenter.getView()).thenReturn(recipePartView);

        presenter.onActivePartChanged(event);

        verify(view).showContainer(recipePartView);
    }

    @Test
    public void machinesShouldBeShowed() throws Exception {
        when(event.getActivePart()).thenReturn(machinePanelPresenter);

        presenter.showAppliance(machine);
        reset(view);

        presenter.onActivePartChanged(event);

        verify(view).showContainer(tabContainerView);
    }

    @Test
    public void stubShouldBeShownWhenMachineIsNull() {
        when(event.getActivePart()).thenReturn(machinePanelPresenter);

        presenter.onActivePartChanged(event);

        verify(view, never()).showContainer(tabContainerView);
        verify(view).showStub(anyString());
    }
}
