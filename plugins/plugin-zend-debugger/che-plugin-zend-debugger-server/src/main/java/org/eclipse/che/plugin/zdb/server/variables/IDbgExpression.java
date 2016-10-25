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

import java.util.List;

/**
 * Common interface for Zend dbg expressions.
 *
 * @author Bartlomiej Laczkowski
 */
public interface IDbgExpression extends IDbgDataFacet, IDbgDataType {

    /**
     * Returns textual representation/statement for this expression.
     *
     * @return textual representation/statement for this expression
     */
    public String getStatement();

    /**
     * Returns expression value string.
     *
     * @return expression value string
     */
    public String getValue();

    /**
     * Returns expression value children.
     *
     * @return expression value children
     */
    public List<? extends IDbgExpression> getChildren();

    /**
     * Returns number of existing children.
     *
     * @return number of existing children
     */
    public int getChildrenCount();

    /**
     * Resolves this expression (computes its value on engine side).
     */
    public void resolve();

}
