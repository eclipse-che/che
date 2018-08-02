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

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.che.commons.xml.NewElement.createElement;

import org.eclipse.che.commons.xml.Element;
import org.eclipse.che.commons.xml.NewElement;

/**
 * Describes activation for <i>/project/profiles/profile/</i>. Activations are the key of a profile.
 * The power of a profile comes from its ability to modify the basic POM only under certain
 * circumstances. Those circumstances are specified via an activation element.
 *
 * <p>Supports next data:
 *
 * <ul>
 *   <li>activationByDefault
 *   <li>os
 *   <li>file
 *   <li>property
 *   <li>jdk
 * </ul>
 */
public class Activation {
  public Element element;

  private String activeByDefault;
  private ActivationOS os;
  private ActivationFile file;
  private ActivationProperty property;
  /**
   * This will activate if the test is run under a jdk version number that matches the prefix given.
   */
  private String jdk;

  public Activation(Element element) {
    this.element = element;
    if (element.hasSingleChild("activeByDefault")) {
      activeByDefault = element.getChildText("activeByDefault");
    }
    if (element.hasSingleChild("os")) {
      os = new ActivationOS(element.getSingleChild("os"));
    }

    if (element.hasSingleChild("file")) {
      file = new ActivationFile(element.getSingleChild("file"));
    }

    if (element.hasSingleChild("property")) {
      property = new ActivationProperty(element.getSingleChild("property"));
    }

    if (element.hasSingleChild("jdk")) {
      jdk = element.getChildText("jdk");
    }
  }

  public String isActiveByDefault() {
    return activeByDefault;
  }

  public Activation setActiveByDefault(String activeByDefault) {
    this.activeByDefault = activeByDefault;
    if (!isNew()) {
      if (isNullOrEmpty(activeByDefault)) {
        element.removeChild("activeByDefault");
      } else if (element.hasSingleChild("activeByDefault")) {
        element.getSingleChild("activeByDefault").setText(activeByDefault);
      } else {
        element.appendChild(createElement("activeByDefault", activeByDefault));
      }
    }
    return this;
  }

  public ActivationOS getOs() {
    return os;
  }

  public Activation setOs(ActivationOS os) {
    this.os = os;
    if (!isNew()) {
      if (os == null) {
        element.removeChild("os");
      } else if (element.hasSingleChild("os")) {
        element.removeChild("os");
        element.appendChild(os.asXMLElement());
      } else {
        element.appendChild(os.asXMLElement());
      }
    }
    return this;
  }

  public ActivationFile getFile() {
    return file;
  }

  public Activation setFile(ActivationFile file) {
    this.file = file;
    if (!isNew()) {
      if (file == null) {
        element.removeChild("file");
      } else if (element.hasSingleChild("file")) {
        element.removeChild("file");
        element.appendChild(file.asXMLElement());
      } else {
        element.appendChild(file.asXMLElement());
      }
    }
    return this;
  }

  public ActivationProperty getProperty() {
    return property;
  }

  public Activation setProperty(ActivationProperty property) {
    this.property = property;
    if (!isNew()) {
      if (property == null) {
        element.removeChild("property");
      } else if (element.hasSingleChild("property")) {
        element.removeChild("property");
        element.appendChild(property.asXMLElement());
      } else {
        element.appendChild(property.asXMLElement());
      }
    }
    return this;
  }

  public String getJdk() {
    return jdk;
  }

  public Activation setJdk(String jdk) {
    this.jdk = jdk;
    if (!isNew()) {
      if (isNullOrEmpty(jdk)) {
        element.removeChild("jdk");
      } else if (element.hasSingleChild("jdk")) {
        element.getSingleChild("jdk").setText(jdk);
      } else {
        element.appendChild(createElement("jdk", jdk));
      }
    }
    return this;
  }

  /** Removes <activation> element. */
  public void remove() {
    if (element != null) {
      element.remove();
      element = null;
    }
  }

  private boolean isNew() {
    return element == null;
  }

  /** Returns full <activation> element */
  public NewElement asXMLElement() {
    final NewElement activation = createElement("activation");

    if (!isNullOrEmpty(activeByDefault)) {
      activation.appendChild(createElement("activeByDefault", activeByDefault));
    }
    if (os != null) {
      activation.appendChild(os.asXMLElement());
    }
    if (file != null) {
      activation.appendChild(file.asXMLElement());
    }
    if (property != null) {
      activation.appendChild(property.asXMLElement());
    }
    if (!isNullOrEmpty(jdk)) {
      activation.appendChild(createElement("jdk", jdk));
    }

    return activation;
  }
}
