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
package org.eclipse.che.ide.project.node.icon;

import org.eclipse.che.ide.api.resources.Resource;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Provides mechanism for resolving icon, based on file extension.
 *
 * @author Vlad Zhukovskiy
 */
public interface NodeIconProvider {
    /**
     * Resolve icon based on given {@code resource}.
     *
     * @param resource
     *         the resource to resolve icon
     * @return icon or null if icons for this extension doesn't exist
     */
    SVGResource getIcon(Resource resource);
}
