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
package org.eclipse.che.generator.archetype;

import java.io.File;

/**
 * Represents result of a project generating.
 *
 * @author Artem Zatsarynnyi
 */
class GenerationResult {
    private final boolean success;

    private File generatedProject;
    private File report;

    GenerationResult(boolean success, File generatedProject, File report) {
        this.success = success;
        this.generatedProject = generatedProject;
        this.report = report;
    }

    /**
     * Reports whether project generation process successful or failed.
     *
     * @return {@code true} if project was successfully generated and {@code false} otherwise
     */
    boolean isSuccessful() {
        return success;
    }

    /** Get zipped file with generated project. */
    File getGeneratedProject() {
        return generatedProject;
    }

    void setGeneratedProject(File generatedProject) {
        this.generatedProject = generatedProject;
    }

    /**
     * Provides report about project generation process.
     *
     * @return report about project generation
     */
    File getGenerationReport() {
        return report;
    }
}
