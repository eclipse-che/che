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
package org.eclipse.che.ide.command.editor.page.renderer;

import java.util.Map;
import org.eclipse.che.ide.api.console.OutputConsoleRenderer;
import org.eclipse.che.ide.api.mvp.View;

/**
 * The view for {@link RenderersPage}.
 *
 * @author Victor Rubezhny
 */
public interface RenderersPageView extends View<RenderersPageView.ActionDelegate> {

  /** Sets the applicable output renderers. */
  void setRenderers(Map<OutputConsoleRenderer, Boolean> renderers);

  /** The action delegate for this view. */
  interface ActionDelegate {

    /** Called when applicable project has been changed. */
    void onApplicableRendererChanged(OutputConsoleRenderer renderer, boolean applicable);
  }
}
