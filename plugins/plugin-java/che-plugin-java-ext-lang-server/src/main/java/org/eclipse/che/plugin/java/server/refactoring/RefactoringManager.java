/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.java.server.refactoring;

import static org.eclipse.che.ide.ext.java.shared.dto.refactoring.RenameRefactoringSession.RenameWizard;
import static org.eclipse.che.ide.ext.java.shared.dto.refactoring.ReorgDestination.DestinationType;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.inject.Singleton;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.eclipse.che.commons.schedule.ScheduleRate;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.ide.ext.java.shared.dto.LinkedModeModel;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ChangeCreationResult;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ChangeEnabledState;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ChangePreview;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.LinkedRenameRefactoringApply;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.MoveSettings;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringChange;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringPreview;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringResult;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringStatus;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RenameRefactoringSession;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RenameSettings;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ReorgDestination;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ValidateNewName;
import org.eclipse.che.jdt.util.JdtFlags;
import org.eclipse.che.plugin.java.server.refactoring.session.MoveRefactoringSession;
import org.eclipse.che.plugin.java.server.refactoring.session.RefactoringSession;
import org.eclipse.che.plugin.java.server.refactoring.session.RenameLinkedModeRefactoringSession;
import org.eclipse.che.plugin.java.server.refactoring.session.RenameSession;
import org.eclipse.che.plugin.java.server.refactoring.session.ReorgRefactoringSession;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.corext.refactoring.reorg.IReorgPolicy;
import org.eclipse.jdt.internal.corext.refactoring.reorg.JavaMoveProcessor;
import org.eclipse.jdt.internal.corext.refactoring.reorg.NullReorgQueries;
import org.eclipse.jdt.internal.corext.refactoring.reorg.ReorgPolicyFactory;
import org.eclipse.jdt.ui.refactoring.RenameSupport;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.participants.MoveRefactoring;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;
import org.eclipse.ltk.internal.ui.refactoring.ChangePreviewViewerDescriptor;
import org.eclipse.ltk.internal.ui.refactoring.PreviewNode;
import org.eclipse.ltk.ui.refactoring.IChangePreviewViewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manager for all refactoring sessions. Handles creating caching and applying refactorings.
 *
 * @author Evgen Vidolob
 * @author Valeriy Svydenko
 */
@Singleton
public class RefactoringManager {
  private static final Logger LOG = LoggerFactory.getLogger(RefactoringManager.class);
  private static final AtomicInteger sessionId = new AtomicInteger(1);
  private final Cache<String, RefactoringSession> sessions;

  public RefactoringManager() {
    sessions =
        CacheBuilder.newBuilder()
            .expireAfterAccess(15, TimeUnit.MINUTES)
            .removalListener(
                new RemovalListener<String, RefactoringSession>() {
                  @Override
                  public void onRemoval(
                      RemovalNotification<String, RefactoringSession> notification) {
                    RefactoringSession value = notification.getValue();
                    if (value != null) {
                      value.dispose();
                    }
                  }
                })
            .build();
  }

  private static RenameSupport createRenameSupport(IJavaElement element, String newName, int flags)
      throws CoreException {
    switch (element.getElementType()) {
      case IJavaElement.PACKAGE_FRAGMENT:
        return RenameSupport.create((IPackageFragment) element, newName, flags);
      case IJavaElement.COMPILATION_UNIT:
        return RenameSupport.create((ICompilationUnit) element, newName, flags);
      case IJavaElement.TYPE:
        return RenameSupport.create((IType) element, newName, flags);
      case IJavaElement.METHOD:
        final IMethod method = (IMethod) element;
        if (method.isConstructor())
          return createRenameSupport(method.getDeclaringType(), newName, flags);
        else return RenameSupport.create((IMethod) element, newName, flags);
      case IJavaElement.FIELD:
        return RenameSupport.create((IField) element, newName, flags);
      case IJavaElement.TYPE_PARAMETER:
        return RenameSupport.create((ITypeParameter) element, newName, flags);
      case IJavaElement.LOCAL_VARIABLE:
        return RenameSupport.create((ILocalVariable) element, newName, flags);
    }
    return null;
  }

