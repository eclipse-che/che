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

package org.eclipse.che.jdt.rest;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.JavaModel;
import org.eclipse.jdt.internal.core.JavaModelManager;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * Special service which allows control parameters of compiler for current project.
 *
 * @author Dmitry Shnurenko
 */
@Path("/jdt/{wsId}/compiler-settings")
public class CompilerSetupService {

    private static final JavaModel JAVA_MODEL = JavaModelManager.getJavaModelManager().getJavaModel();

    @POST
    @Path("/set")
    @Consumes(APPLICATION_JSON)
    public void setParameters(@QueryParam("projectpath") String projectPath, @NotNull Map<String, String> changedParameters) {
        IJavaProject project = JAVA_MODEL.getJavaProject(projectPath);

        for (Map.Entry<String, String> entry : changedParameters.entrySet()) {
            String optionId = entry.getKey();
            String value = entry.getValue();

            project.setOption(optionId, value);
        }
    }

    @GET
    @Path("/all")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Map<String, String> getAllParameters(@QueryParam("projectpath") String projectPath) {
        IJavaProject project = JAVA_MODEL.getJavaProject(projectPath);

        //noinspection unchecked
        Map<String, String> map = project.getOptions(true);

        CompilerOptions options = new CompilerOptions(map);

        //noinspection unchecked
        return options.getMap();
    }
}
