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
import static org.eclipse.che.commons.xml.XMLTreeLocation.before;
import static org.eclipse.che.commons.xml.XMLTreeLocation.inTheBegin;
import static org.eclipse.che.commons.xml.XMLTreeLocation.inTheEnd;

import org.eclipse.che.commons.xml.Element;
import org.eclipse.che.commons.xml.NewElement;

/**
 * The {@literal <parent>} element contains information required to locate the parent project which
 * this project will inherit from.
 *
 * <p>Supports next data:
 *
 * <ul>
 *   <li>artifactId
 *   <li>groupId
 *   <li>version
 *   <li>relativePath
 * </ul>
 *
 * @author Eugene Voevodin
 */
public class Parent {

  private String groupId;
  private String artifactId;
  private String version;
  private String relativePath;

  Element parentElement;

  public Parent() {}

  public Parent(String groupId, String artifactId, String version) {
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.version = version;
  }

  Parent(Element element) {
    parentElement = element;
    groupId = element.getChildText("groupId");
    artifactId = element.getChildText("artifactId");
    version = element.getChildText("version");
  }

  /** Returns the artifact id of the parent project to inherit from. */
  public String getArtifactId() {
    return artifactId;
  }

  /** Returns the group id of the parent project to inherit from */
  public String getGroupId() {
    return groupId;
  }

  /** Returns the version of the parent project to inherit */
  public String getVersion() {
    return version;
  }

  public String getRelativePath() {
    return relativePath == null ? "../pom.xml" : relativePath;
  }

  /**
   * Sets the artifact id of the parent project to inherit from
   *
   * <p>If {@code artifactId} is {@code null} and this parent instance is associated with xml
   * element then {@code artifactId} will be removed from model as well as from xml.
   *
   * @param artifactId new parent artifact identifier
   * @return this parent instance
   */
  public Parent setArtifactId(String artifactId) {
    this.artifactId = artifactId;
    if (!isNew()) {
      if (artifactId == null) {
        parentElement.removeChild("artifactId");
      } else if (parentElement.hasSingleChild("artifactId")) {
        parentElement.getSingleChild("artifactId").setText(artifactId);
      } else {
        parentElement.insertChild(
            createElement("artifactId", artifactId), after("groupId").or(inTheBegin()));
      }
    }
    return this;
  }

  /**
   * Sets the group id of the parent project to inherit from
   *
   * <p>If {@code groupId} is {@code null} and this parent instance is associated with xml element
   * then {@code groupId} will be removed from model as well as from xml.
   *
   * @param groupId new parent group identifier
   * @return this parent instance
   */
  public Parent setGroupId(String groupId) {
    this.groupId = groupId;
    if (!isNew()) {
      if (groupId == null) {
        parentElement.removeChild("groupId");
      } else if (parentElement.hasSingleChild("groupId")) {
        parentElement.getSingleChild("groupId").setText(groupId);
      } else {
        parentElement.insertChild(createElement("groupId", groupId), inTheBegin());
      }
    }
    return this;
  }

  /**
   * Sets the version of the parent project to inherit
   *
   * <p>If {@code version} is {@code null} and this parent instance is associated with xml element
   * then {@code version} will be removed from model as well as from xml
   *
   * @param version new parent version
   * @return this parent instance
   */
  public Parent setVersion(String version) {
    this.version = version;
    if (!isNew()) {
      if (version == null) {
        parentElement.removeChild("version");
      } else if (parentElement.hasSingleChild("version")) {
        parentElement.getSingleChild("version").setText(version);
      } else {
        parentElement.insertChild(
            createElement("version", version), before("relativePath").or(inTheEnd()));
      }
    }
    return this;
  }

  /**
   * Sets parent relative path
   *
   * <p>If {@code relativePath} is {@code null} and this parent instance is associated with xml
   * element then {@code relativePath} will be removed from model as well as from xml
   *
   * @param relativePath new parent relative path
   * @return this parent instance
   */
  public Parent setRelativePath(String relativePath) {
    this.relativePath = relativePath;
    if (!isNew()) {
      if (relativePath == null) {
        parentElement.removeChild("relativePath");
      } else if (parentElement.hasSingleChild("relativePath")) {
        parentElement.getSingleChild("relativePath").setText(relativePath);
      } else {
        parentElement.appendChild(createElement("relativePath", relativePath));
      }
    }
    return this;
  }

  /** Returns the id as <i>groupId:artifactId:version</i> */
  public String getId() {
    return groupId + ':' + artifactId + ":pom:" + version;
  }

  @Override
  public String toString() {
    return getId();
  }

  NewElement asXMLElement() {
    final NewElement newParent = createElement("parent");
    if (groupId != null) {
      newParent.appendChild(createElement("groupId", groupId));
    }
    if (artifactId != null) {
      newParent.appendChild(createElement("artifactId", artifactId));
    }
    if (version != null) {
      newParent.appendChild(createElement("version", version));
    }
    if (relativePath != null) {
      newParent.appendChild(createElement("relativePath", relativePath));
    }
    return newParent;
  }

  private boolean isNew() {
    return parentElement == null;
  }
}