  /**
   * Create move refactoring session.
   *
   * @param javaElements the java elements
   * @return the ID of the refactoring session
   */
  public String createMoveRefactoringSession(IJavaElement[] javaElements)
      throws JavaModelException, RefactoringException {
    IReorgPolicy.IMovePolicy policy =
        ReorgPolicyFactory.createMovePolicy(new IResource[0], javaElements);
    if (policy.canEnable()) {
      JavaMoveProcessor processor = new JavaMoveProcessor(policy);
      // TODO this may overwrite existing sources.
      processor.setReorgQueries(new NullReorgQueries());
      processor.setCreateTargetQueries(() -> null);
      Refactoring refactoring = new MoveRefactoring(processor);
      MoveRefactoringSession session = new MoveRefactoringSession(refactoring, processor);
      final String id = String.format("move-%s", sessionId.getAndIncrement());
      sessions.put(id, session);
      return id;
    } else {
      throw new RefactoringException("Can't create move refactoring session.");
    }
  }

  /** Periodically cleanup cache, to avoid memory leak. */
  @ScheduleRate(initialDelay = 1, period = 1, unit = TimeUnit.HOURS)
  void cacheClenup() {
    sessions.cleanUp();
  }

  public RefactoringStatus setRefactoringDestination(ReorgDestination destination)
      throws RefactoringException, JavaModelException {
    RefactoringSession session = getRefactoringSession(destination.getSessionId());
    if (!(session instanceof ReorgRefactoringSession)) {
      throw new RefactoringException("Can't set destination on none reorg refactoring session.");
    }

    ReorgRefactoringSession rs = ((ReorgRefactoringSession) session);
    Object dest =
        getDestination(
            destination.getProjectPath(), destination.getType(), destination.getDestination());
    org.eclipse.ltk.core.refactoring.RefactoringStatus refactoringStatus =
        rs.verifyDestination(dest);

    return DtoConverter.toRefactoringStatusDto(refactoringStatus);
  }

  private RefactoringSession getRefactoringSession(String sessionId) throws RefactoringException {
    RefactoringSession session = sessions.getIfPresent(sessionId);
    if (session == null) {
      throw new RefactoringException("Can't find refactoring session.");
    }
    return session;
  }

  private Object getDestination(String projectPath, DestinationType type, String destination)
      throws RefactoringException, JavaModelException {
    IJavaProject javaProject =
        JavaModelManager.getJavaModelManager().getJavaModel().getJavaProject(projectPath);
    if (javaProject == null) {
      throw new RefactoringException("Can't find project: " + projectPath);
    }
    switch (type) {
      case PACKAGE:
        return javaProject.findPackageFragment(new Path(destination));

      case RESOURCE:
      case SOURCE_REFERENCE:
      default:
        throw new UnsupportedOperationException(
            "Can't use destination for 'RESOURCE' or 'SOURCE_REFERENCE'.");
    }
  }

  /**
   * Sets move refactoring settings. update references, update qualified names, files pattern
   *
   * @param settings the move refactoring settings
   * @throws RefactoringException when move refactoring session not found.
   */
  public void setMoveSettings(MoveSettings settings) throws RefactoringException {
    RefactoringSession session = getRefactoringSession(settings.getSessionId());
    if (!(session instanceof MoveRefactoringSession)) {
      throw new RefactoringException("Can't set move on none move refactoring session.");
    }

    MoveRefactoringSession refactoring = ((MoveRefactoringSession) session);
    refactoring.setUpdateReferences(settings.isUpdateReferences());
    if (settings.isUpdateQualifiedNames()) {
      refactoring.setFilePatterns(settings.getFilePatterns());
    }
    refactoring.setUpdateQualifiedNames(settings.isUpdateQualifiedNames());
  }

