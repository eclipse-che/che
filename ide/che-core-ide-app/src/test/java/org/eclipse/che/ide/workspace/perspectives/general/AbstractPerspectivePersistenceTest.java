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
package org.eclipse.che.ide.workspace.perspectives.general;

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwtmockito.GwtMockitoTestRunner;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.editor.AbstractEditorPresenter;
import org.eclipse.che.ide.api.event.ActivePartChangedEvent;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.parts.PartStackView;
import org.eclipse.che.ide.part.PartStackPresenter;
import org.eclipse.che.ide.workspace.PartStackPresenterFactory;
import org.eclipse.che.ide.workspace.PartStackViewFactory;
import org.eclipse.che.ide.workspace.WorkBenchControllerFactory;
import org.eclipse.che.ide.workspace.WorkBenchPartController;
import org.eclipse.che.providers.DynaProvider;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Evgen Vidolob
 */
@RunWith(GwtMockitoTestRunner.class)
public class AbstractPerspectivePersistenceTest {
    //constructor mocks
    @Mock
    private PerspectiveViewImpl        view;
    @Mock
    private PartStackPresenterFactory  stackPresenterFactory;
    @Mock
    private PartStackViewFactory       partStackViewFactory;
    @Mock
    private WorkBenchControllerFactory controllerFactory;
    @Mock
    private EventBus                   eventBus;

    //additional mocks
    @Mock
    private FlowPanel               panel;
    @Mock
    private SplitLayoutPanel        layoutPanel;
    @Mock
    private SimplePanel             simplePanel;
    @Mock
    private SimpleLayoutPanel       simpleLayoutPanel;
    @Mock
    private PartStackView           partStackView;
    @Mock
    private PartStackPresenter      partStackPresenter;
    @Mock
    private WorkBenchPartController workBenchController;
    @Mock
    private PartPresenter           partPresenter;
    @Mock
    private Constraints             constraints;
    @Mock
    private PartPresenter           activePart;
    @Mock
    private AbstractEditorPresenter editorPart;
    @Mock
    private DynaProvider            dynaProvider;
    @Mock
    private Provider<PartPresenter> partProvider;


    private AbstractPerspective perspective;

    @Before
    public void setUp() throws Exception {
        when(view.getLeftPanel()).thenReturn(panel);
        when(view.getRightPanel()).thenReturn(panel);
        when(view.getBottomPanel()).thenReturn(panel);

        when(view.getSplitPanel()).thenReturn(layoutPanel);

        when(view.getNavigationPanel()).thenReturn(simplePanel);
        when(view.getInformationPanel()).thenReturn(simpleLayoutPanel);
        when(view.getToolPanel()).thenReturn(simplePanel);

        when(controllerFactory.createController(Matchers.<SplitLayoutPanel>anyObject(),
                                                Matchers.<SimplePanel>anyObject())).thenReturn(workBenchController);

        when(partStackViewFactory.create(Matchers.<PartStackView.TabPosition>anyObject(),
                                         Matchers.<FlowPanel>anyObject())).thenReturn(partStackView);

        when(stackPresenterFactory.create(Matchers.<PartStackView>anyObject(),
                                          Matchers.<WorkBenchPartController>anyObject())).thenReturn(partStackPresenter);

        perspective =
                new AbstractPerspectiveTest.DummyPerspective(view, stackPresenterFactory, partStackViewFactory, controllerFactory, eventBus,
                                                             partStackPresenter, dynaProvider);
        perspective.onActivePartChanged(new ActivePartChangedEvent(activePart));
    }

    @Test
    public void shouldStoreActivePart() throws Exception {
        JsonObject state = perspective.getState();
        assertThat(state).isNotNull();
        String activePart = state.getString("ACTIVE_PART");
        assertThat(activePart).isNotNull().isNotEmpty();
    }

    @Test
    public void shouldStoreParts() throws Exception {
        JsonObject state = perspective.getState();
        JsonObject partStacks = state.getObject("PART_STACKS");
        assertThat(partStacks).isNotNull();
    }

    @Test
    public void shouldNotStoreEditorPartStack() throws Exception {
        JsonObject state = perspective.getState();
        JsonObject partStacks = state.getObject("PART_STACKS");
        String[] keys = partStacks.keys();
        assertThat(keys).containsOnly("INFORMATION", "NAVIGATION", "TOOLING");
    }

    @Test
    public void shouldRestorePartStackSize() throws Exception {
        JsonObject state = Json.createObject();
        JsonObject parts = Json.createObject();
        state.put("PART_STACKS", parts);
        JsonObject partStack = Json.createObject();
        parts.put("INFORMATION", partStack);
        partStack.put("SIZE", 42);

        perspective.loadState(state);

        verify(workBenchController).setSize(42d);

    }

    @Test
    public void shouldRestoreHiddenPartStackState() throws Exception {
        JsonObject state = Json.createObject();
        JsonObject parts = Json.createObject();
        state.put("PART_STACKS", parts);
        JsonObject partStack = Json.createObject();
        parts.put("INFORMATION", partStack);
        partStack.put("HIDDEN", true);

        //PartStackPresenter should not be empty otherwise setHidden() will call twice
        final List<PartPresenter> partPresenters = new ArrayList<>();
        partPresenters.add(mock(PartPresenter.class));
        when(partStackPresenter.getParts()).thenAnswer(new Answer<List<? extends PartPresenter>>() {
            public List<? extends PartPresenter> answer(InvocationOnMock invocation) throws Throwable {
                return partPresenters;
            }

        });

        perspective.loadState(state);
        verify(workBenchController).setHidden(true);
    }

    @Test
    @Ignore //TODO
    public void shouldRestoreOpenedParts() throws Exception {
        JsonObject state = Json.createObject();
        JsonObject parts = Json.createObject();
        state.put("PART_STACKS", parts);
        JsonObject partStack = Json.createObject();
        parts.put("INFORMATION", partStack);
        JsonArray partsArray = Json.createArray();
        partStack.put("PARTS", partsArray);
        JsonObject part = Json.createObject();
        partsArray.set(0, part);
        part.put("CLASS", "foo.Bar");

        when(dynaProvider.<PartPresenter>getProvider(anyString())).thenReturn(partProvider);
        when(partProvider.get()).thenReturn(partPresenter);

        perspective.loadState(state);

        verify(dynaProvider).getProvider("foo.Bar");
        verify(partProvider).get();

        verify(partStackPresenter).addPart(partPresenter);

    }
}
