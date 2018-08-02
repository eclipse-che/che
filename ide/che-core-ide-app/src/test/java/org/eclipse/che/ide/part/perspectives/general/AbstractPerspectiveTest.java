/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.part.perspectives.general;

import static org.eclipse.che.ide.api.parts.PartStackType.EDITING;
import static org.eclipse.che.ide.api.parts.PartStackType.INFORMATION;
import static org.eclipse.che.ide.api.parts.PartStackType.NAVIGATION;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwtmockito.GwtMockitoTestRunner;
import com.google.web.bindery.event.shared.EventBus;
import java.util.Arrays;
import javax.validation.constraints.NotNull;
import javax.ws.rs.NotSupportedException;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.editor.AbstractEditorPresenter;
import org.eclipse.che.ide.api.parts.ActivePartChangedEvent;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.parts.PartStack;
import org.eclipse.che.ide.api.parts.PartStackView;
import org.eclipse.che.ide.part.PartStackPresenter;
import org.eclipse.che.ide.part.PartStackPresenterFactory;
import org.eclipse.che.ide.part.PartStackViewFactory;
import org.eclipse.che.ide.part.WorkBenchControllerFactory;
import org.eclipse.che.ide.part.WorkBenchPartController;
import org.eclipse.che.providers.DynaProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

/** @author Dmitry Shnurenko */
@RunWith(GwtMockitoTestRunner.class)
public class AbstractPerspectiveTest {

  private static final String SOME_TEXT = "someText";

  // constructor mocks
  @Mock private PerspectiveViewImpl view;
  @Mock private PartStackPresenterFactory stackPresenterFactory;
  @Mock private PartStackViewFactory partStackViewFactory;
  @Mock private WorkBenchControllerFactory controllerFactory;
  @Mock private EventBus eventBus;

  // additional mocks
  @Mock private FlowPanel panel;
  @Mock private SplitLayoutPanel layoutPanel;
  @Mock private SimplePanel simplePanel;
  @Mock private SimpleLayoutPanel simpleLayoutPanel;
  @Mock private PartStackView partStackView;
  @Mock private PartStackPresenter extraPartStackPresenter;
  @Mock private PartStackPresenter partStackPresenter;
  @Mock private WorkBenchPartController workBenchController;
  @Mock private PartPresenter partPresenter;
  @Mock private Constraints constraints;
  @Mock private PartPresenter navigationPart;
  @Mock private PartPresenter activePart;
  @Mock private AbstractEditorPresenter editorPart;
  @Mock private DynaProvider dynaProvider;

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

    when(controllerFactory.createController(
            org.mockito.ArgumentMatchers.<SplitLayoutPanel>anyObject(),
            org.mockito.ArgumentMatchers.<SimplePanel>anyObject()))
        .thenReturn(workBenchController);

    when(partStackViewFactory.create(org.mockito.ArgumentMatchers.<FlowPanel>anyObject()))
        .thenReturn(partStackView);

    when(stackPresenterFactory.create(
            org.mockito.ArgumentMatchers.<PartStackView>anyObject(),
            org.mockito.ArgumentMatchers.<WorkBenchPartController>anyObject()))
        .thenReturn(partStackPresenter);

    perspective =
        new DummyPerspective(
            view,
            stackPresenterFactory,
            partStackViewFactory,
            controllerFactory,
            eventBus,
            extraPartStackPresenter,
            partStackPresenter,
            dynaProvider);
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

    verify(partStackViewFactory, times(3)).create(panel);

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

    verify(partStackPresenter).hide();
  }

  @Test
  public void partShouldBeMaximized() {
    perspective.onMaximize(partStackPresenter);

    verify(partStackPresenter).maximize();
  }

  @Test
  public void partShouldBeMinimized() {
    perspective.onMaximize(extraPartStackPresenter);

    verify(partStackPresenter, times(3)).minimize();
    verify(extraPartStackPresenter).maximize();
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

  public static class DummyPerspective extends AbstractPerspective {

    public DummyPerspective(
        @NotNull PerspectiveViewImpl view,
        @NotNull PartStackPresenterFactory stackPresenterFactory,
        @NotNull PartStackViewFactory partViewFactory,
        @NotNull WorkBenchControllerFactory controllerFactory,
        @NotNull EventBus eventBus,
        PartStackPresenter extraPartStackPresenter,
        PartStackPresenter editingPartStackPresenter,
        DynaProvider dynaProvider) {
      super(
          SOME_TEXT,
          view,
          stackPresenterFactory,
          partViewFactory,
          controllerFactory,
          eventBus,
          dynaProvider);

      if (extraPartStackPresenter != null) {
        partStacks.put(NAVIGATION, extraPartStackPresenter);
      }

      if (editingPartStackPresenter != null) {
        partStacks.put(EDITING, editingPartStackPresenter);
      }
    }

    @Override
    public String getPerspectiveId() {
      return SOME_TEXT;
    }

    @Override
    public String getPerspectiveName() {
      return "Dummy";
    }

    @Override
    public void go(@NotNull AcceptsOneWidget container) {
      throw new NotSupportedException(
          "This method will be tested in the class which extends AbstractPerspective");
    }
  }
}
