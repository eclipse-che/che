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
package org.eclipse.che.ide.ext.java.client.navigation.openimplementation;

import elemental.dom.Element;
import elemental.dom.Node;
import elemental.events.CustomEvent;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.html.SpanElement;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.ide.ui.popup.PopupResources;
import org.eclipse.che.ide.ui.popup.PopupWidget;
import org.eclipse.che.ide.util.dom.Elements;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.vectomatic.dom.svg.ui.SVGImage;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Widget for displaying implementations.
 *
 * @author Valeriy Svydenko
 */
public class ImplementationWidget extends PopupWidget<SymbolInformation> {

  /** Custom event type. */
  private static final String CUSTOM_EVT_TYPE_VALIDATE = "itemvalidate";

  private final JavaResources javaResources;
  private final JavaLocalizationConstant locale;
  private final OpenImplementationPresenter openImplementationPresenter;

  public ImplementationWidget(
      PopupResources popupResources,
      JavaResources javaResources,
      JavaLocalizationConstant locale,
      OpenImplementationPresenter openImplementationPresenter,
      String title) {
    super(popupResources, title);
    this.javaResources = javaResources;
    this.locale = locale;
    this.openImplementationPresenter = openImplementationPresenter;

    this.asElement().setId("implementationsId");
  }

  @Override
  public String getEmptyMessage() {
    return locale.noImplementations();
  }

  @Override
  public Element createItem(final SymbolInformation itemModel) {
    final Element element = Elements.createLiElement(popupResources.popupStyle().item());
    final Element iconElement = Elements.createDivElement(popupResources.popupStyle().icon());

    SVGImage svgImage = getSvgImage(itemModel.getKind());
    iconElement.appendChild((Node) svgImage.getElement());

    element.appendChild(iconElement);
    element.appendChild(createTitleOfElement(itemModel));

    final EventListener validateListener =
        evt -> {
          openImplementationPresenter.openOneImplementation(itemModel);
          hide();
        };

    element.addEventListener(Event.DBLCLICK, validateListener, false);
    element.addEventListener(CUSTOM_EVT_TYPE_VALIDATE, validateListener, false);

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

  private SpanElement createTitleOfElement(SymbolInformation symbolInformation) {
    String path = symbolInformation.getLocation().getUri();
    SpanElement texElement = Elements.createSpanElement();
    SpanElement highlightElement =
        Elements.createSpanElement(javaResources.css().presentableTextContainer());
    highlightElement.setInnerText(" - (" + path + ')');
    texElement.setInnerText(symbolInformation.getName());
    texElement.appendChild(highlightElement);

    return texElement;
  }

  private native CustomEvent createValidateEvent(String eventType) /*-{
        return new CustomEvent(eventType);
    }-*/;

  private SVGImage getSvgImage(SymbolKind symbolKind) {
    SVGResource icon;
    switch (symbolKind) {
      case Interface:
        icon = javaResources.interfaceItem();
        break;
      case Enum:
        icon = javaResources.enumItem();
        break;
      default:
        icon = javaResources.javaFile();
    }
    return new SVGImage(icon);
  }
}
