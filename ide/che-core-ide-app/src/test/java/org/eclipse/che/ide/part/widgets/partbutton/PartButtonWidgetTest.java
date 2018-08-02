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
package org.eclipse.che.ide.part.widgets.partbutton;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwtmockito.GwtMockitoTestRunner;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.part.widgets.partbutton.PartButton.ActionDelegate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.vectomatic.dom.svg.OMSVGSVGElement;
import org.vectomatic.dom.svg.ui.SVGResource;

/** @author Dmitry Shnurenko */
@RunWith(GwtMockitoTestRunner.class)
public class PartButtonWidgetTest {

  private static final String SOME_TEXT = "someText";

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private Resources resources;

  @Mock private PartPresenter partPresenter;
  @Mock private IsWidget isWidget;
  @Mock private SVGResource svgResource;
  @Mock private OMSVGSVGElement svg;
  @Mock private ActionDelegate delegate;

  private PartButtonWidget partButton;

  @Before
  public void setUp() {
    partButton = new PartButtonWidget(SOME_TEXT);
    partButton.setDelegate(delegate);
  }

  @Test
  public void onPartButtonShouldBeClicked() {
    ClickEvent event = mock(ClickEvent.class);

    partButton.onClick(event);

    verify(delegate).onTabClicked(partButton);
  }
}
