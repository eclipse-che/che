/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ui.button;

import static org.eclipse.che.ide.ui.button.ConsoleButtonImpl.TOP_TOOLTIP_SHIFT;
import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwtmockito.GwtMockitoTestRunner;
import org.eclipse.che.ide.ui.button.ConsoleButton.ActionDelegate;
import org.eclipse.che.ide.ui.tooltip.TooltipWidget;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * @author Andrey Plotnikov
 * @author Valeriy Svydenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class ConsoleButtonImplTest {

  private static final String SOME_MESSAGE = "some message";

  @Mock private ButtonResources resources;
  @Mock private TooltipWidget tooltip;

  @Mock private ButtonResources.Css runnerCss;

  private ConsoleButtonImpl widget;

  @Before
  public void setUp() throws Exception {
    when(resources.buttonCss()).thenReturn(runnerCss);

    widget =
        new ConsoleButtonImpl(
            resources, tooltip, SOME_MESSAGE, mock(SVGResource.class, RETURNS_MOCKS));
  }

  @Test
  public void constructorActionsShouldBeValidated() throws Exception {
    verify(tooltip).setDescription(SOME_MESSAGE);

    verify(runnerCss).activeConsoleButton();
    verify(runnerCss).whiteColor();
  }

  @Test
  public void checkStatusShouldBeChangedToCheckedStyle() throws Exception {
    reset(runnerCss);

    widget.setCheckedStatus(true);

    verify(runnerCss).activeConsoleButton();
    verify(runnerCss).whiteColor();
  }

  @Test
  public void checkStatusShouldBeChangedToUncheckedStyle() throws Exception {
    reset(runnerCss);

    widget.setCheckedStatus(false);

    verify(runnerCss).activeConsoleButton();
    verify(runnerCss).whiteColor();
  }

  @Test
  public void clickActionShouldBeDelegated() throws Exception {
    ActionDelegate delegate = mock(ActionDelegate.class);

    widget.setDelegate(delegate);
    widget.onClick(mock(ClickEvent.class));

    verify(delegate).onButtonClicked();
  }

  @Test
  public void tooltipShouldBeHidden() throws Exception {
    widget.onMouseOut(mock(MouseOutEvent.class));

    verify(tooltip).hide();
  }

  @Test
  public void tooltipShouldBeShown() throws Exception {
    widget.onMouseOver(mock(MouseOverEvent.class));

    verify(tooltip).setPopupPosition(0, TOP_TOOLTIP_SHIFT);
    verify(tooltip).show();
  }
}
