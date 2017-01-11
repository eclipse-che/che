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
package org.eclipse.che.ide.api.resources.modification;

import com.google.common.annotations.Beta;

import org.eclipse.che.ide.api.resources.marker.Marker;

/**
 * Marker that indicates resource which is cut by {@link CutProvider}.
 *
 * @author Vlad Zhukovskiy
 * @since 4.4.0
 */
@Beta
public class CutResourceMarker implements Marker {

    public static final String ID = "cutResourceMarker";

    @Override
    public String getType() {
        return ID;
    }
}
