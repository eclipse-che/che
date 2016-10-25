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

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
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

//    @Test
//    public void shouldStoreActivePartInPartStack() throws Exception {
//        when(partStackPresenter.getActivePart()).thenReturn(activePart);
//        Map<PartPresenter, Constraints> map = new HashMap<>();
//        map.put(partPresenter, null);
//        when(partStackPresenter.getParts()).thenReturn(map);
//
//        JsonObject state = perspective.getState();
//
//        JsonObject partStacks = state.getObject("PART_STACKS");
//        JsonObject information = partStacks.getObject("INFORMATION");
//        assertThat(information.getString("ACTIVE_PART")).isNotNull().isNotEmpty().isEqualTo(activePart.getClass().getName());
//    }

    @Test
    public void shouldNotStoreEditorPartStack() throws Exception {
        JsonObject state = perspective.getState();
        JsonObject partStacks = state.getObject("PART_STACKS");
        String[] keys = partStacks.keys();
        assertThat(keys).containsOnly("INFORMATION", "NAVIGATION", "TOOLING");
    }

//    @Test
//    public void shouldStorePartStackSize() throws Exception {
//        Map<PartPresenter, Constraints> map = new HashMap<>();
//        map.put(partPresenter, null);
//        when(partStackPresenter.getParts()).thenReturn(map);
//        when(workBenchController.getSize()).thenReturn(42d, 24d, 13d);
//
//        JsonObject state = perspective.getState();
//
//        JsonObject partStacks = state.getObject("PART_STACKS");
//        JsonObject partStacksObject = partStacks.getObject("INFORMATION");
//        double size = partStacksObject.getNumber("SIZE");
//        assertThat(size).isEqualTo(42d);
//
//        partStacksObject = partStacks.getObject("NAVIGATION");
//        size = partStacksObject.getNumber("SIZE");
//        assertThat(size).isEqualTo(24d);
//
//        partStacksObject = partStacks.getObject("TOOLING");
//        size = partStacksObject.getNumber("SIZE");
//        assertThat(size).isEqualTo(13d);
//
//    }

//    @Test
//    public void shouldStorePartPresenterClassName() throws Exception {
//        Map<PartPresenter, Constraints> map = new HashMap<>();
//        map.put(partPresenter, null);
//        when(partStackPresenter.getParts()).thenReturn(map);
//
//        JsonObject state = perspective.getState();
//
//
//        JsonObject partStacks = state.getObject("PART_STACKS");
//        JsonObject information = partStacks.getObject("INFORMATION");
//        JsonArray parts = information.getArray("PARTS");
//        assertThat(parts).isNotNull();
//
//        JsonObject part = parts.getObject(0);
//        assertThat(part.getString("CLASS")).isNotNull().isNotEmpty();
//    }

//    @Test
//    public void shouldStorePartPresenterConstrain() throws Exception {
//        Map<PartPresenter, Constraints> map = new HashMap<>();
//        map.put(partPresenter, Constraints.FIRST);
//        when(partStackPresenter.getParts()).thenReturn(map);
//
//        JsonObject state = perspective.getState();
//
//
//        JsonObject partStacks = state.getObject("PART_STACKS");
//        JsonObject information = partStacks.getObject("INFORMATION");
//        JsonArray parts = information.getArray("PARTS");
//        assertThat(parts).isNotNull();
//
//        JsonObject part = parts.getObject(0);
//        assertThat(part.getObject("CONSTRAINTS")).isNotNull();
//    }

//    @Test
//    public void shouldStorePartStackHiddenState() throws Exception {
//        Map<PartPresenter, Constraints> map = new HashMap<>();
//        map.put(partPresenter, null);
//        when(partStackPresenter.getParts()).thenReturn(map);
//        when(workBenchController.getSize()).thenReturn(42d, 24d, 13d);
//        when(workBenchController.isHidden()).thenReturn(true, false, true);
//
//        JsonObject state = perspective.getState();
//
//        JsonObject partStacks = state.getObject("PART_STACKS");
//        JsonObject partStacksObject = partStacks.getObject("INFORMATION");
//        boolean hidden = partStacksObject.getBoolean("HIDDEN");
//        assertThat(hidden).isTrue();
//
//        partStacksObject = partStacks.getObject("NAVIGATION");
//        hidden = partStacksObject.getBoolean("HIDDEN");
//        assertThat(hidden).isFalse();
//
//        partStacksObject = partStacks.getObject("TOOLING");
//        hidden = partStacksObject.getBoolean("HIDDEN");
//        assertThat(hidden).isTrue();
//    }

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
