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
package org.eclipse.che.plugin.maven.shared.event;

import java.util.List;

/**
 * Event that describes Maven notification output.
 */
public interface MavenUpdateEvent extends MavenOutputEvent {
    /** Returns list of projects which were modified. */
    List<String> getUpdatedProjects();

    /** Returns list of projects which were removed. */
    List<String> getRemovedProjects();
}
