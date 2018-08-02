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
import static org.eclipse.che.commons.xml.XMLTreeLocation.inTheBegin;

import org.eclipse.che.commons.xml.Element;
import org.eclipse.che.commons.xml.NewElement;

/**
 * Describes file for <i>/project/profiles/profile/activation.</i> A given filename may activate the
 * profile by the existence of a file, or if it is missing. NOTE: interpolation for this element is
 * limited to ${basedir}, System properties and request properties.
 *
 * <p>Supports next data:
 *
 * <ul>
 *   <li>missing
 *   <li>exist
 * </ul>
 */
public class ActivationFile {
  public Element element;

  private String missing;
  private String exist;

  public ActivationFile(Element element) {
    this.element = element;
    this.missing = element.getChildText("missing");
    this.exist = element.getChildText("exists");
  }

  /** Returns missed file. */
  public String getMissing() {
    return missing;
  }

  /** Sets value of missed file. */
  public ActivationFile setMissing(String missing) {
    this.missing = missing;
    if (element != null) {
      if (missing == null) {
        element.removeChild("missing");
      } else if (element.hasSingleChild("missing")) {
        element.getSingleChild("missing").setText(missing);
      } else {
        element.insertChild(createElement("missing", missing), inTheBegin());
      }
    }
    return this;
  }

  /** Returns existed file. */
  public String getExist() {
    return exist;
  }

  /** Sets value of existed file. */
  public ActivationFile setExist(String exist) {
    this.exist = exist;
    if (element != null) {
      if (exist == null) {
        element.removeChild("exists");
      } else if (element.hasSingleChild("exists")) {
        element.getSingleChild("exists").setText(exist);
      } else {
        element.insertChild(createElement("exists", exist), inTheBegin());
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

  public NewElement asXMLElement() {
    final NewElement profile = createElement("file");
    if (missing != null) {
      profile.appendChild(createElement("missing", missing));
    }
    if (exist != null) {
      profile.appendChild(createElement("exists", exist));
    }

    return profile;
  }
}
