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
package org.eclipse.che.ide.ext.java.client.settings.property;

import static junit.framework.TestCase.assertEquals;
import static org.eclipse.che.ide.ext.java.client.settings.compiler.ErrorWarningsOptions.DEAD_CODE;
import static org.eclipse.che.ide.ext.java.client.settings.property.PropertyWidgetImpl.ERROR;
import static org.eclipse.che.ide.ext.java.client.settings.property.PropertyWidgetImpl.IGNORE;
import static org.eclipse.che.ide.ext.java.client.settings.property.PropertyWidgetImpl.WARNING;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwtmockito.GwtMockitoTestRunner;
import org.eclipse.che.ide.ext.java.client.settings.compiler.ErrorWarningsOptions;
import org.eclipse.che.ide.ext.java.client.settings.property.PropertyWidget.ActionDelegate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

/** @author Dmitry Shnurenko */
@RunWith(GwtMockitoTestRunner.class)
public class PropertyWidgetImplTest {

  private static final String SOME_TEXT = "someText";

  @Mock private PropertyNameManager nameManager;
  @Mock private ActionDelegate delegate;
  @Mock private ChangeEvent event;

  private PropertyWidgetImpl widget;

  @Before
  public void setUp() {
    when(nameManager.getName(org.mockito.ArgumentMatchers.<ErrorWarningsOptions>anyObject()))
        .thenReturn(SOME_TEXT);

    widget = new PropertyWidgetImpl(nameManager, DEAD_CODE);
    widget.setDelegate(delegate);
  }

  @Test
  public void constructorShouldBeVerified() {
    verify(nameManager).getName(DEAD_CODE);
    verify(widget.title).setText(SOME_TEXT);

    verify(widget.property).addItem(IGNORE);
    verify(widget.property).addItem(WARNING);
    verify(widget.property).addItem(ERROR);
  }

  @Test
  public void propertyValueShouldBeSelected() {
    when(widget.property.getItemCount()).thenReturn(3);
    when(widget.property.getValue(1)).thenReturn(WARNING);

    widget.selectPropertyValue(WARNING);

    verify(widget.property).setItemSelected(1, true);
  }

  @Test
  public void onPropertyShouldBeChanged() {
    when(widget.property.getSelectedIndex()).thenReturn(1);
    when(widget.property.getValue(1)).thenReturn(SOME_TEXT);

    widget.onPropertyChanged(event);

    verify(delegate).onPropertyChanged();
  }

  @Test
  public void optionIdShouldBeReturned() {
    assertEquals(widget.getOptionId(), DEAD_CODE);
  }
}
