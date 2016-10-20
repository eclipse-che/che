/*******************************************************************************
 * Copyright (c) 2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zend Technologies - initial API and implementation
 *******************************************************************************/
package zend.com.che.plugin.zdb.server.variables;

import static zend.com.che.plugin.zdb.server.variables.IDbgDataFacet.Facet.KIND_ARRAY_MEMBER;
import static zend.com.che.plugin.zdb.server.variables.IDbgDataFacet.Facet.KIND_OBJECT_MEMBER;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
	protected AbstractDbgExpression createChild(String childName, Facet... facets) {
		List<String> childPath = new ArrayList<>(getPath());
		childPath.add(childName);
		ZendDbgVariable child = new ZendDbgVariable(getResolver(), childName, Collections.unmodifiableList(childPath), facets);
		children.add(child);
		return child;
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

}
