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
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static org.eclipse.che.commons.xml.NewElement.createElement;
import static org.eclipse.che.commons.xml.XMLTreeLocation.after;
import static org.eclipse.che.commons.xml.XMLTreeLocation.afterAnyOf;
import static org.eclipse.che.commons.xml.XMLTreeLocation.before;
import static org.eclipse.che.commons.xml.XMLTreeLocation.inTheBegin;
import static org.eclipse.che.commons.xml.XMLTreeLocation.inTheEnd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.che.commons.xml.Element;
import org.eclipse.che.commons.xml.ElementMapper;
import org.eclipse.che.commons.xml.NewElement;

/**
 * The {@code <dependency>} element contains information about project's dependency.
 *
 * <p>Supported next data:
 *
 * <ul>
 *   <li>artifactId
 *   <li>groupId
 *   <li>version
 *   <li>scope
 *   <li>classifier
 *   <li>type
 *   <li>optional
 *   <li>exclusions
 * </ul>
 *
 * @author Eugene Voevodin
 */
public class Dependency {

  private static final ToExclusionMapper TO_EXCLUSION_MAPPER = new ToExclusionMapper();

  private String groupId;
  private String artifactId;
  private String version;
  private String type;
  private String classifier;
  private String scope;
  private String optional;
  private List<Exclusion> exclusions;

  Element dependencyElement;

  public Dependency(String groupId, String artifactId, String version) {
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.version = version;
  }

  public Dependency() {}

  Dependency(Element element) {
    dependencyElement = element;
    artifactId = element.getChildText("artifactId");
    groupId = element.getChildText("groupId");
    version = element.getChildText("version");
    classifier = element.getChildText("classifier");
    optional = element.getChildText("optional");
    scope = element.getChildText("scope");
    type = element.getChildText("type");
    if (element.hasSingleChild("exclusions")) {
      exclusions = element.getSingleChild("exclusions").getChildren(TO_EXCLUSION_MAPPER);
    }
  }

  /**
   * Returns the unique id for an artifact produced by the project group, e.g. {@code
   * maven-artifact}.
   */
  public String getArtifactId() {
    return artifactId;
  }

  /**
   * Returns the classifier of the dependency.
   *
   * <p>This allows distinguishing two artifacts that belong to the same POM but were built
   * differently, and is appended to the filename after the version. For example, {@code jdk14} and
   * {@code jdk15}.
   */
  public String getClassifier() {
    return classifier;
  }

  /**
   * Returns dependency exclusions if dependency has it or empty set if doesn't
   *
   * <p><b>Note: update methods should not be used on returned list</b>
   */
  public List<Exclusion> getExclusions() {
    if (exclusions == null) {
      return emptyList();
    }
    return new ArrayList<>(exclusions);
  }

  /** Returns the project group that produced the dependency, e.g. {@code org.apache.maven}. */
  public String getGroupId() {
    return groupId;
  }

  /**
   * Returns the dependency is optional or not. If it is optional then {@code true} will be returned
   * otherwise {@code false}
   */
  public String getOptional() {
    return optional;
  }

  /**
   * Returns the scope of the dependency:
   *
   * <ul>
   *   <li>compile
   *   <li>runtime
   *   <li>test
   *   <li>system
   *   <li>provided
   * </ul>
   *
   * Used to calculate the various classpath used for compilation, testing, and so on. It also
   * assists in determining which artifacts to include in a distribution of this project. For more
   * information, see <a
   * href="http://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html">the
   * dependency mechanism</a>.
   */
  public String getScope() {
    return scope == null ? "compile" : scope;
  }

  /**
   * Returns the type of dependency.
   *
   * <p>This defaults to <code>jar</code>. While it usually represents the extension on the filename
   * of the dependency, that is not always the case. A type can be mapped to a different extension
   * and a classifier. The type often corresponds to the packaging used, though this is also not
   * always the case. Some examples are {@code jar, war, ejb-client}. New types can be defined by
   * plugins that set {@code extensions} to {@code true}, so this is not a complete list.
   */
  public String getType() {
    return type == null ? "jar" : type;
  }

  /** Returns the version of the dependency */
  public String getVersion() {
    return version;
  }

  /**
   * Adds new exclusion to the list of dependency exclusions.
   *
   * <p>If dependency doesn't have exclusions then it will be added to xml.
   *
   * @param exclusion new exclusion which will be added
   * @return this dependency instance
   * @throws NullPointerException when {@code exclusion} is {@code null}
   */
  public Dependency addExclusion(Exclusion exclusion) {
    requireNonNull(exclusion, "Required not null exclusion");
    exclusions().add(exclusion);
    // add exclusion to xml tree
    if (!isNew()) {
      if (dependencyElement.hasSingleChild("exclusions")) {
        dependencyElement.getSingleChild("exclusions").appendChild(exclusion.asXMLElement());
      } else {
        dependencyElement.appendChild(createElement("exclusions", exclusion.asXMLElement()));
      }
      exclusion.exclusionElement = dependencyElement.getSingleChild("exclusions").getLastChild();
    }
    return this;
  }

