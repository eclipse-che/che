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
package org.eclipse.che.plugin.jsonexample;

import com.google.inject.Inject;

import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.server.FileEntry;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.RegisteredProject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Service for counting lines of code within all JSON files in a given project.
 */
@Path("json-example/{ws-id}")
public class JsonLocService {

    private ProjectManager projectManager;

    /**
     * Constructor for the JSON Exapmle lines of code service.
     *
     * @param projectManager
     *         the {@link ProjectManager} that is used to access the project resources
     */
    @Inject
    public JsonLocService(ProjectManager projectManager) {
        this.projectManager = projectManager;
    }

    private static int countLines(FileEntry fileEntry) throws ServerException, ForbiddenException {
        String content = fileEntry.getVirtualFile().getContentAsString();
        String[] lines = content.split("\r\n|\r|\n");
        return lines.length;
    }

    private static boolean isJsonFile(FileEntry fileEntry) {
        return fileEntry.getName().endsWith("json");
    }

    /**
     * Count LOC for all JSON files within the given project.
     *
     * @param projectPath
     *         the path to the project that contains the JSON files for which to calculate the LOC
     * @return a Map mapping the file name to their respective LOC value
     * @throws ServerException
     *         in case the server encounters an error
     * @throws NotFoundException
     *         in case the project couldn't be found
     * @throws ForbiddenException
     *         in case the operation is forbidden
     */
    @GET
    @Path("{projectPath}")
    public Map<String, String> countLinesPerFile(@PathParam("projectPath") String projectPath)
            throws ServerException, NotFoundException, ForbiddenException {

        Map<String, String> linesPerFile = new LinkedHashMap<>();
        RegisteredProject project = projectManager.getProject(projectPath);

        for (FileEntry child : project.getBaseFolder().getChildFiles()) {
            if (isJsonFile(child)) {
                linesPerFile.put(child.getName(), Integer.toString(countLines(child)));
            }
        }

        return linesPerFile;
    }
}
