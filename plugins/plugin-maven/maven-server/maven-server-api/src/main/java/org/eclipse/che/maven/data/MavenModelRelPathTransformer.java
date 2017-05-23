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
package org.eclipse.che.maven.data;

import java.io.File;

/**
 * Wrapper for {@link MavenModel} class that wraps internal {@link MavenBuild} to allow translating absolute paths given by maven server
 * into relative paths.
 *
 * @author Vlad Zhukovskyi
 * @see MavenBuild
 * @since 5.11.0
 */
public class MavenModelRelPathTransformer extends MavenModelDecorator {

    private final MavenBuild build;

    public MavenModelRelPathTransformer(File projectDir, MavenModel model) {
        super(model);

        this.build = new MavenBuildRelPathTransformer(projectDir, model.getBuild());
    }

    @Override
    public MavenBuild getBuild() {
        return build;
    }
}
