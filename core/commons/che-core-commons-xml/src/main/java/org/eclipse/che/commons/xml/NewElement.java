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
package org.eclipse.che.commons.xml;

import static java.util.Arrays.asList;
import static org.eclipse.che.commons.xml.XMLTreeUtil.tabulate;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Used to add new element to {@link XMLTree}.
 *
 * <p>This class is really convenient for complex tree updates. To do it you need to make hierarchy
 * from NewElement instances which can contain NewAttribute instances or text as well. When {@link
 * NewElement} instance is ready tree uses {@link NewElement#asString()} to get view of new element.
 *
 * <p>Why don't we just create {@link Element} instead of using {@link NewElement} class?
 *
 * <ul>
 *   <li>First reason - is performance! Each time when you need to insert element tree bytes should
 *       be rewrote, but with {@link NewElement} tree bytes will be updated only time
 *   <li>Second reason is - data redundancy! Element should keep values such as children,
 *       attributes, name, text for each element instance which will be added to tree and after tree
 *       update this values must be dropped because element delegates for {@link org.w3c.dom.Node}
 *       and doesn't need it anymore.
 *   <li>Third reason is - tree integrity! Element instance created with tree should be inserted
 *       into same tree, so each time when update is going we need to make a lot of checks to save
 *       tree elements integrity
 * </ul>
 *
 * @author Eugene Voevodin
 */
public final class NewElement extends QName {

  public static NewElement createElement(String name) {
    return new NewElement(name, null);
  }

  public static NewElement createElement(String name, String text) {
    return new NewElement(name, text);
  }

  public static NewElement createElement(String name, NewElement... children) {
    final NewElement newElement = createElement(name);
    newElement.children = new ArrayList<>(asList(children));
    return newElement;
  }

  private String text;
  private List<NewAttribute> attributes;
  private List<NewElement> children;

  private NewElement(String name, String text) {
    super(name);
    this.text = text;
  }

  public NewElement setText(String text) {
    this.text = text;
    return this;
  }

  public NewElement setAttributes(List<NewAttribute> attributes) {
    this.attributes = attributes;
    return this;
  }

  public NewElement setChildren(List<NewElement> children) {
    this.children = children;
    return this;
  }

  public NewElement appendChild(NewElement child) {
    getChildren().add(child);
    return this;
  }

  public NewElement setAttribute(String name, String value) {
    getAttributes().add(new NewAttribute(name, value));
    return this;
  }

  public String getText() {
    return text == null ? "" : text;
  }

  public List<NewAttribute> getAttributes() {
    if (attributes == null) {
      attributes = new LinkedList<>();
    }
    return attributes;
  }

  public List<NewElement> getChildren() {
    if (children == null) {
      children = new LinkedList<>();
    }
    return children;
  }

  public boolean hasChildren() {
    return children != null && !children.isEmpty();
  }

  public boolean isVoid() {
    return text == null && !hasChildren();
  }

  public String asString() {
    final StringBuilder builder = new StringBuilder();
    builder.append('<').append(getName());
    if (attributes != null) {
      for (NewAttribute attribute : attributes) {
        builder.append(' ').append(attribute.asString());
      }
    }
    // if it is void element such as <tag attr="value"/>
    if (isVoid()) {
      return builder.append('/').append('>').toString();
    }
    builder.append('>').append(getText());
    if (hasChildren()) {
      builder.append('\n');
      for (NewElement child : children) {
        builder.append(tabulate(child.asString(), 1)).append('\n');
      }
    }
    builder.append('<').append('/').append(getName()).append('>');
    return builder.toString();
  }

  @Override
  public String toString() {
    return asString();
  }
}
