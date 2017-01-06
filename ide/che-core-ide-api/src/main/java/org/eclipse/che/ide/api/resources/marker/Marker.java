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
package org.eclipse.che.ide.api.resources.marker;

import com.google.common.annotations.Beta;

import org.eclipse.che.ide.api.resources.Project.ProblemProjectMarker;
import org.eclipse.che.ide.api.resources.Resource;

/**
 * Markers are a general mechanism for associating notes and meta-data with resources.
 * <p/>
 * Each marker has a type string, specifying its unique id. The resources plugin defines only one standard
 * marker (at this moment): {@link ProblemProjectMarker#PROBLEM_PROJECT}.
 * <p/>
 * Marker, by nature is only runtime attribute and doesn't store on the server side.
 *
 * @author Vlad Zhukovskiy
 * @see Resource#getMarker(String)
 * @see Resource#getMarkers()
 * @see Resource#addMarker(Marker)
 * @see Resource#deleteMarker(String)
 * @since 4.4.0
 */
@Beta
public interface Marker {

    /**
     * Kind constant (bit mask) indicating that the marker has been created to given resource.
     *
     * @since 4.4.0
     */
    int CREATED = 0x1;

    /**
     * Kind constant (bit mask) indicating that the marker has been removed from given resource.
     *
     * @since 4.4.0
     */
    int REMOVED = 0x2;

    /**
     * Kind constant (bit mask) indicating that the marker has been updated to given resource.
     *
     * @since 4.4.0
     */
    int UPDATED = 0x4;

    /**
     * Returns the type of this marker. The returned marker type will not be {@code null}.
     *
     * @return the type of this marker
     * @since 4.4.0
     */
    String getType();
}
