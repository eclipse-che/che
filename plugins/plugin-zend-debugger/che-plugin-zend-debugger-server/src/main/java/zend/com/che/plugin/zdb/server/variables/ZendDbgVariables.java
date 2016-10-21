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
package zend.com.che.plugin.zdb.server.variables;

import static zend.com.che.plugin.zdb.server.variables.IDbgDataFacet.Facet.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import zend.com.che.plugin.zdb.server.utils.ZendDbgUtils;

/**
 * Expression for fetching current stack frame variables.
 * 
 * @author Bartlomiej Laczkowski
 */
public class ZendDbgVariables extends AbstractDbgExpression {

	private final static String DUMP_VARIABLES_EXPRESSION = "eval('if (isset($this)) {$this;}; return get_defined_vars();')";

	private final List<IDbgVariable> children = new ArrayList<>();

	public ZendDbgVariables(ZendDbgExpressionResolver resolver) {
		super(resolver, DUMP_VARIABLES_EXPRESSION);
	}

	@Override
	protected AbstractDbgExpression createChild(String name, Facet... facets) {
		name = '$' + name;
		Facet facet = KIND_LOCAL;
		if (ZendDbgUtils.isThis(name))
			facet = KIND_THIS;
		else if (ZendDbgUtils.isSuperGlobal(name))
			facet = KIND_SUPER_GLOBAL;
		ZendDbgVariable variable = new ZendDbgVariable(getResolver(), name, Collections.singletonList(name), facet);
		children.add(variable);
		return variable;
	}

	@Override
	public List<IDbgVariable> getChildren() {
		return children;
	}

}
