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
import org.eclipse.che.ide.api.action.BaseAction;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.ide.js.api.action.ActionData;
import org.eclipse.che.ide.js.api.action.PerformAction;
import org.eclipse.che.ide.js.api.action.UpdateAction;
import org.eclipse.che.ide.js.api.resources.ImageRegistry;

/** @author Yevhen Vydolob */
public class JsAction extends BaseAction {

  private final String imageId;
  private final ImageRegistry imageRegistry;
  private UpdateAction updateAction;
  private PerformAction performAction;

  private ActionData actionData;

  JsAction(String label, String description, String imageId, ImageRegistry imageRegistry) {
    super(label, description);
    this.imageId = imageId;
    this.imageRegistry = imageRegistry;
  }

  @Override
  public void update(ActionEvent e) {
    // image can appears later so check it every time
    if (imageId != null) {
      e.getPresentation().setImageElement(imageRegistry.getImage(imageId));
    }
    if (updateAction == null) {
      return;
    }
    Presentation presentation = e.getPresentation();
    ActionData actionData = getData(presentation);

    updateAction.updateAction(actionData);

    presentation.setImageElement(actionData.getImageElement());
    presentation.setText(actionData.getText());
    presentation.setDescription(actionData.getDescription());
    presentation.setEnabled(actionData.isEnabled());
    presentation.setVisible(actionData.isVisible());
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (performAction == null) {
      return;
    }
    performAction.performAction();
  }

  private ActionData getData(Presentation presentation) {
    if (actionData == null) {
      actionData = new ActionData();
    }

    actionData.setText(presentation.getText());
    actionData.setDescription(presentation.getDescription());
    actionData.setImageElement(presentation.getImageElement());
    actionData.setVisible(presentation.isVisible());
    actionData.setEnabled(presentation.isEnabled());
    return actionData;
  }

  public void setUpdateAction(UpdateAction updateAction) {
    this.updateAction = updateAction;
  }

  public void setPerformAction(PerformAction performAction) {
    this.performAction = performAction;
  }
}
