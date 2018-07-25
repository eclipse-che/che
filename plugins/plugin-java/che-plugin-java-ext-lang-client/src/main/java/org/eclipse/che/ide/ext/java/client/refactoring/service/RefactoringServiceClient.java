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
package org.eclipse.che.ide.ext.java.client.refactoring.service;

import com.google.inject.ImplementedBy;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ChangeCreationResult;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ChangeEnabledState;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ChangePreview;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.CreateMoveRefactoring;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.CreateRenameRefactoring;
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

/**
 * Provides methods which allow send requests to special refactoring service to do refactoring.
 *
 * @author Dmitry Shnurenko
 * @author Valeriy Svydenko
 */
@ImplementedBy(RefactoringServiceClientImpl.class)
public interface RefactoringServiceClient {

  /**
   * Creates move refactoring and returns special refactoring session id which will be need to
   * continue setup refactoring steps.
   *
   * @param moveRefactoring special object which contains information about items which will be
   *     refactored
   * @return an instance of refactoring session id
   */
  Promise<String> createMoveRefactoring(CreateMoveRefactoring moveRefactoring);

  /**
   * Creates rename refactoring session.
   *
   * @param settings rename settings
   * @return an instance of refactoring session id
   */
  Promise<RenameRefactoringSession> createRenameRefactoring(CreateRenameRefactoring settings);

  /**
   * Apply linked mode rename refactoring.
   *
   * @param refactoringApply linked mode setting and refactoring session id
   * @return an instance of refactoring result
   */
  Promise<RefactoringResult> applyLinkedModeRename(LinkedRenameRefactoringApply refactoringApply);

  /**
   * Sets destination for reorg refactorings.
   *
   * @param destination the destination for reorg refactorings
   * @return status of refactoring operation
   */
  Promise<RefactoringStatus> setDestination(ReorgDestination destination);

  /**
   * Set move refactoring wizard setting.
   *
   * @param settings the move settings
   * @return empty promise result
   */
  Promise<Void> setMoveSettings(MoveSettings settings);

  /**
   * Create refactoring change. Creation of the change starts final checking for refactoring.
   * Without creating change refactoring can't be applied.
   *
   * @param session the refactoring session.
   * @return result of creation of the change.
   */
  Promise<ChangeCreationResult> createChange(RefactoringSession session);

  /**
   * Get refactoring preview. Preview is tree of refactoring changes.
   *
   * @param session the refactoring session.
   * @return refactoring preview tree
   */
  Promise<RefactoringPreview> getRefactoringPreview(RefactoringSession session);

  /**
   * Applies refactoring.
   *
   * @param session the refactoring session
   * @return the result for applied refactoring
   */
  Promise<RefactoringResult> applyRefactoring(RefactoringSession session);

  /**
   * Change enabled/disabled state of the corresponding refactoring change.
   *
   * @param state the state of refactoring change
   * @return empty promise result
   */
  Promise<Void> changeChangeEnabledState(ChangeEnabledState state);

  /**
   * Get refactoring change preview. Preview contains new and old content of the file.
   *
   * @param change the change to get preview
   * @return refactoring change preview
   */
  Promise<ChangePreview> getChangePreview(RefactoringChange change);

  /**
   * Validates new name for the rename operation.
   *
   * @param newName new name that should be validated
   * @return the status for the name validated
   */
  Promise<RefactoringStatus> validateNewName(ValidateNewName newName);

  /**
   * Set rename refactoring wizard setting.
   *
   * @param settings the rename settings
   * @return empty promise result
   */
  Promise<Void> setRenameSettings(RenameSettings settings);

  /**
   * Make reindex for the project.
   *
   * @param projectPath path to the project
   * @return empty promise result
   */
  Promise<Void> reindexProject(String projectPath);
}