  /**
   * Get refactoring preview tree.
   *
   * @param sessionId id of the refactoring session
   * @return refactoring preview
   * @throws RefactoringException when refactoring session not found.
   */
  public RefactoringPreview getRefactoringPreview(String sessionId) throws RefactoringException {
    RefactoringSession session = getRefactoringSession(sessionId);
    PreviewNode node = session.getChangePreview();
    return DtoConverter.toRefactoringPreview(node);
  }

  /**
   * Create refactoring change and return status of creating changes.
   *
   * @param sessionId id of the refactoring session
   * @return change creations result
   * @throws RefactoringException when refactoring session not found.
   */
  public ChangeCreationResult createChange(String sessionId) throws RefactoringException {
    RefactoringSession session = getRefactoringSession(sessionId);
    return session.createChange();
  }

  /**
   * Apply refactoring.
   *
   * @param sessionId id of the refactoring session
   * @return refactoring result
   * @throws RefactoringException when refactoring session not found.
   */
  public RefactoringResult applyRefactoring(String sessionId) throws RefactoringException {
    RefactoringSession session = getRefactoringSession(sessionId);
    RefactoringResult result = session.apply();
    deleteRefactoringSession(sessionId);
    return result;
  }

  private void deleteRefactoringSession(String sessionId) {
    sessions.invalidate(sessionId);
  }

  /**
   * Create rename refactoring. It can create two rename refactoring types. First is linked mode
   * rename refactoring, second is classic rename refactoring with wizard.
   *
   * @param element element to rename
   * @param cu compilation unit which element belongs. null if element is IPackageFragment.
   * @param offset cursor position inside editor, used only for linked mode
   * @param lightweight if true try to create linked mode refactoring
   * @return rename refactoring session
   * @throws CoreException when impossible to create RenameSupport
   * @throws RefactoringException when we don't support renaming provided element
   */
  public RenameRefactoringSession createRenameRefactoring(
      IJavaElement element, ICompilationUnit cu, int offset, boolean lightweight)
      throws CoreException, RefactoringException {

    // package fragments are always renamed with wizard
    RenameRefactoringSession session = DtoFactory.newDto(RenameRefactoringSession.class);
    String id = String.format("rename-%s", sessionId.getAndIncrement());
    session.setSessionId(id);
    session.setOldName(element.getElementName());
    session.setWizardType(getWizardType(element));
    if (lightweight && !(element instanceof IPackageFragment)) {
      RenameLinkedModeRefactoringSession refactoringSession =
          new RenameLinkedModeRefactoringSession(element, cu, offset);
      LinkedModeModel model = refactoringSession.getModel();
      if (model != null) {
        session.setLinkedModeModel(model);
      }
      sessions.put(id, refactoringSession);
      return session;
    } else {
      RenameSupport renameSupport =
          createRenameSupport(element, null, RenameSupport.UPDATE_REFERENCES);
      if (renameSupport != null && renameSupport.preCheck().isOK()) {
        RenameRefactoring refactoring = renameSupport.getfRefactoring();
        RenameSession renameSession = new RenameSession(refactoring);
        sessions.put(id, renameSession);
        return session;
      }
      throw new RefactoringException(
          "Can't create refactoring session for element: " + element.getElementName());
    }
  }

  private RenameWizard getWizardType(IJavaElement element) throws JavaModelException {
    switch (element.getElementType()) {
      case IJavaElement.PACKAGE_FRAGMENT:
        return RenameWizard.PACKAGE;
      case IJavaElement.COMPILATION_UNIT:
        return RenameWizard.COMPILATION_UNIT;
      case IJavaElement.TYPE:
        return RenameWizard.TYPE;
      case IJavaElement.METHOD:
        final IMethod method = (IMethod) element;
        if (method.isConstructor()) return RenameWizard.TYPE;
        else return RenameWizard.METHOD;
      case IJavaElement.FIELD:
        if (JdtFlags.isEnum((IMember) element)) {
          return RenameWizard.ENUM_CONSTANT;
        }
        return RenameWizard.FIELD;
      case IJavaElement.TYPE_PARAMETER:
        return RenameWizard.TYPE_PARAMETER;
      case IJavaElement.LOCAL_VARIABLE:
        return RenameWizard.LOCAL_VARIABLE;
    }
    return null;
  }

