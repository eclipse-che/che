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
package org.eclipse.che.plugin.java.server.rest;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.ide.ext.java.shared.dto.Change;
import org.eclipse.che.ide.ext.java.shared.dto.ConflictImportDTO;
import org.eclipse.che.ide.ext.java.shared.dto.OrganizeImportResult;
import org.eclipse.che.ide.ext.java.shared.dto.Problem;
import org.eclipse.che.ide.ext.java.shared.dto.ProposalApplyResult;
import org.eclipse.che.ide.ext.java.shared.dto.Proposals;
import org.eclipse.che.plugin.java.server.CodeAssist;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaModel;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.corext.format.Formatter;
import org.eclipse.jface.text.BadLocationException;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.List;

/**
 * @author Evgen Vidolob
 */
@Path("java/code-assist")
public class CodeAssistService {

    private static final JavaModel model = JavaModelManager.getJavaModelManager().getJavaModel();

    @Inject
    private CodeAssist codeAssist;

    @Inject
    private Formatter formatter;

    @POST
    @Path("compute/completion")
    @Produces("application/json")
    public Proposals computeCompletionProposals(@QueryParam("projectpath") String projectPath,
                                                @QueryParam("fqn") String fqn,
                                                @QueryParam("offset") int offset, String content) throws JavaModelException {
        IJavaProject javaProject = model.getJavaProject(projectPath);
        return codeAssist.computeProposals(javaProject, fqn, offset, content);

    }

    @GET
    @Path("apply/completion")
    @Produces("application/json")
    public ProposalApplyResult applyCompletion(@QueryParam("sessionid") String sessionId,
                                               @QueryParam("index") int index,
                                               @DefaultValue("true") @QueryParam("insert") boolean insert) {
        return codeAssist.applyCompletion(sessionId, index, insert);
    }

    @POST
    @Path("compute/assist")
    @Produces("application/json")
    @Consumes("application/json")
    public Proposals computeAssistProposals(@QueryParam("projectpath") String projectPath,
                                            @QueryParam("fqn") String fqn,
                                            @QueryParam("offset") int offset,
                                            List<Problem> problems) throws CoreException {
        IJavaProject javaProject = model.getJavaProject(projectPath);
        return codeAssist.computeAssistProposals(javaProject, fqn, offset, problems);

    }

    @GET
    @Produces("text/html")
    @Path("compute/info")
    public String getJavaDoc(@QueryParam("sessionid") String sessionId,
                             @QueryParam("index") int index, @Context UriInfo uriInfo) {

        return codeAssist.getJavaDoc(sessionId, index);
    }

    /**
     * Organizes the imports of a compilation unit.
     *
     * @param projectPath
     *         path to the project
     * @param fqn
     *         fully qualified name of the java file
     * @return list of imports which have conflicts
     */
    @POST
    @Path("/organize-imports")
    @Produces({MediaType.APPLICATION_JSON})
    public OrganizeImportResult organizeImports(@QueryParam("projectpath") String projectPath,
                                                @QueryParam("fqn") String fqn) throws NotFoundException,
                                                                                        CoreException,
                                                                                        BadLocationException {
        IJavaProject project = model.getJavaProject(projectPath);
        return codeAssist.organizeImports(project, fqn);
    }

    /**
     * Applies chosen imports after resolving conflicts.
     *
     * @param projectPath
     *         path to the project
     * @param fqn
     *         fully qualified name of the java file
     * @param  chosen
     * @return list of document changes
     */
    @POST
    @Path("/apply-imports")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<Change> applyChosenImports(@QueryParam("projectpath") String projectPath,
                                           @QueryParam("fqn") String fqn,
                                           ConflictImportDTO chosen) throws NotFoundException,
                                                                    CoreException,
                                                                    BadLocationException {
        IJavaProject project = model.getJavaProject(projectPath);
        return codeAssist.applyChosenImports(project, fqn, chosen.getTypeMatches());
    }

    @POST
    @Path("/format")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Creates edits that describe how to format the given string")
    public List<Change> getFormatChanges(@ApiParam(value = "The given offset to start recording the edits (inclusive)")
                                         @QueryParam("offset") int offset,
                                         @ApiParam(value = "The given length to stop recording the edits (exclusive)")
                                         @QueryParam("length") int length,
                                         @ApiParam(value = "The content to format. Java code formatting is supported only")
                                         final String content) throws BadLocationException, IllegalArgumentException {
        return formatter.getFormatChanges(content, offset, length);
    }

}
