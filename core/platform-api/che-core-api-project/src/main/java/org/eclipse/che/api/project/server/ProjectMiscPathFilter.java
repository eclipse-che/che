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
package org.eclipse.che.api.project.server;

import org.eclipse.che.api.vfs.server.Path;
import org.eclipse.che.api.vfs.server.SystemVirtualFilePathFilter;

/**
 * A system path filter that ignores the project misc file (misc.xml).
 * 
 * @author Tareq Sharafy (tareq.sharafy@sap.com)
 */
public class ProjectMiscPathFilter implements SystemVirtualFilePathFilter {

    @Override
    public boolean accept(String workspace, Path path) {
        int length = path.length();
        return !(length >= 2
                && Constants.CODENVY_DIR.equals(path.element(length - 2))
                && Constants.CODENVY_MISC_FILE.equals(path.element(length - 1)));
    }

}
