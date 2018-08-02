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

import static java.util.Objects.requireNonNull;
import static org.eclipse.che.commons.xml.NewElement.createElement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.che.commons.xml.Element;
import org.eclipse.che.commons.xml.NewElement;

/**
 * Describes set of dependencies
 *
 * @author Eugene Voevodin
 */
public class Dependencies {

  private List<Dependency> dependencies;

  Element dependenciesParent;

  Dependencies(Element dependenciesParent) {
    this(dependenciesParent, new ArrayList<Dependency>());
  }

  Dependencies(Element dependenciesParent, List<Dependency> dependencies) {
    this.dependenciesParent = dependenciesParent;
    this.dependencies = dependencies;
  }

  /** Returns list of dependencies */
  public List<Dependency> get() {
    return new ArrayList<>(dependencies());
  }

  private List<Dependency> dependencies() {
    return dependencies == null ? dependencies = new ArrayList<>() : dependencies;
  }

  /**
   * Adds new dependency to the end of dependencies list.
   *
   * <p>Creates dependencies tag if element doesn't have dependencies yet
   *
   * @param dependency new dependency which will be added to the end of dependencies list
   */
  public Dependencies add(Dependency dependency) {
    dependencies.add(requireNonNull(dependency, "Required not null dependency"));
    if (!isNew()) {
      addDependencyToXML(dependency);
    }
    return this;
  }

  /**
   * Removes dependency from the list of existing dependencies.
   *
   * @param dependency dependency which should be removed
   */
  public Dependencies remove(Dependency dependency) {
    if (dependencies().remove(requireNonNull(dependency, "Required not null dependency"))
        && !isNew()) {
      removeDependencyFromXML(dependency);
    }
    return this;
  }

  /**
   * Sets dependencies associated with a project.
   *
   * <p>These dependencies are used to construct a classpath for your project during the build
   * process. They are automatically downloaded from the repositories defined in this project. See
   * <a
   * href="http://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html">the
   * dependency mechanism</a> for more information.
   */
  public Dependencies set(Collection<? extends Dependency> newDependencies) {
    if (newDependencies == null || newDependencies.isEmpty()) {
      removeDependencies();
    } else if (isNew()) {
      dependencies = new ArrayList<>(newDependencies);
    } else {
      setDependencies(newDependencies);
    }
    return this;
  }

  private void removeDependencies() {
    if (!isNew()) {
      dependenciesParent.removeChild("dependencies");
    }
    this.dependencies = null;
  }

  /** Returns selected dependencies or {@code null} if nothing was selected */
  public Dependency first() {
    return dependencies().isEmpty() ? null : dependencies.get(0);
  }

  /** Returns last selected dependency or {@code null} if nothing was selected */
  public Dependency last() {
    return dependencies().isEmpty() ? null : dependencies.get(dependencies.size() - 1);
  }

  void remove() {
    dependencies = null;
  }

  NewElement asXMLElement() {
    final NewElement newDependencies = createElement("dependencies");
    for (Dependency dependency : dependencies) {
      newDependencies.appendChild(dependency.asXMLElement());
    }
    return newDependencies;
  }

  private void addDependencyToXML(Dependency dependency) {
    if (dependenciesParent.hasSingleChild("dependencies")) {
      dependenciesParent.getSingleChild("dependencies").appendChild(dependency.asXMLElement());
    } else {
      dependenciesParent.appendChild(createElement("dependencies", dependency.asXMLElement()));
    }
    dependency.dependencyElement = dependenciesParent.getSingleChild("dependencies").getLastChild();
  }

  private void removeDependencyFromXML(Dependency dependency) {
    if (dependencies.isEmpty()) {
      dependenciesParent.removeChild("dependencies");
      dependency.dependencyElement = null;
    } else {
      dependency.remove();
    }
  }

  private void setDependencies(Collection<? extends Dependency> newDependencies) {
    // removing all dependencies from xml tree
    for (Dependency dependency : dependencies) {
      dependency.remove();
    }
    // add and associate each new dependency with element in tree
    dependencies = new ArrayList<>(newDependencies.size());
    for (Dependency newDependency : newDependencies) {
      add(newDependency);
    }
  }

  private boolean isNew() {
    return dependenciesParent == null;
  }
}
