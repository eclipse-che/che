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
package org.eclipse.che.workspace.infrastructure.openshift;

import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.workspace.infrastructure.openshift.project.OpenShiftProject;

/**
 * @author Sergii Leshchenko
 */
public interface OpenShiftRuntimeFactory {
    OpenShiftInternalRuntime create(@Assisted OpenShiftRuntimeContext context,
                                    @Assisted OpenShiftProject openShiftProject);
}
