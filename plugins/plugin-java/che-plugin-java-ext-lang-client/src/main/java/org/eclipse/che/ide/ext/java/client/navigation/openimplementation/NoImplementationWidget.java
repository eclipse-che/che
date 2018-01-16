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
package org.eclipse.che.ide.ext.java.client.navigation.openimplementation;

import elemental.dom.Element;
import elemental.events.CustomEvent;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ui.popup.PopupResources;
import org.eclipse.che.ide.ui.popup.PopupWidget;
import org.eclipse.che.ide.util.dom.Elements;
import org.eclipse.lsp4j.SymbolInformation;

/**
 * Widget for displaying popup with informing about no implementations found.
 *
 * @author Dmitrii Bocharov
 */
public class NoImplementationWidget extends PopupWidget<SymbolInformation> {

  /** Custom event type. */
  private static final String CUSTOM_EVT_TYPE_VALIDATE = "itemvalidate";

  private final JavaLocalizationConstant locale;

  public NoImplementationWidget(
      PopupResources popupResources, JavaLocalizationConstant locale, String title) {
    super(popupResources, title);
    this.locale = locale;
    this.asElement().setId("implementationsId");
  }

  @Override
  public String getEmptyMessage() {
    return locale.noImplementations();
  }

  @Override
  public Element createItem(final SymbolInformation itemModel) {
    Element element = Elements.createLiElement(popupResources.popupStyle().item());
    element.setInnerText(getEmptyMessage());
    return element;
  }

  @Override
  public void validateItem(final Element validatedItem) {
    validatedItem.dispatchEvent(createValidateEvent(CUSTOM_EVT_TYPE_VALIDATE));
    super.validateItem(validatedItem);
  }

  @Override
  public boolean needsFocus() {
    return true;
  }

  private native CustomEvent createValidateEvent(String eventType) /*-{
        return new CustomEvent(eventType);
    }-*/;
}
