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
package org.eclipse.che.ide.editor.orion.client;

public interface OrionEventConstants {

  /* not complete atm */
  String MODEL_CHANGED_EVENT = "ModelChanged";
  String MODEL_CHANING_EVENT = "ModelChanging";
  String SELECTION_EVENT = "Selection";
  String FOCUS_EVENT = "Focus";
  String BLUR_EVENT = "Blur";
  String KEYDOWN_EVENT = "KeyDown";
  String KEYPRESS_EVENT = "KeyPress";
  String KEYUP_EVENT = "KeyUp";
  String DESTROY_EVENT = "Destroy";
  String CONTEXT_MENU_EVENT = "ContextMenu";
  String SCROLL_EVENT = "Scroll";
  String RULER_CLICK_EVENT = "RulerClick";
}
