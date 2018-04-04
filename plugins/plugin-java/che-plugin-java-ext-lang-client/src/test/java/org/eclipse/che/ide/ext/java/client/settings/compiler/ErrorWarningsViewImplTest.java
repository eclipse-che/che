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
package org.eclipse.che.ide.ext.java.client.settings.compiler;

import static org.mockito.Mockito.verify;

import com.google.gwtmockito.GwtMockitoTestRunner;
import org.eclipse.che.ide.ext.java.client.settings.property.PropertyWidget;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

/** @author Dmitry Shnurenko */
@RunWith(GwtMockitoTestRunner.class)
public class ErrorWarningsViewImplTest {

  @Mock private PropertyWidget widget;

  @InjectMocks private ErrorWarningsViewImpl view;

  @Test
  public void propertyShouldBeAdded() {
    view.addProperty(widget);

    verify(view.properties).add(widget);
  }
}
