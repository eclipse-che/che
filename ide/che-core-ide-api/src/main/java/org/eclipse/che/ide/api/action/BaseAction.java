/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.api.action;

import org.vectomatic.dom.svg.ui.SVGImage;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Represents an entity that has a state, a presentation and can be performed.
 *
 * <p>For an action to be useful, you need to implement {@link Action#actionPerformed} and
 * optionally to override {@link Action#update}. By overriding the {@link Action#update} method you
 * can dynamically change action's presentation.
 *
 * <p>The same action can have various presentations.
 *
 * @author Evgen Vidolob
 * @author Dmitry Shnurenko
 * @author Vitaliy Guliy
 */
public abstract class BaseAction implements Action {

  private final Presentation presentation = new Presentation();

  /** Creates a new action with its text, description and icon set to <code>null</code>. */
  public BaseAction() {
    this(null, null, null, null);
  }

  /**
   * Creates a new action with the specified text. Description and icon are set to <code>null</code>
   * .
   *
   * @param text Serves as a tooltip when the presentation is a button and the name of the menu item
   *     when the presentation is a menu item.
   */
  public BaseAction(String text) {
    this(text, null, null, null);
  }

  /**
   * Constructs a new action with the specified text, description.
   *
   * @param text Serves as a tooltip when the presentation is a button and the name of the menu item
   *     when the presentation is a menu item
   * @param description Describes current action, this description will appear on the status bar
   *     when presentation has focus
   */
  public BaseAction(String text, String description) {
    this(text, description, null, null);
  }

  /**
   * Constructs a new action with the specified text, description.
   *
   * @param text Serves as a tooltip when the presentation is a button and the name of the menu item
   *     when the presentation is a menu item
   * @param description Describes current action, this description will appear on the status bar
   *     when presentation has focus
   * @param svgResource Action's SVG icon
   */
  public BaseAction(String text, String description, SVGResource svgResource) {
    this(text, description, svgResource, null);
  }

  /**
   * Constructs a new action with the specified text, description.
   *
   * @param text Serves as a tooltip when the presentation is a button and the name of the menu item
   *     when the presentation is a menu item
   * @param description Describes current action, this description will appear on the status bar
   *     when presentation has focus
   * @param htmlResource HTML representation of icon
   */
  public BaseAction(String text, String description, String htmlResource) {
    this(text, description, null, htmlResource);
  }

  /**
   * Constructs a new action with the specified text, description and icon.
   *
   * @param text Serves as a tooltip when the presentation is a button and the name of the menu item
   *     when the presentation is a menu item
   * @param description Describes current action, this description will appear on the status bar
   *     when presentation has focus
   * @param svgResource Action's SVG icon
   * @param htmlResource HTML representation of icon
   */
  public BaseAction(String text, String description, SVGResource svgResource, String htmlResource) {
    presentation.setText(text);
    presentation.setDescription(description);
    if (svgResource != null) {
      presentation.setImageElement(new SVGImage(svgResource).getElement());
    }
    presentation.setHTMLResource(htmlResource);
  }

  @Override
  public void update(ActionEvent e) {}

  @Override
  public final Presentation getTemplatePresentation() {
    return presentation;
  }

  @Override
  public String toString() {
    return getTemplatePresentation().toString();
  }
}
