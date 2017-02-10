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
package org.eclipse.che.ide.ext.java.client.resource;

import com.google.common.annotations.Beta;

import org.eclipse.che.ide.api.resources.marker.Marker;
import org.eclipse.che.ide.ext.java.shared.ContentRoot;

/**
 * @author Vlad Zhukovskiy
 */
@Beta
public class SourceFolderMarker implements Marker {

    public static final String ID = "javaSourceFolderMarker";

    private final ContentRoot contentRoot;

    public SourceFolderMarker(ContentRoot contentRoot) {

        this.contentRoot = contentRoot;
    }

    @Override
    public String getType() {
        return ID;
    }

    public ContentRoot getContentRoot() {
        return contentRoot;
    }
}
