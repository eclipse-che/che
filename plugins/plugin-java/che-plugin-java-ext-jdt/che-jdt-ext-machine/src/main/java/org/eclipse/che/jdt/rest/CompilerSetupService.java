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

import com.google.inject.name.Named;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.JavaModel;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.ui.fix.StringCleanUp;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import static com.google.common.io.Files.createParentDirs;
import static com.google.common.io.Files.toByteArray;
import static com.google.common.io.Files.readLines;
import static com.google.common.io.Files.write;

/**
 * Special service which allows control parameters of compiler for current project or current workspace.
 *
 * @author Dmitry Shnurenko
 */
@Path("/jdt/{wsId}/compiler-settings")
public class CompilerSetupService {

    @Inject
    @Named("che.jdt.settings.dir")
    String settingsDir;

    @Inject
    @Named("jdt.preferences.file.name")
    String preferencesFileName;

    private static final JavaModel JAVA_MODEL = JavaModelManager.getJavaModelManager().getJavaModel();

    /**
     * Set java compiler preferences {@code changedParameters} for project by not empty path {@code projectpath}. If {@code projectpath}
     * is empty then java compiler preferences will be set for current workspace.
     *
     * @param projectPath
     *         project path
     * @param changedParameters
     *         java compiler preferences
     */
    @POST
    @Path("/set")
    @Consumes(APPLICATION_JSON)
    public void setParameters(@QueryParam("projectpath") String projectPath, @NotNull Map<String, String> changedParameters)
            throws IOException {
        if (projectPath == null || projectPath.isEmpty()) {

            updateCreateSettingsFile(changedParameters);

            JavaCore.setOptions(new Hashtable<>(changedParameters));
            return;
        }
        IJavaProject project = JAVA_MODEL.getJavaProject(projectPath);
        project.setOptions(changedParameters);
    }

    /**
     * Return java compiler preferences for current project by not empty path {@code projectpath}. If {@code projectpath} if empty then
     * return java compile preferences for current workspace.
     *
     * @param projectPath
     *         project path
     * @return java compiler preferences
     */
    @GET
    @Path("/all")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Map<String, String> getAllParameters(@QueryParam("projectpath") String projectPath) {
        if (projectPath == null || projectPath.isEmpty()) {
            //noinspection unchecked
            CompilerOptions options = new CompilerOptions(new HashMap<>(JavaCore.getOptions()));
            //noinspection unchecked
            return options.getMap();
        }

        IJavaProject project = JAVA_MODEL.getJavaProject(projectPath);

        //noinspection unchecked
        Map<String, String> map = project.getOptions(true);
        CompilerOptions options = new CompilerOptions(map);

        //noinspection unchecked
        return options.getMap();
    }

    private void updateCreateSettingsFile(Map<String, String> changedParameters) throws IOException {
        if (changedParameters.size() == 0) {
            return;
        }

        File preferencesFile = Paths.get(settingsDir, preferencesFileName).toFile();
        createParentDirs(preferencesFile);

        StringBuilder contentStringBuilder = new StringBuilder();
        if (preferencesFile.createNewFile()) {
            for (String property : changedParameters.keySet()) {
                contentStringBuilder.append(property).append("=").append(changedParameters.get(property)).append("\n");
            }
        } else {
            Map<String, String> options = readLines(preferencesFile, Charset.defaultCharset())
                    .stream()
                    .map(fileLine -> fileLine.split("="))
                    .collect(Collectors.toMap(elements -> elements[0], elements -> elements[1]));

            options.putAll(changedParameters);

            for (String param : options.keySet()) {
                contentStringBuilder.append(param).append("=").append(options.get(param)).append("\n");
            }
        }

        write(contentStringBuilder.toString().getBytes(), preferencesFile);
    }
}
