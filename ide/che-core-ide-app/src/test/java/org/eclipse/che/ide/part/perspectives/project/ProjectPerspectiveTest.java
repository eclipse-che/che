/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.part.perspectives.project;

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
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.parts.PartStackView;
import org.eclipse.che.ide.command.explorer.CommandsExplorerPresenter;
import org.eclipse.che.ide.part.PartStackPresenter;
import org.eclipse.che.ide.part.PartStackPresenterFactory;
import org.eclipse.che.ide.part.PartStackViewFactory;
import org.eclipse.che.ide.part.WorkBenchControllerFactory;
import org.eclipse.che.ide.part.WorkBenchPartController;
import org.eclipse.che.ide.part.editor.multipart.EditorMultiPartStackPresenter;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.part.perspectives.general.PerspectiveViewImpl;
import org.eclipse.che.ide.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.providers.DynaProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

/** @author Dmitry Shnurenko */
@RunWith(GwtMockitoTestRunner.class)
public class ProjectPerspectiveTest {

  // constructor mocks
  @Mock private PerspectiveViewImpl view;
  @Mock private PartStackViewFactory partViewFactory;
  @Mock private WorkBenchControllerFactory controllerFactory;
  @Mock private PartStackPresenterFactory stackPresenterFactory;
  @Mock private EventBus eventBus;
  @Mock private EditorMultiPartStackPresenter editorMultiPartStackPresenter;

  // additional mocks
  @Mock private FlowPanel panel;
  @Mock private SplitLayoutPanel layoutPanel;
  @Mock private SimplePanel simplePanel;
  @Mock private SimpleLayoutPanel simpleLayoutPanel;
  @Mock private WorkBenchPartController workBenchController;
  @Mock private PartStackView partStackView;
  @Mock private PartStackPresenter partStackPresenter;
  @Mock private AcceptsOneWidget container;
  @Mock private DynaProvider dynaProvider;
  @Mock private NotificationManager notificationManager;
  @Mock private ProjectExplorerPresenter projectExplorerPresenter;
  @Mock private CommandsExplorerPresenter commandsExplorerPresenter;
  @Mock private ProcessesPanelPresenter processesPanelPresenter;

  private ProjectPerspective perspective;

  @Before
  public void setUp() {

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

    when(partViewFactory.create(org.mockito.ArgumentMatchers.<FlowPanel>anyObject()))
        .thenReturn(partStackView);

    when(stackPresenterFactory.create(
            org.mockito.ArgumentMatchers.<PartStackView>anyObject(),
            org.mockito.ArgumentMatchers.<WorkBenchPartController>anyObject()))
        .thenReturn(partStackPresenter);

    perspective =
        new ProjectPerspective(
            view,
            editorMultiPartStackPresenter,
            stackPresenterFactory,
            partViewFactory,
            controllerFactory,
            eventBus,
            dynaProvider,
            projectExplorerPresenter,
            commandsExplorerPresenter,
            notificationManager,
            processesPanelPresenter);
  }

  @Test
  public void perspectiveShouldBeDisplayed() {
    perspective.go(container);

    verify(view).getEditorPanel();
    verify(view, times(2)).getNavigationPanel();
    verify(view, times(2)).getToolPanel();
    verify(view, times(2)).getInformationPanel();

    verify(partStackPresenter, times(2)).go(simplePanel);
    verify(partStackPresenter).go(simpleLayoutPanel);
    verify(container).setWidget(view);
  }
}
