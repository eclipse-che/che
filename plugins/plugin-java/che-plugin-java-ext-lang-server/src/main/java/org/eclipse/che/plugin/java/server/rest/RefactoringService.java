/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.java.server.rest;

import com.google.inject.Inject;
import java.util.function.Function;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ChangeCreationResult;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ChangeEnabledState;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ChangePreview;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.CreateMoveRefactoring;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.CreateRenameRefactoring;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ElementToMove;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.LinkedRenameRefactoringApply;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.MoveSettings;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringChange;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringPreview;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringResult;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringSession;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringStatus;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RenameRefactoringSession;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RenameSettings;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ReorgDestination;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ValidateNewName;
import org.eclipse.che.plugin.java.server.refactoring.RefactoringException;
import org.eclipse.che.plugin.java.server.refactoring.RefactoringManager;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaModel;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringAvailabilityTester;

/**
 * Service for all Java refactorings
 *
 * @author Evgen Vidolob
 */
@Path("java/refactoring")
public class RefactoringService {
  private static final JavaModel model = JavaModelManager.getJavaModelManager().getJavaModel();
  private RefactoringManager manager;

  @Inject
  public RefactoringService(RefactoringManager manager) {
    this.manager = manager;
  }

  /**
   * Create move refactoring session.
   *
   * @param cmr move settings, contains resource paths to move.
   * @return refactoring session id.
   * @throws JavaModelException when JavaModel has a failure
   * @throws RefactoringException when impossible to create move refactoring session
   */
  @POST
  @Path("move/create")
  @Consumes("application/json")
  @Produces("text/plain")
  public String createMoveRefactoring(CreateMoveRefactoring cmr)
      throws JavaModelException, RefactoringException {
    IJavaProject javaProject = model.getJavaProject(cmr.getProjectPath());
    IJavaElement[] javaElements;
    try {
      Function<ElementToMove, IJavaElement> map =
          javaElement -> {
            try {
              if (javaElement.isPack()) {
                return javaProject.findPackageFragment(
                    new org.eclipse.core.runtime.Path(javaElement.getPath()));
              } else {

                IType type = javaProject.findType(javaElement.getPath());

                // in some cases client may send FQN that doesn't exist
                if (type == null) {
                  throw new IllegalArgumentException("Can't find type: " + javaElement.getPath());
                }
                return type.getCompilationUnit();
              }
            } catch (JavaModelException e) {
              throw new IllegalArgumentException(e);
            }
          };
      javaElements = cmr.getElements().stream().map(map).toArray(IJavaElement[]::new);

    } catch (IllegalArgumentException e) {
      if (e.getCause() instanceof JavaModelException) {
        throw (JavaModelException) e.getCause();
      } else {
        throw e;
      }
    }
    if (RefactoringAvailabilityTester.isMoveAvailable(new IResource[0], javaElements)) {
      return manager.createMoveRefactoringSession(javaElements);
    }

    throw new RefactoringException("Can't create move refactoring.");
  }

  /**
   * Set destination for reorg refactorings.
   *
   * @param destination the destination for reorg refactoring
   * @return refactoring status
   * @throws RefactoringException when there are no corresponding refactoring session
   * @throws JavaModelException when JavaModel has a failure
   */
  @POST
  @Path("set/destination")
  @Produces("application/json")
  @Consumes("application/json")
  public RefactoringStatus setDestination(ReorgDestination destination)
      throws RefactoringException, JavaModelException {
    return manager.setRefactoringDestination(destination);
  }

  /**
   * Set move refactoring wizard setting.
   *
   * @param settings the move settings
   * @throws RefactoringException when there are no corresponding refactoring session
   */
  @POST
  @Path("set/move/setting")
  @Consumes("application/json")
  public void setMoveSettings(MoveSettings settings) throws RefactoringException {
    manager.setMoveSettings(settings);
  }

  /**
   * Create refactoring change. Creation of the change starts final checking for refactoring.
   * Without creating change refactoring can't be applied.
   *
   * @param refactoringSession the refactoring session.
   * @return result of creation of the change.
   * @throws RefactoringException when there are no corresponding refactoring session
   */
  @POST
  @Path("create/change")
  @Produces("application/json")
  @Consumes("application/json")
  public ChangeCreationResult createChange(RefactoringSession refactoringSession)
      throws RefactoringException {
    return manager.createChange(refactoringSession.getSessionId());
  }

  /**
   * Get refactoring preview. Preview is tree of refactoring changes.
   *
   * @param refactoringSession the refactoring session.
   * @return refactoring preview tree
   * @throws RefactoringException when there are no corresponding refactoring session
   */
  @POST
  @Path("get/preview")
  @Produces("application/json")
  @Consumes("application/json")
  public RefactoringPreview getRefactoringPreview(RefactoringSession refactoringSession)
      throws RefactoringException {
    return manager.getRefactoringPreview(refactoringSession.getSessionId());
  }

