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
package org.eclipse.che.ide.ext.git.client.plugins;

import static org.eclipse.che.ide.api.editor.gutter.Gutters.VCS_CHANGE_MARKERS_GUTTER;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import elemental.html.DivElement;
import org.eclipse.che.api.git.shared.EditedRegionType;
import org.eclipse.che.ide.api.editor.gutter.Gutter;
import org.eclipse.che.ide.api.vcs.VcsChangeMarkerRender;
import org.eclipse.che.ide.ext.git.client.GitResources;
import org.eclipse.che.ide.util.dom.Elements;

public class GitChangeMarkerRender implements VcsChangeMarkerRender {
  private final GitResources resources;
  private final Gutter hasGutter;

  @AssistedInject
  public GitChangeMarkerRender(GitResources resources, @Assisted final Gutter hasGutter) {
    this.resources = resources;
    this.hasGutter = hasGutter;

    resources.changeMarkersCSS().ensureInjected();
  }

  @Override
  public void addChangeMarker(int lineStart, int lineEnd, EditedRegionType type) {
    DivElement element;
    GitResources.Css css = resources.changeMarkersCSS();
    switch (type) {
      case INSERTION:
        {
          element = Elements.createDivElement(css.marker(), css.insertion());
          break;
        }
      case MODIFICATION:
        {
          element = Elements.createDivElement(css.marker(), css.modification());
          break;
        }
      case DELETION:
        {
          element = Elements.createDivElement(css.marker(), css.deletion());
          break;
        }
      default:
        {
          return;
        }
    }
    hasGutter.addGutterItem(lineStart, lineEnd, VCS_CHANGE_MARKERS_GUTTER, element);
  }

  @Override
  public void clearAllChangeMarkers() {
    hasGutter.clearGutter(VCS_CHANGE_MARKERS_GUTTER);
  }
}
