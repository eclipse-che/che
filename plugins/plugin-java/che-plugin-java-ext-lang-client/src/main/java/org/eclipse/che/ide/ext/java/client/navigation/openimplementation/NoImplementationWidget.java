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
import org.eclipse.che.ide.ext.java.client.util.Flags;
import org.eclipse.che.ide.ext.java.shared.dto.model.Type;
import org.eclipse.che.ide.ui.popup.PopupResources;
import org.eclipse.che.ide.ui.popup.PopupWidget;
import org.eclipse.che.ide.util.dom.Elements;
import org.vectomatic.dom.svg.ui.SVGImage;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Widget for displaying implementations.
 *
 * @author Valeriy Svydenko
 */
public class NoImplementationWidget extends PopupWidget<Type> {
  /** Custom event type. */
  private static final String CUSTOM_EVT_TYPE_VALIDATE = "itemvalidate";

  private final JavaResources javaResources;
  private final JavaLocalizationConstant locale;
  private final OpenImplementationPresenter openImplementationPresenter;

  public NoImplementationWidget(
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
  public Element createItem(final Type itemModel) {
    final Element element = Elements.createLiElement(popupResources.popupStyle().item());
    final Element iconElement = Elements.createDivElement(popupResources.popupStyle().icon());

    int flag = itemModel.getFlags();
    if (flag == -1) {
      element.setInnerText(getEmptyMessage());
      return element;
    }

    SVGImage svgImage = getSvgImage(flag);
    iconElement.appendChild((Node) svgImage.getElement());

    element.appendChild(iconElement);
    element.appendChild(createTitleOfElement(itemModel));

    final EventListener validateListener =
        new EventListener() {
          @Override
          public void handleEvent(final Event evt) {
            openImplementationPresenter.actionPerformed(itemModel);
            hide();
          }
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

  private SpanElement createTitleOfElement(Type type) {
    String path = type.getRootPath();
    SpanElement texElement = Elements.createSpanElement();
    SpanElement highlightElement =
        Elements.createSpanElement(javaResources.css().presentableTextContainer());
    highlightElement.setInnerText(" - (" + path + ')');
    texElement.setInnerText(type.getElementName());
    texElement.appendChild(highlightElement);

    return texElement;
  }

  private native CustomEvent createValidateEvent(String eventType) /*-{
        return new CustomEvent(eventType);
    }-*/;

  private SVGImage getSvgImage(int flag) {
    SVGResource icon;
    if (Flags.isInterface(flag)) {
      icon = javaResources.interfaceItem();
    } else if (Flags.isEnum(flag)) {
      icon = javaResources.enumItem();
    } else if (Flags.isAnnotation(flag)) {
      icon = javaResources.annotationItem();
    } else {
      icon = javaResources.javaFile();
    }
    return new SVGImage(icon);
  }
}
