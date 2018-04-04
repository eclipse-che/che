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
package org.eclipse.che.ide.ui.toolbar;

import java.util.HashMap;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.Presentation;

/** @author Evgen Vidolob */
public class PresentationFactory {

  private final HashMap<Action, Presentation> myAction2Presentation;

  public PresentationFactory() {
    myAction2Presentation = new HashMap<>();
  }

  public final Presentation getPresentation(@NotNull Action action) {
    Presentation presentation = myAction2Presentation.get(action);
    if (presentation == null) {
      presentation = action.getTemplatePresentation().clone();
      myAction2Presentation.put(action, presentation);
    }
    return presentation;
  }
}
