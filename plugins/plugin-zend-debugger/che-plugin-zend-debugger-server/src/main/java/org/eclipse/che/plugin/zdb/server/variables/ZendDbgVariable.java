/*******************************************************************************
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.zdb.server.variables;

import static org.eclipse.che.plugin.zdb.server.variables.IDbgDataFacet.Facet.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Zend debug variable descriptor.
 *
 * @author Bartlomiej Laczkowski
 */
public class ZendDbgVariable extends AbstractDbgExpression implements IDbgVariable {

    private String simpleName;
    private final List<String> path;
    private final List<IDbgVariable> children = new ArrayList<>();

    public ZendDbgVariable(ZendDbgExpressionResolver resolver, String name, List<String> path, Facet... facets) {
        super(resolver, name, facets);
        this.path = path;
    }

    @Override
    public List<IDbgVariable> getChildren() {
        return children;
    }

    @Override
    public List<String> getPath() {
        return path;
    }

    @Override
    public String getName() {
        if (simpleName == null) {
            String fullName = getStatement();
            if (hasFacet(KIND_OBJECT_MEMBER)) {
                simpleName = fullName.substring(fullName.lastIndexOf(":") + 1);
            } else if (hasFacet(KIND_ARRAY_MEMBER)) {
                simpleName = '[' + fullName + ']';
            } else {
                simpleName = fullName;
            }
        }
        return simpleName;
    }

    @Override
    protected AbstractDbgExpression createChild(String childName, Facet... facets) {
        List<String> childPath = new ArrayList<>(getPath());
        childPath.add(childName);
        ZendDbgVariable child = new ZendDbgVariable(getResolver(), childName, Collections.unmodifiableList(childPath),
                facets);
        children.add(child);
        return child;
    }

}
