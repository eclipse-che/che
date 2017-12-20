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
package org.eclipse.che.ide.api.action;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwtmockito.GwtMockitoTestRunner;
import java.util.Arrays;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.ws.rs.NotSupportedException;
import org.eclipse.che.ide.api.parts.PerspectiveManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.vectomatic.dom.svg.ui.SVGResource;

/** @author Dmitry Shnurenko */
@RunWith(GwtMockitoTestRunner.class)
public class AbstractPerspectiveActionTest {

  private static final String SOME_TEXT = "someText";

  @Mock private PerspectiveManager manager;
  @Mock private ActionEvent event;

  private DummyAction dummyAction;

  @Before
  public void setUp() {
    dummyAction = new DummyAction(Arrays.asList(SOME_TEXT), SOME_TEXT, SOME_TEXT, null);
  }

  @Test
  public void actionShouldBePerformed() {
    Presentation presentation = new Presentation();
    when(event.getPresentation()).thenReturn(presentation);
    when(manager.getPerspectiveId()).thenReturn("123");

    dummyAction.update(event);

    verify(event).getPresentation();
    verify(manager).getPerspectiveId();
  }

  private class DummyAction extends AbstractPerspectiveAction {

    public DummyAction(
        @NotNull List<String> activePerspectives,
        @NotNull String tooltip,
        @NotNull String description,
        @NotNull SVGResource icon) {
      super(activePerspectives, tooltip, description, icon);
      perspectiveManager = () -> AbstractPerspectiveActionTest.this.manager;
      appContext = () -> null;
    }

    @Override
    public void updateInPerspective(@NotNull ActionEvent event) {
      throw new NotSupportedException("Method isn't supported in current mode...");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      throw new NotSupportedException("Method isn't supported in current mode...");
    }
  }
}