  /**
   * Removes exclusion from the dependency exclusions.
   *
   * <p>If last exclusion has been removed removes exclusions element as well.
   *
   * @param exclusion exclusion which should be removed
   * @return this dependency instance
   * @throws NullPointerException when {@code exclusion} is {@code null}
   */
  public Dependency removeExclusion(Exclusion exclusion) {
    requireNonNull(exclusion, "Required not null exclusion");
    exclusions().remove(exclusion);
    // remove dependency from xml
    if (!isNew() && exclusions.isEmpty()) {
      dependencyElement.removeChild("exclusions");
      exclusion.exclusionElement = null;
    } else {
      exclusion.remove();
    }
    return this;
  }

  /**
   * Sets list of artifacts that should be excluded from this dependency's artifact list when it
   * comes to calculating transitive dependencies.
   *
   * <p>If {@code exclusions} is {@code null} or <i>empty</i> and this dependency instance is
   * associated with xml element then {@code exclusions} be removed from model as well as from xml.
   *
   * @param exclusions new dependency exclusions
   * @return this dependency instance
   */
  public Dependency setExclusions(Collection<? extends Exclusion> exclusions) {
    if (exclusions == null || exclusions.isEmpty()) {
      removeExclusions();
    } else if (isNew()) {
      this.exclusions = new ArrayList<>(exclusions);
    } else {
      setExclusions0(exclusions);
    }
    return this;
  }

  /**
   * Sets the unique id for an artifact produced by the project group, e.g. {@code maven-artifact}.
   *
   * <p>If {@code artifactId} is {@code null} and this dependency instance is associated with xml
   * element then {@code artifactId} will be removed from model as well as from xml.
   */
  public Dependency setArtifactId(String artifactId) {
    this.artifactId = artifactId;
    if (!isNew()) {
      if (artifactId == null) {
        dependencyElement.removeChild("artifactId");
      } else if (dependencyElement.hasSingleChild("artifactId")) {
        dependencyElement.getSingleChild("artifactId").setText(artifactId);
      } else {
        dependencyElement.insertChild(
            createElement("artifactId", artifactId), after("groupId").or(inTheBegin()));
      }
    }
    return this;
  }

  /**
   * Sets the classifier of the dependency.
   *
   * <p>If {@code classifier} is {@code null} and this dependency instance is associated with xml
   * element then {@code classifier} will be removed from model as well as from xml.
   *
   * @param classifier new dependency classifier
   * @return this dependency instance
   */
  public Dependency setClassifier(String classifier) {
    this.classifier = classifier;
    if (!isNew()) {
      if (classifier == null) {
        dependencyElement.removeChild("classifier");
      } else if (dependencyElement.hasSingleChild("classifier")) {
        dependencyElement.getSingleChild("classifier").setText(classifier);
      } else {
        dependencyElement.insertChild(
            createElement("classifier", classifier), before("exclusions").or(inTheEnd()));
      }
    }
    return this;
  }

  /**
   * Sets the project group that produced the dependency, e.g. <i>org.apache.maven</i>.
   *
   * <p>If {@code groupId} is {@code null} and this dependency instance is associated with xml
   * element then {@code groupId} will be removed from model as well as from xml.
   *
   * @param groupId new dependency groupId
   * @return this dependency instance
   */
  public Dependency setGroupId(String groupId) {
    this.groupId = groupId;
    if (!isNew()) {
      if (groupId == null) {
        dependencyElement.removeChild("groupId");
      } else if (dependencyElement.hasSingleChild("groupId")) {
        dependencyElement.getSingleChild("groupId").setText(groupId);
      } else {
        dependencyElement.insertChild(createElement("groupId", groupId), inTheBegin());
      }
    }
    return this;
  }

  /**
   * Sets indicates the dependency is optional for use of this library.
   *
   * <p>If {@code optional} is {@code null} and this dependency instance is associated with xml
   * element then {@code optional} will be removed from model as well as from xml.
   *
   * @param optional new dependency optional parameter
   * @return this dependency instance
   * @see #setOptional(boolean)
   */
  public Dependency setOptional(String optional) {
    this.optional = optional;
    if (!isNew()) {
      if (optional == null) {
        dependencyElement.removeChild("optional");
      } else if (dependencyElement.hasSingleChild("optional")) {
        dependencyElement.getSingleChild("optional").setText(optional);
      } else {
        dependencyElement.insertChild(createElement("optional", optional), inTheBegin());
      }
    }
    return this;
  }

