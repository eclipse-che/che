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
package org.eclipse.che.ide.ext.java.client.project.classpath.valueproviders.selectnode.interceptors;

import org.eclipse.che.ide.api.project.node.Node;
import org.eclipse.che.ide.api.project.node.interceptor.NodeInterceptor;
import org.eclipse.che.ide.ext.java.shared.dto.classpath.ClasspathEntryDTO;

/**
 * Perform children interception to check if current children are available for conversion
 * and check if some node is valid for the current interceptor.
 *
 * @author Valeriy Svydenko
 */
public interface ClasspathNodeInterceptor extends NodeInterceptor {

    /** Returns {@code true} if node is valid for this interceptor else returns {@code false}. */
    boolean isNodeValid(Node node);

    /** Returns type of classpath entry. For more details see {@link ClasspathEntryDTO} **/
    int getKind();
}
