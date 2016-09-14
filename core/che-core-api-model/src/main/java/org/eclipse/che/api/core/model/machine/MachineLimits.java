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
package org.eclipse.che.api.core.model.machine;

/**
 * Describes machine limits such as RAM size.
 *
 * @author Alexander Garagatyi
 */
public interface MachineLimits {
    /** Get memory size (in megabytes) that is allocated for starting machine. */
    int getRam();
}
