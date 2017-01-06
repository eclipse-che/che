/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.maven.tools;

import org.eclipse.che.commons.xml.Element;
import org.eclipse.che.commons.xml.NewElement;

import java.util.List;

import static org.eclipse.che.commons.xml.NewElement.createElement;

/**
 * Section for management of default dependency information for use in a group of POMs.
 *
 * @author Eugene Voevodin
 */
public class DependencyManagement {

    private Dependencies dependencies;

    Element dmElement;

    public DependencyManagement() {
    }

    DependencyManagement(Element element, List<Dependency> dependencies) {
        this.dmElement = element;
        this.dependencies = new Dependencies(element, dependencies);
    }

    /**
     * Returns list of dependencies or empty list if project doesn't have dependencies.
     * <p/>
     * <b>Note: update methods should not be used on returned list</b>
     *
     * @see #dependencies()
     */
    public List<Dependency> getDependencies() {
        return dependencies().get();
    }

    /**
     * Returns returns {@link Dependencies} instance which
     * helps to manage project dependencies
     */
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
