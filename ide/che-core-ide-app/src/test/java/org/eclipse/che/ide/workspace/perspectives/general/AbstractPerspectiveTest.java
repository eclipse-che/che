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

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwtmockito.GwtMockitoTestRunner;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.editor.AbstractEditorPresenter;
import org.eclipse.che.ide.api.event.ActivePartChangedEvent;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.parts.PartStack;
import org.eclipse.che.ide.api.parts.PartStackView;
import org.eclipse.che.ide.api.parts.PartStackView.TabPosition;
import org.eclipse.che.ide.part.PartStackPresenter;
import org.eclipse.che.ide.workspace.PartStackPresenterFactory;
import org.eclipse.che.ide.workspace.PartStackViewFactory;
import org.eclipse.che.ide.workspace.WorkBenchControllerFactory;
import org.eclipse.che.ide.workspace.WorkBenchPartController;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;

import javax.validation.constraints.NotNull;
import javax.ws.rs.NotSupportedException;
import java.util.Arrays;

import static org.eclipse.che.ide.api.parts.PartStackType.EDITING;
import static org.eclipse.che.ide.api.parts.PartStackType.INFORMATION;
import static org.eclipse.che.ide.api.parts.PartStackView.TabPosition.BELOW;
import static org.eclipse.che.ide.api.parts.PartStackView.TabPosition.LEFT;
import static org.eclipse.che.ide.api.parts.PartStackView.TabPosition.RIGHT;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class AbstractPerspectiveTest {

    private final static String SOME_TEXT = "someText";

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

        when(partStackViewFactory.create(Matchers.<TabPosition>anyObject(),
                                         Matchers.<FlowPanel>anyObject())).thenReturn(partStackView);

        when(stackPresenterFactory.create(Matchers.<PartStackView>anyObject(),
                                          Matchers.<WorkBenchPartController>anyObject())).thenReturn(partStackPresenter);

        perspective = new DummyPerspective(view, stackPresenterFactory, partStackViewFactory, controllerFactory, eventBus);
    }

    @Test
    public void activePartShouldBeOpened() {
        perspective.openActivePart(INFORMATION);

        verify(partStackPresenter).openPreviousActivePart();
    }

    @Test
    public void constructorShouldBeVerified() {
        verify(view).getLeftPanel();
        verify(view).getBottomPanel();
        verify(view).getRightPanel();

        verify(partStackViewFactory).create(LEFT, panel);
        verify(partStackViewFactory).create(BELOW, panel);
        verify(partStackViewFactory).create(RIGHT, panel);

        verify(view, times(3)).getSplitPanel();
        verify(view).getNavigationPanel();
        verify(controllerFactory, times(2)).createController(layoutPanel, simplePanel);
        verify(controllerFactory).createController(layoutPanel, simpleLayoutPanel);
        verify(stackPresenterFactory, times(3)).create(partStackView, workBenchController);

        verify(eventBus).addHandler(ActivePartChangedEvent.TYPE, perspective);
    }

    @Test
    public void partShouldBeRemoved() {
        when(partStackPresenter.containsPart(partPresenter)).thenReturn(true);

        perspective.removePart(partPresenter);

        verify(partStackPresenter).removePart(partPresenter);
    }

    @Test
    public void perspectiveStateShouldBeStored() {
        perspective.onActivePartChanged(new ActivePartChangedEvent(editorPart));

        perspective.storeState();

        verify(editorPart).storeState();
    }

    @Test
    public void perspectiveStateShouldBeRestored() {
        perspective.onActivePartChanged(new ActivePartChangedEvent(editorPart));
        perspective.storeState();

        perspective.restoreState();

        verify(editorPart).restoreState();
    }

    @Test
    public void partShouldBeHided() {
        when(partStackPresenter.containsPart(partPresenter)).thenReturn(true);

        perspective.hidePart(partPresenter);

        verify(partStackPresenter).hidePart(partPresenter);
    }

    @Test
    public void partsShouldBeCollapsed() {
        perspective.maximizeCentralPart();

        verify(workBenchController, times(3)).getSize();
        verify(workBenchController, times(3)).setHidden(true);
    }

    @Test
    public void partsShouldBeRestored() {
        perspective.restoreParts();

        verify(workBenchController, times(3)).setSize(anyDouble());
    }

    @Test
    public void activePartShouldBeSet() {
        when(partStackPresenter.containsPart(partPresenter)).thenReturn(true);

        perspective.setActivePart(partPresenter);

        verify(partStackPresenter).setActivePart(partPresenter);
    }

    @Test
    public void activePartShouldBeSetWithType() {
        perspective.setActivePart(partPresenter, INFORMATION);

        verify(partStackPresenter).setActivePart(partPresenter);
    }

    @Test
    public void nullShouldBeReturnedWhenPartIsNotFound() {
        PartStack partStack = perspective.findPartStackByPart(partPresenter);

        assertSame(partStack, null);
    }

    @Test
    public void nullShouldBeFound() {
        when(partStackPresenter.containsPart(partPresenter)).thenReturn(true);

        PartStack partStack = perspective.findPartStackByPart(partPresenter);

        assertSame(partStack, partStackPresenter);
    }

    @Test
    public void partShouldBeAddedWithoutConstraints() {
        perspective.addPart(partPresenter, INFORMATION);

        verify(partStackPresenter).addPart(partPresenter, null);
    }

    @Test
    public void partShouldBeAddedWithConstraints() {
        perspective.addPart(partPresenter, INFORMATION, constraints);

        verify(partStackPresenter).addPart(partPresenter, constraints);
    }

    @Test
    public void partStackShouldBeReturned() {
        perspective.addPart(partPresenter, INFORMATION);

        PartStack partStack = perspective.getPartStack(INFORMATION);

        assertSame(partStack, partStackPresenter);
    }

    @Test
    public void partShouldBeAddedWithRules() {
        when(partPresenter.getRules()).thenReturn(Arrays.asList(SOME_TEXT));

        perspective.addPart(partPresenter, INFORMATION);

        verify(partStackPresenter).addPart(partPresenter, null);
    }


    private class DummyPerspective extends AbstractPerspective {

        private DummyPerspective(@NotNull PerspectiveViewImpl view,
                                 @NotNull PartStackPresenterFactory stackPresenterFactory,
                                 @NotNull PartStackViewFactory partViewFactory,
                                 @NotNull WorkBenchControllerFactory controllerFactory,
                                 @NotNull EventBus eventBus) {
            super(SOME_TEXT, view, stackPresenterFactory, partViewFactory, controllerFactory, eventBus);

            partStacks.put(EDITING, partStackPresenter);
        }

        @Override
        public void go(@NotNull AcceptsOneWidget container) {
            throw new NotSupportedException("This method will be tested in the class which extends AbstractPerspective");
        }
    }
}
