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
