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
 * Describes os for <i>/project/profiles/profile/activation.</i> The os element can define some
 * operating system specific properties shown above.
 *
 * <p>Supports next data:
 *
 * <ul>
 *   <li>name
 *   <li>family
 *   <li>arch
 *   <li>version
 * </ul>
 */
public class ActivationOS {
  public Element element;

  private String name;
  private String family;
  private String arch;
  private String version;

  public ActivationOS(Element element) {
    this.element = element;
    this.name = element.getChildText("name");
    this.family = element.getChildText("family");
    this.arch = element.getChildText("arch");
    this.version = element.getChildText("version");
  }

  /** Gets value of "name" element. */
  public String getName() {
    return name;
  }

  /** Sets value for "name" element. */
  public ActivationOS setName(String name) {
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

  /** Gets value of "family" element. */
  public String getFamily() {
    return family;
  }

  /** Sets value for "family" element. */
  public ActivationOS setFamily(String family) {
    this.family = family;

    if (element != null) {
      if (family == null) {
        element.removeChild("family");
      } else if (element.hasSingleChild("family")) {
        element.getSingleChild("family").setText(family);
      } else {
        element.insertChild(createElement("family", family), after("name").or(inTheEnd()));
      }
    }

    return this;
  }

  /** Gets value of "arch" element. */
  public String getArch() {
    return arch;
  }

  /** Sets value for "arch" element. */
  public ActivationOS setArch(String arch) {
    this.arch = arch;

    if (element != null) {
      if (arch == null) {
        element.removeChild("arch");
      } else if (element.hasSingleChild("arch")) {
        element.getSingleChild("arch").setText(arch);
      } else {
        element.insertChild(createElement("arch", arch), after("family").or(inTheEnd()));
      }
    }

    return this;
  }

  /** Gets value of "version" element. */
  public String getVersion() {
    return version;
  }

  /** Sets value for "version" element. */
  public ActivationOS setVersion(String version) {
    this.version = version;

    if (element != null) {
      if (version == null) {
        element.removeChild("version");
      } else if (element.hasSingleChild("version")) {
        element.getSingleChild("version").setText(version);
      } else {
        element.insertChild(createElement("version", version), after("arch").or(inTheEnd()));
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

  /** Returns full <os> element */
  public NewElement asXMLElement() {
    final NewElement os = createElement("os");
    if (name != null) {
      os.appendChild(createElement("name", name));
    }
    if (family != null) {
      os.appendChild(createElement("family", family));
    }
    if (arch != null) {
      os.appendChild(createElement("arch", arch));
    }
    if (version != null) {
      os.appendChild(createElement("version", version));
    }
    return os;
  }
}
