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

import java.util.List;
import org.eclipse.che.commons.xml.Element;
import org.eclipse.che.commons.xml.NewElement;

/**
 * Section for management of default dependency information for use in a group of POMs.
 *
 * @author Eugene Voevodin
 */
public class DependencyManagement {

  private Dependencies dependencies;

  Element dmElement;

  public DependencyManagement() {}

  DependencyManagement(Element element, List<Dependency> dependencies) {
    this.dmElement = element;
    this.dependencies = new Dependencies(element, dependencies);
  }

  /**
   * Returns list of dependencies or empty list if project doesn't have dependencies.
   *
   * <p><b>Note: update methods should not be used on returned list</b>
   *
   * @see #dependencies()
   */
  public List<Dependency> getDependencies() {
    return dependencies().get();
  }

  /** Returns returns {@link Dependencies} instance which helps to manage project dependencies */
  public Dependencies dependencies() {
    if (dependencies == null) {
      dependencies = new Dependencies(dmElement);
    }
    return dependencies;
  }

  void remove() {
    if (dmElement != null) {
      dmElement.remove();
    }
  }

  NewElement asXMLElement() {
    return createElement("dependencyManagement", dependencies.asXMLElement());
  }
}