  /**
   * Sets the scope of the dependency:
   *
   * <ul>
   *   <li>compile
   *   <li>runtime
   *   <li>test
   *   <li>system
   *   <li>provided
   * </ul>
   *
   * Used to calculate the various classpath used for compilation, testing, and so on. It also
   * assists in determining which artifacts to include in a distribution of this project. For more
   * information, see <a
   * href="http://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html">the
   * dependency mechanism</a>.
   *
   * <p>If {@code scope} is {@code null} and this dependency instance is associated with xml element
   * then {@code scope} will be removed from model as well as from xml.
   *
   * @param scope new dependency scope
   * @return this dependency instance
   */
  public Dependency setScope(String scope) {
    this.scope = scope;
    if (!isNew()) {
      if (scope == null) {
        dependencyElement.removeChild("scope");
      } else if (dependencyElement.hasSingleChild("scope")) {
        dependencyElement.getSingleChild("scope").setText(scope);
      } else {
        dependencyElement.appendChild(createElement("scope", scope));
      }
    }
    return this;
  }

  /**
   * Sets the type of dependency.
   *
   * <p>This defaults to <code>jar</code>. While it usually represents the extension on the filename
   * of the dependency, that is not always the case. A type can be mapped to a different extension
   * and a classifier. The type often corresponds to the packaging used, though this is also not
   * always the case. Some examples are {@code jar, war, ejb-client}. New types can be defined by
   * plugins that set {@code extensions} to {@code true}, so this is not a complete list.
   *
   * <p>If {@code type} is {@code null} and this dependency instance is associated with xml element
   * then {@code type} will be removed from model as well as from xml.
   *
   * @param type new dependency type
   * @return this dependency instance
   */
  public Dependency setType(String type) {
    this.type = type;
    if (!isNew()) {
      if (type == null) {
        dependencyElement.removeChild("type");
      } else if (dependencyElement.hasSingleChild("type")) {
        dependencyElement.getSingleChild("type").setText(type);
      } else {
        dependencyElement.appendChild(createElement("type", type));
      }
    }
    return this;
  }

  /**
   * Set the version of the dependency, e.g. <i>3.2.1</i>. In Maven 2, this can also be specified as
   * a range of versions.
   *
   * <p>If {@code version} is {@code null} and this dependency instance is associated with xml
   * element then {@code version} will be removed from model as well as from xml.
   *
   * @param version new dependency version
   * @return this dependency instance
   */
  public Dependency setVersion(String version) {
    this.version = version;
    if (!isNew()) {
      if (version == null) {
        dependencyElement.removeChild("version");
      } else if (dependencyElement.hasChild("version")) {
        dependencyElement.getSingleChild("version").setText(version);
      } else {
        dependencyElement.insertChild(
            createElement("version", version),
            afterAnyOf("artifactId", "groupId").or(inTheBegin()));
      }
    }
    return this;
  }

  /** Returns {@code true} if dependency is optional otherwise returns {@code false} */
  public boolean isOptional() {
    return parseBoolean(optional);
  }

  /**
   * Sets indicates the dependency is optional for use of this library.
   *
   * @see #setOptional(String)
   */
  public Dependency setOptional(boolean optional) {
    return setOptional(String.valueOf(optional));
  }

  @Override
  public String toString() {
    return "Dependency {groupId="
        + groupId
        + ", artifactId="
        + artifactId
        + ", version="
        + version
        + ", type="
        + type
        + "}";
  }

  public void remove() {
    if (!isNew()) {
      dependencyElement.remove();
      dependencyElement = null;
    }
  }

  NewElement asXMLElement() {
    final NewElement newElement = createElement("dependency");
    newElement.appendChild(createElement("groupId", groupId));
    newElement.appendChild(createElement("artifactId", artifactId));
    newElement.appendChild(createElement("version", version));
    if (scope != null && !scope.equals("compile")) {
      newElement.appendChild(createElement("scope", scope));
    }
    if (type != null && !type.equals("jar")) {
      newElement.appendChild(createElement("type", type));
    }
    if (classifier != null) {
      newElement.appendChild(createElement("classifier", classifier));
    }
    if (optional != null) {
      newElement.appendChild(createElement("optional", optional));
    }
    if (exclusions != null) {
      final NewElement exclusionsEl = createElement("exclusions");
      for (Exclusion exclusion : exclusions) {
        exclusionsEl.appendChild(exclusion.asXMLElement());
      }
      exclusionsEl.appendChild(exclusionsEl);
    }
    return newElement;
  }

  private void setExclusions0(Collection<? extends Exclusion> exclusions) {
    for (Exclusion exclusion : exclusions()) {
      exclusion.remove();
    }
    // use addExclusion to add and associate each new exclusion with element
    this.exclusions = new ArrayList<>(exclusions.size());
    for (Exclusion exclusion : exclusions) {
      addExclusion(exclusion);
    }
  }

  private void removeExclusions() {
    if (!isNew()) {
      dependencyElement.removeChild("exclusions");
    }
    this.exclusions = null;
  }

  private List<Exclusion> exclusions() {
    return exclusions == null ? exclusions = new LinkedList<>() : exclusions;
  }

  private boolean isNew() {
    return dependencyElement == null;
  }

  private static class ToExclusionMapper implements ElementMapper<Exclusion> {

    @Override
    public Exclusion map(Element element) {
      return new Exclusion(element);
    }
  }
}
