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
 * The <i>../dependency/exclusions/exclusion</i> element contains information required to exclude an
 * artifact from the project
 *
 * <p>Supported next data:
 *
 * <ul>
 *   <li>artifactId
 *   <li>groupId
 * </ul>
 *
 * @author Eugene Voevodin
 */
public class Exclusion {

  private String artifactId;
  private String groupId;

  Element exclusionElement;

  public Exclusion(String artifactId, String groupId) {
    this.artifactId = artifactId;
    this.groupId = groupId;
  }

  Exclusion(Element element) {
    exclusionElement = element;
    artifactId = element.getChildText("artifactId");
    groupId = element.getChildText("groupId");
  }

  /** Get the artifact ID of the project to exclude. */
  public String getArtifactId() {
    return artifactId;
  }

  /** Get the group ID of the project to exclude. */
  public String getGroupId() {
    return groupId;
  }

  /** Set the artifact ID of the project to exclude. */
  public Exclusion setArtifactId(String artifactId) {
    this.artifactId = artifactId;
    if (!isNew()) {
      if (artifactId == null) {
        exclusionElement.removeChild("artifactId");
      } else if (exclusionElement.hasSingleChild("artifactId")) {
        exclusionElement.getSingleChild("artifactId").setText(artifactId);
      } else {
        exclusionElement.appendChild(createElement("artifactId", artifactId));
      }
    }
    return this;
  }

  /** Set the group ID of the project to exclude. */
  public Exclusion setGroupId(String groupId) {
    this.groupId = groupId;
    if (!isNew()) {
      if (groupId == null) {
        exclusionElement.removeChild("groupId");
      } else if (exclusionElement.hasSingleChild("groupId")) {
        exclusionElement.getSingleChild("groupId").setText(groupId);
      } else {
        exclusionElement.insertChild(createElement("groupId", groupId), inTheBegin());
      }
    }
    return this;
  }

  void remove() {
    if (!isNew()) {
      exclusionElement.remove();
      exclusionElement = null;
    }
  }

  NewElement asXMLElement() {
    final NewElement newExclusion = createElement("exclusion");
    newExclusion.appendChild(createElement("groupId", groupId));
    newExclusion.appendChild(createElement("artifactId", artifactId));
    return newExclusion;
  }

  private boolean isNew() {
    return exclusionElement == null;
  }
}