  /**
   * Change enabled/disabled state of the corresponding refactoring change.
   *
   * @param state the state of refactoring change
   * @throws RefactoringException when there are no corresponding refactoring session or refactoring
   *     change
   */
  @POST
  @Path("change/enabled")
  public void changeChangeEnabledState(ChangeEnabledState state) throws RefactoringException {
    manager.changeChangeEnabled(state);
  }

  /**
   * Get refactoring change preview. Preview contains new and old content of the file
   *
   * @param change the change to get preview
   * @return refactoring change preview
   * @throws RefactoringException
   */
  @POST
  @Path("change/preview")
  @Produces("application/json")
  @Consumes("application/json")
  public ChangePreview getChangePreview(RefactoringChange change) throws RefactoringException {
    return manager.getChangePreview(change);
  }

  /**
   * Apply refactoring.
   *
   * @param session the refactoring session
   * @return the result fo applied refactoring
   * @throws RefactoringException when there are no corresponding refactoring session
   */
  @POST
  @Path("apply")
  @Produces("application/json")
  @Consumes("application/json")
  public RefactoringResult applyRefactoring(RefactoringSession session)
      throws RefactoringException, JavaModelException {
    return manager.applyRefactoring(session.getSessionId());
  }

  /**
   * Create rename refactoring session.
   *
   * @param settings rename settings
   * @return the rename refactoring session
   * @throws CoreException when RenameSupport can't be created
   * @throws RefactoringException when Java element was not found
   */
  @POST
  @Path("rename/create")
  @Produces("application/json")
  @Consumes("application/json")
  public RenameRefactoringSession createRenameRefactoring(CreateRenameRefactoring settings)
      throws CoreException, RefactoringException {
    IJavaProject javaProject = model.getJavaProject(settings.getProjectPath());
    IJavaElement elementToRename;
    ICompilationUnit cu = null;
    switch (settings.getType()) {
      case COMPILATION_UNIT:
        elementToRename = javaProject.findType(settings.getPath()).getCompilationUnit();
        break;
      case PACKAGE:
        elementToRename =
            javaProject.findPackageFragment(new org.eclipse.core.runtime.Path(settings.getPath()));
        break;
      case JAVA_ELEMENT:
        cu = javaProject.findType(settings.getPath()).getCompilationUnit();
        elementToRename = getSelectionElement(cu, settings.getOffset());
        break;
      default:
        elementToRename = null;
    }
    if (elementToRename == null) {
      throw new RefactoringException("Can't find java element to rename.");
    }

    return manager.createRenameRefactoring(
        elementToRename, cu, settings.getOffset(), settings.isRefactorLightweight());
  }

  /**
   * Apply linked mode rename refactoring.
   *
   * @param refactoringApply linked mode setting and refactoring session id
   * @return the result fo applied refactoring
   * @throws RefactoringException when there are no corresponding refactoring session
   * @throws CoreException when impossible to apply rename refactoring
   */
  @POST
  @Path("rename/linked/apply")
  @Consumes("application/json")
  @Produces("application/json")
  public RefactoringResult applyLinkedModeRename(LinkedRenameRefactoringApply refactoringApply)
      throws RefactoringException, CoreException {
    return manager.applyLinkedRename(refactoringApply);
  }

  /**
   * Validate new name. Used for validation new name in rename refactoring wizard.
   *
   * @param newName the new element name
   * @return the status of validation
   * @throws RefactoringException when there are no corresponding refactoring session
   */
  @POST
  @Path("rename/validate/name")
  @Consumes("application/json")
  @Produces("application/json")
  public RefactoringStatus validateNewName(ValidateNewName newName) throws RefactoringException {
    return manager.renameValidateNewName(newName);
  }

  /**
   * Set rename refactoring wizard settings.
   *
   * @param settings refactoring wizard settings
   * @throws RefactoringException when there are no corresponding refactoring session
   */
  @POST
  @Path("set/rename/settings")
  @Consumes("application/json")
  public void setRenameSettings(RenameSettings settings) throws RefactoringException {
    manager.setRenameSettings(settings);
  }

  /**
   * Make reindex for the project.
   *
   * @param projectPath path to the project
   * @throws JavaModelException when something is wrong
   */
  @GET
  @Path("reindex")
  @Consumes("text/plain")
  public Response reindexProject(@QueryParam("projectpath") String projectPath)
      throws JavaModelException {
    manager.reindexProject(model.getJavaProject(projectPath));
    return Response.ok().build();
  }

  private IJavaElement getSelectionElement(ICompilationUnit compilationUnit, int offset)
      throws JavaModelException, RefactoringException {
    IJavaElement[] javaElements = compilationUnit.codeSelect(offset, 0);
    if (javaElements != null && javaElements.length > 0) {
      return javaElements[0];
    }
    throw new RefactoringException("Can't find java element to rename.");
  }
}
