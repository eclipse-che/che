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
package org.eclipse.che.ide.api.editor.annotation;

import static elemental.css.CSSStyleDeclaration.Position.ABSOLUTE;
import static elemental.css.CSSStyleDeclaration.Position.STATIC;

import elemental.css.CSSStyleDeclaration;
import elemental.dom.Element;
import elemental.dom.Node;
import elemental.html.DivElement;
import elemental.html.HTMLCollection;
import elemental.util.Mappable;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.ide.api.editor.text.annotation.Annotation;
import org.eclipse.che.ide.util.dom.Elements;

/**
 * A composite of one or more annotation displays.<br>
 * All annotation are positioned at the same place and the one that is visible is determined by
 * their relative "layer" ( {@link Annotation#getLayer()}).<br>
 * All annotation texts ({@link Annotation#getText()}) are aggregated and shown in the annotation
 * tooltip.
 */
class AnnotationGroupImpl implements AnnotationGroup {

  private static final String ELEMENT_ROLE_VALUE_ANNOTATION = "annotation";
  private static final String ELEMENT_ROLE_DATA_PROPERTY = "eltRole";
  /*
   * This implementation - relies on z-index to display the higher priority annotation - uses data-* attribute (dataset) to store the
   * tooltips
   */
  private static final String MESSAGE_DATASET_NAME = "annotationMessage";
  private static final String TYPE_DATASET_NAME = "annotationType";
  private static final String LAYER_DATASET_NAME = "annotationLayer";
  private static final String OFFSET_DATASET_NAME = "annotationOffset";

  private elemental.dom.Element mainElement;

  public static final AnnotationGroupImpl create() {
    final AnnotationGroupImpl result = new AnnotationGroupImpl();
    final DivElement element = Elements.createDivElement();
    element.getStyle().setPosition(STATIC);
    result.mainElement = element;
    element.getDataset().setAt(ELEMENT_ROLE_DATA_PROPERTY, ELEMENT_ROLE_VALUE_ANNOTATION);
    return result;
  }

  public static final AnnotationGroupImpl create(final Element existingElement) {
    final AnnotationGroupImpl result = new AnnotationGroupImpl();
    result.mainElement = existingElement;
    return result;
  }

  public elemental.dom.Element asElemental() {
    return this.mainElement;
  }

  @Override
  public final void addAnnotation(final Annotation annotation, int offset) {
    asElemental().appendChild(buildIncludedElement(annotation, offset));
    updateIconVisibility();
  }

  @Override
  public final void removeAnnotation(final Annotation annotation, int offset) {
    final HTMLCollection children = asElemental().getChildren();
    for (int i = 0; i < children.length(); i++) {
      final Node child = (Node) children.at(i);
      if (child instanceof elemental.dom.Element) {
        final elemental.dom.Element element = (elemental.dom.Element) child;
        final Mappable dataset = element.getDataset();
        if (compareStrings(getMessage(dataset), annotation.getText())
            && getOffset(dataset) == offset
            && getLayer(dataset) == annotation.getLayer()
            && compareStrings(getType(dataset), annotation.getType())) {
          // we may not strictly be on the same annotation instance, but it is not discernible
          asElemental().removeChild(element);
          updateIconVisibility();
          break;
        }
      }
    }
  }

  private elemental.dom.Element buildIncludedElement(Annotation annotation, int offset) {
    final elemental.dom.Element element = annotation.getImageElement();
    final CSSStyleDeclaration style = element.getStyle();
    int layer = annotation.getLayer();
    style.setZIndex(layer);
    style.setPosition(ABSOLUTE);
    style.setTop("0");
    style.setLeft("0");
    style.setRight("0");
    style.setBottom("0");

    element.getDataset().setAt(MESSAGE_DATASET_NAME, annotation.getText());
    element.getDataset().setAt(TYPE_DATASET_NAME, annotation.getType());
    element.getDataset().setAt(LAYER_DATASET_NAME, Integer.toString(layer));
    element.getDataset().setAt(OFFSET_DATASET_NAME, Integer.toString(offset));

    return element;
  }

