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

import static java.lang.Boolean.parseBoolean;
import static org.eclipse.che.commons.xml.NewElement.createElement;
import static org.eclipse.che.commons.xml.XMLTreeLocation.after;
import static org.eclipse.che.commons.xml.XMLTreeLocation.inTheBegin;
import static org.eclipse.che.commons.xml.XMLTreeLocation.inTheEnd;

import org.eclipse.che.commons.xml.Element;
import org.eclipse.che.commons.xml.NewElement;

/**
 * Describes information about snapshots and releases of <i>/project/repositories</i> and
 * <i>/project/pluginRepositories</i>
 *
 * <p>Supports next data:
 *
 * <ul>
 *   <li>enabled
 *   <li>checksumPolicy
 *   <li>updatePolicy
 * </ul>
 *
 * @author Eugene Voevodin
 */
public class RepositoryPolicy {

  Element element;

  private Boolean isEnabled;
  private String checksumPolicy;
  private String updatePolicy;

  public RepositoryPolicy(boolean isEnabled, String checksumPolicy, String updatePolicy) {
    this.isEnabled = isEnabled;
    this.checksumPolicy = checksumPolicy;
    this.updatePolicy = updatePolicy;
  }

  public RepositoryPolicy() {}

  RepositoryPolicy(Element element) {
    this.element = element;
    isEnabled = parseBoolean(element.getChildText("enabled"));
    updatePolicy = element.getChildText("updatePolicy");
    checksumPolicy = element.getChildText("checksumPolicy");
  }

  /** Returns {@code true} when repository is enabled, otherwise returns {@code false}. */
  public Boolean isEnabled() {
    return isEnabled;
  }

  /**
   * Sets repository is enabled or not.
   *
   * @param isEnabled {@code true} if repository should be enabled, {@code false} if not, {@code
   *     null} if it is necessary to remove <i>enabled</i> from xml
   */
  public RepositoryPolicy setEnabled(Boolean isEnabled) {
    this.isEnabled = isEnabled;
    if (element != null) {
      if (isEnabled == null) {
        element.removeChild("enabled");
      } else if (element.hasSingleChild("enabled")) {
        element.getSingleChild("enabled").setText(Boolean.toString(isEnabled));
      } else {
        element.insertChild(createElement("enabled", Boolean.toString(isEnabled)), inTheBegin());
      }
    }
    return this;
  }

  /**
   * Returns policy which is responsible for missing or incorrect artifact checksum.
   *
   * <p>Available values:
   *
   * <ul>
   *   <li>ignore
   *   <li>fail
   *   <li>warn
   * </ul>
   */
  public String getChecksumPolicy() {
    return checksumPolicy;
  }

  /**
   * Sets checksum policy.
   *
   * @param checksumPolicy new checksum policy, if it is {@code null} then <i>checksumPolicy</i>
   *     element will be removed from xml if exists
   */
  public RepositoryPolicy setChecksumPolicy(String checksumPolicy) {
    this.checksumPolicy = checksumPolicy;
    if (element != null) {
      if (checksumPolicy == null) {
        element.removeChild("checksumPolicy");
      } else if (element.hasSingleChild("checksumPolicy")) {
        element.getSingleChild("checksumPolicy").setText(checksumPolicy);
      } else {
        element.insertChild(createElement("checksumPolicy", checksumPolicy), inTheEnd());
      }
    }
    return this;
  }

  /**
   * Returns update policy which is responsible for update period(how often update should occur i.e.
   * daily)
   */
  public String getUpdatePolicy() {
    return updatePolicy;
  }

  /**
   * Sets update policy(i.e. daily, monthly).
   *
   * @param updatePolicy new update policy,if it is {@code null} then <i>updatePolicy</i> element
   *     will be removed from xml if exists
   */
  public RepositoryPolicy setUpdatePolicy(String updatePolicy) {
    this.updatePolicy = updatePolicy;
    if (element != null) {
      if (updatePolicy == null) {
        element.removeChild("updatePolicy");
      } else if (element.hasSingleChild("updatePolicy")) {
        element.getSingleChild("updatePolicy").setText(updatePolicy);
      } else {
        element.insertChild(
            createElement("updatePolicy", updatePolicy), after("enabled").or(inTheBegin()));
      }
    }
    return this;
  }

  NewElement asXMLElement(String parentName) {
    final NewElement xmlPolicy = createElement(parentName);
    if (isEnabled != null) {
      xmlPolicy.appendChild(createElement("enabled", Boolean.toString(isEnabled)));
    }
    if (checksumPolicy != null) {
      xmlPolicy.appendChild(createElement("checksumPolicy", checksumPolicy));
    }
    if (updatePolicy != null) {
      xmlPolicy.appendChild(createElement("updatePolicy", updatePolicy));
    }
    return xmlPolicy;
  }

  void remove() {
    if (element != null) {
      element.remove();
      element = null;
    }
  }
}
