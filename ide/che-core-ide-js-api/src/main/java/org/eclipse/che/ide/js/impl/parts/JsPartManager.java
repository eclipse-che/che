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

package org.eclipse.che.ide.js.impl.parts;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.parts.PartStackType;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.js.api.parts.Part;
import org.eclipse.che.ide.js.api.parts.PartManager;
import org.eclipse.che.ide.js.api.resources.ImageRegistry;

/** @author Yevhen Vydolob */
@Singleton
public class JsPartManager implements PartManager {

  private final Provider<WorkspaceAgent> workspaceAgent;
  private final ImageRegistry imageRegistry;

  private final BiMap<Part, JsPartPresenter> presenters = HashBiMap.create();

  @Inject
  public JsPartManager(Provider<WorkspaceAgent> workspaceAgent, ImageRegistry imageRegistry) {
    this.workspaceAgent = workspaceAgent;
    this.imageRegistry = imageRegistry;
  }

  @Override
  public void activatePart(Part part) {
    if (presenters.containsKey(part)) {
      JsPartPresenter presenter = presenters.get(part);
      workspaceAgent.get().setActivePart(presenter);
    }
  }

  @Override
  public boolean isActivePart(Part part) {
    PartPresenter activePart = workspaceAgent.get().getActivePart();
    if (activePart == null) {
      return false;
    }

    if (presenters.inverse().containsKey(activePart)) {
      Part actPart = presenters.inverse().get(activePart);
      return actPart.equals(part);
    }

    return false;
  }

  @Override
  public void openPart(Part part, PartStackType type) {
    if (!presenters.containsKey(part)) {
      JsPartPresenter presenter = new JsPartPresenter(part, imageRegistry);
      presenters.put(part, presenter);
      workspaceAgent.get().openPart(presenter, type);
    }
  }

  @Override
  public void hidePart(Part part) {
    if (presenters.containsKey(part)) {
      JsPartPresenter presenter = presenters.get(part);
      workspaceAgent.get().hidePart(presenter);
    }
  }

  @Override
  public void removePart(Part part) {
    if (presenters.containsKey(part)) {
      JsPartPresenter presenter = presenters.get(part);
      workspaceAgent.get().removePart(presenter);
    }
  }
}
