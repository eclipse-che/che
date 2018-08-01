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
package org.eclipse.che.ide.maven.tools;

import static org.eclipse.che.commons.xml.NewElement.createElement;
import static org.eclipse.che.commons.xml.XMLTreeLocation.after;
import static org.eclipse.che.commons.xml.XMLTreeLocation.inTheBegin;
import static org.eclipse.che.commons.xml.XMLTreeLocation.inTheEnd;

import org.eclipse.che.commons.xml.Element;
import org.eclipse.che.commons.xml.NewElement;

/**
 * Describes property for <i>/project/profiles/profile/activation.</i> The profile will activate if
 * Maven detects a property (a value which can be dereferenced within the POM by ${name}) of the
 * corresponding name=value pair.
 *
 * <p>Supports next data:
 *
 * <ul>
 *   <li>name
 *   <li>value
 * </ul>
 */
public class ActivationProperty {
  public Element element;

  private String name;
  private String value;

  public ActivationProperty(Element element) {
    this.element = element;
    this.name = element.getChildText("name");
    this.value = element.getChildText("value");
  }

  /** Returns value of "name" filed. */
  public String getName() {
    return name;
  }

  /** Sets value of "name" filed. */
  public ActivationProperty setName(String name) {
    this.name = name;

    if (element != null) {
      if (name == null) {
        element.removeChild("name");
      } else if (element.hasSingleChild("name")) {
        element.getSingleChild("name").setText(name);
      } else {
        element.insertChild(createElement("name", name), inTheBegin());
      }
    }

    return this;
  }

  /** Returns value of "value" filed. */
  public String getValue() {
    return value;
  }

  /** Sets value of "value" filed. */
  public ActivationProperty setValue(String value) {
    this.value = value;

    if (element != null) {
      if (value == null) {
        element.removeChild("value");
      } else if (element.hasSingleChild("value")) {
        element.getSingleChild("value").setText(value);
      } else {
        element.insertChild(createElement("value", value), after("name").or(inTheEnd()));
      }
    }

    return this;
  }

  /** Removes element. */
  public void remove() {
    if (element != null) {
      element.remove();
      element = null;
    }
  }

  /** Returns full <property> element */
  public NewElement asXMLElement() {
    final NewElement property = createElement("property");
    if (name != null) {
      property.appendChild(createElement("name", name));
    }
    if (value != null) {
      property.appendChild(createElement("value", value));
    }

    return property;
  }
}
