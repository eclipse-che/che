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
package org.eclipse.che.commons.xml;

/** @author Eugene Voevodin */
public final class Attribute {

  private final Element container;
  private final QName name;
  private String value;

  Attribute(Element container, String name, String value) {
    this.name = new QName(name);
    this.container = container;
    this.value = value;
  }

  public String getName() {
    return name.getName();
  }

  public String getPrefix() {
    return name.getPrefix();
  }

  public String getValue() {
    return value;
  }

  public Element getElement() {
    return container;
  }

  public boolean hasPrefix() {
    return name.hasPrefix();
  }

  public void remove() {
    container.removeAttribute(name.getName());
  }

  public Attribute setValue(String value) {
    this.value = value;
    container.setAttributeValue(this);
    return this;
  }

  public String asString() {
    final StringBuilder sb = new StringBuilder();
    if (hasPrefix()) {
      sb.append(getPrefix()).append(':');
    }
    return sb.append(name).append('=').append('"').append(value).append('"').toString();
  }

  @Override
  public String toString() {
    return asString();
  }
}