  /**
   * Apply linked mode rename refactoring.
   *
   * @param apply contains new element name
   * @return refactoring result
   * @throws RefactoringException when refactoring session not found.
   * @throws CoreException when impossible to apply rename refactoring
   */
  public RefactoringResult applyLinkedRename(LinkedRenameRefactoringApply apply)
      throws RefactoringException, CoreException {
    RefactoringSession session = getRefactoringSession(apply.getSessionId());
    if (session instanceof RenameLinkedModeRefactoringSession) {
      RenameLinkedModeRefactoringSession renameSession =
          (RenameLinkedModeRefactoringSession) session;
      try {
        RefactoringResult refactoringResult = renameSession.doRename(apply.getNewName());
        deleteRefactoringSession(apply.getSessionId());
        return refactoringResult;
      } catch (InvocationTargetException | InterruptedException | AssertionFailedException e) {
        LOG.error(e.getMessage(), e);
        return DtoConverter.toRefactoringResultDto(
            org.eclipse.ltk.core.refactoring.RefactoringStatus.createFatalErrorStatus(
                e.getMessage()));
      }
    }

    throw new RefactoringException("There is no RenameLinkedModeRefactoringSession.");
  }

  /**
   * Set rename refactoring wizard setting. This settings common for all 8 rename wizards.
   *
   * @param settings refactoring wizard settings
   * @throws RefactoringException when refactoring session not found or corresponding session is not
   *     RenameSession.
   */
  public void setRenameSettings(RenameSettings settings) throws RefactoringException {
    RefactoringSession session = getRefactoringSession(settings.getSessionId());
    if (session instanceof RenameSession) {
      ((RenameSession) session).setSettings(settings);
    } else {
      throw new RefactoringException("Rename settings may be applied only to RenameSession");
    }
  }

  /**
   * Validate new name for rename refactoring. Uses from wizard.
   *
   * @param newName the new element name
   * @return validation status
   * @throws RefactoringException when corresponding session is not RenameSession.
   */
  public RefactoringStatus renameValidateNewName(ValidateNewName newName)
      throws RefactoringException {
    RefactoringSession session = getRefactoringSession(newName.getSessionId());
    if (session instanceof RenameSession) {
      return DtoConverter.toRefactoringStatusDto(
          ((RenameSession) session).validateNewName(newName.getNewName()));
    } else {
      throw new RefactoringException("Validating of new name only available on RenameSession.");
    }
  }

  /**
   * Include/exclude refactoring change from refactoring
   *
   * @param state updating state
   * @throws RefactoringException when refactoring session not found.
   */
  public void changeChangeEnabled(ChangeEnabledState state) throws RefactoringException {
    RefactoringSession session = getRefactoringSession(state.getSessionId());
    session.updateChangeEnabled(state.getChangeId(), state.isEnabled());
  }

  /**
   * generate preview for refactoring change
   *
   * @param change the refactoring change
   * @return refactoring change preview
   * @throws RefactoringException when refactoring session or change not found.
   */
  public ChangePreview getChangePreview(RefactoringChange change) throws RefactoringException {
    RefactoringSession session = getRefactoringSession(change.getSessionId());
    PreviewNode previewNode = session.getChangePreview(change.getChangeId());
    try {
      ChangePreviewViewerDescriptor descriptor = previewNode.getChangePreviewViewerDescriptor();
      if (descriptor != null) {
        IChangePreviewViewer viewer = descriptor.createViewer();
        if (viewer != null) {
          return previewNode.feedInput(viewer, Collections.EMPTY_LIST);
        }
      }
    } catch (CoreException e) {
      throw new RefactoringException(e.getMessage());
    }
    return null;
  }

  /**
   * Make reindex for the project.
   *
   * @param javaProject java project
   * @throws JavaModelException when something is wrong
   */
  public void reindexProject(IJavaProject javaProject) throws JavaModelException {
    if (javaProject != null) {
      JavaModelManager.getIndexManager().indexAll(javaProject.getProject());
    }
  }
}
