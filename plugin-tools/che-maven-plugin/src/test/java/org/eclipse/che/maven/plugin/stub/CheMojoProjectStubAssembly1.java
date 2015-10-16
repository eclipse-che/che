/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.maven.plugin.stub;

import java.io.File;

/**
 * Stub for sample assembly1
 * @author Florent Benoit
 */
public class CheMojoProjectStubAssembly1 extends CheMojoProjectStub {

    /** {@inheritDoc} */
    public File getBasedir() {
        return new File(super.getBasedir() + "/src/test/projects/assembly1");
    }

}