  private void updateIconVisibility() {
    int maxLayer = 0;
    final HTMLCollection children = asElemental().getChildren();
    for (int i = 0; i < children.length(); i++) {
      final Node child = (Node) children.at(i);
      if (child instanceof elemental.dom.Element) {
        final elemental.dom.Element element = (elemental.dom.Element) child;
        final Mappable dataset = element.getDataset();
        final int layer = getLayer(dataset);
        if (maxLayer < layer) {
          maxLayer = layer;
        }
      }
    }

    for (int i = 0; i < children.length(); i++) {
      final Node child = (Node) children.at(i);
      if (child instanceof elemental.dom.Element) {
        final elemental.dom.Element element = (elemental.dom.Element) child;
        final Mappable dataset = element.getDataset();
        final int layer = getLayer(dataset);
        if (layer >= maxLayer) {
          element.getStyle().removeProperty("display");
        } else {
          element.getStyle().setDisplay("none");
        }
      }
    }
  }

  @Override
  public Element getElement() {
    return asElemental();
  }

  @Override
  public List<String> getMessages() {
    final List<String> result = new ArrayList<>();

    final HTMLCollection children = asElemental().getChildren();
    for (int i = 0; i < children.length(); i++) {
      final Node child = (Node) children.at(i);
      if (child instanceof elemental.dom.Element) {
        final elemental.dom.Element element = (elemental.dom.Element) child;
        final Mappable dataset = element.getDataset();
        final String message = getMessage(dataset);
        if (message != null) {
          result.add(message);
        }
      }
    }
    return result;
  }

  @Override
  public int getAnnotationCount() {
    final HTMLCollection children = asElemental().getChildren();
    return children.getLength();
  }

  /**
   * Null-safe string comparison for equality.
   *
   * @param s1 first string to compare
   * @param s2 second string to compare
   * @return true iff both strings are equal
   */
  private static boolean compareStrings(final String s1, final String s2) {
    if (s1 == s2) {
      return true;
    }
    if (s1 == null) {
      return false;
    }
    return s1.equals(s2);
  }

  /**
   * Read the offset value stored in the dataset.
   *
   * @param dataset the dataset
   * @return the offset value or -1 if no valid value is found
   */
  private static int getOffset(final Mappable dataset) {
    final String asString = (String) dataset.at(OFFSET_DATASET_NAME);
    if (asString == null) {
      return -1;
    }
    try {
      return Integer.parseInt(asString);
    } catch (final NumberFormatException e) {
      return -1;
    }
  }

  /**
   * Read the layer value stored in the dataset.
   *
   * @param dataset the dataset
   * @return the layer value or -1 if no valid value is found
   */
  private static int getLayer(final Mappable dataset) {
    final String asString = (String) dataset.at(LAYER_DATASET_NAME);
    if (asString == null) {
      return -1;
    }
    try {
      return Integer.parseInt(asString);
    } catch (final NumberFormatException e) {
      return -1;
    }
  }

  /**
   * Read the type value stored in the dataset.
   *
   * @param dataset the dataset
   * @return the type value
   */
  private static String getType(final Mappable dataset) {
    return (String) dataset.at(TYPE_DATASET_NAME);
  }

  /**
   * Read the message value stored in the dataset.
   *
   * @param dataset the dataset
   * @return the message value
   */
  private static String getMessage(final Mappable dataset) {
    return (String) dataset.at(MESSAGE_DATASET_NAME);
  }

  /**
   * Checks if the element has the annotation marker set.
   *
   * @param element the element to check
   * @return true if the element is marked as 'annotation'
   */
  public static final boolean isAnnotation(final Element element) {
    if (element == null) {
      return false;
    }
    final Object role = element.getDataset().at(ELEMENT_ROLE_DATA_PROPERTY);
    return ELEMENT_ROLE_VALUE_ANNOTATION.equals(role);
  }
}
