/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.reference;

/**
 * The class provides methods which allows extract fqn from nodes which contains it.
 *
 * @author Dmitry Shnurenko
 */
public interface FqnProvider {

    /**
     * The methods extracts fqn from nodes which contains it. If node doesn't contain fqn, method returns empty string.
     *
     * @param object
     *         node for which fqn will be extract
     * @return string representation of fqn or empty string if node doesn't contain fqn.
     */
    String getFqn(Object object);
}
