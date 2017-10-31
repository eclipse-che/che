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

package org.eclipse.che.ide.js.impl.action;

import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.js.api.resources.ImageRegistry;

/** @author Yevhen Vydolob */
public class JsActionGroup extends DefaultActionGroup {

  private final ImageRegistry imageRegistry;
  private String imageId;

  public JsActionGroup(ActionManager actionManager, ImageRegistry imageRegistry) {
    super(actionManager);
    this.imageRegistry = imageRegistry;
  }

  @Override
  public void update(ActionEvent e) {
    if (imageId != null) {
      e.getPresentation().setImageElement(imageRegistry.getImage(imageId));
    }
  }

  public void setImageId(String imageId) {
    this.imageId = imageId;
  }
}
