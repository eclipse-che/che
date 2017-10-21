/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.part;

import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwtmockito.GwtMockitoTestRunner;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.parts.PartStackUIResources;
import org.eclipse.che.ide.api.parts.PartStackView.ActionDelegate;
import org.eclipse.che.ide.api.parts.PartStackView.TabItem;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

/**
 * @author Dmitry Shnurenko
 * @author Valeriy Svydenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class PartStackViewImplTest {

  @Mock(answer = RETURNS_DEEP_STUBS)
  PartStackUIResources resources;

  @Mock private CoreLocalizationConstant localizationConstant;

  @Mock FlowPanel tabsPanel;

  // additional mocks
  @Mock private MouseDownEvent event;
  @Mock private ContextMenuEvent contextMenuEvent;

  @Mock private ActionDelegate delegate;

  @Mock private TabItem tabItem;
  @Mock private TabItem tabItem2;
  @Mock private PartPresenter partPresenter;
  @Mock private PartPresenter partPresenter2;
  @Mock private IsWidget widget;
  @Mock private IsWidget widget2;
  @Mock private Widget focusedWidget;
  @Mock private Element element;

  @Captor private ArgumentCaptor<AcceptsOneWidget> contentCaptor;

  private PartStackViewImpl view;

  @Before
  public void setUp() {
    when(focusedWidget.getElement()).thenReturn(element);
    when(tabItem.getView()).thenReturn(widget);
    when(partPresenter.getView()).thenReturn(widget);

    view = new PartStackViewImpl(resources, localizationConstant);
    view.setDelegate(delegate);
  }

  @Test
  public void onPartStackMouseShouldBeDown() {
    view.onMouseDown(event);
    verify(delegate).onRequestFocus();
  }

  @Test
  public void onPartStackContextMenuShouldBeClicked() {
    view.onContextMenu(contextMenuEvent);
    verify(delegate).onRequestFocus();
  }

  @Test
  public void tabShouldBeAdded() {
    view.addTab(tabItem, partPresenter);

    verify(tabItem).getView();
    verify(partPresenter).go(any());
  }

  @Test
  public void tabShouldBeSelected() {
    view.addTab(tabItem, partPresenter);
    view.selectTab(partPresenter);

    verify(tabItem).select();
    verify(delegate).onRequestFocus();
  }
}
