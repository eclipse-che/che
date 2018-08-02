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
package org.eclipse.che.ide.ext.java.client.refactoring;

import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.ext.java.client.refactoring.move.MoveType;
import org.eclipse.che.ide.ext.java.client.refactoring.move.RefactoredItemType;

/**
 * @author Dmitry Shnurenko
 * @author Valeriy Svydenko
 * @author Vlad Zhukovskyi
 */
public class RefactorInfo {

  private final MoveType moveType;
  private final RefactoredItemType refactoredItemType;
  private final Resource[] resources;

  public static RefactorInfo of(
      MoveType moveType, RefactoredItemType refactoredItemType, Resource[] resources) {
    return new RefactorInfo(moveType, refactoredItemType, resources);
  }

  public static RefactorInfo of(RefactoredItemType refactoredItemType, Resource[] resources) {
    return new RefactorInfo(refactoredItemType, resources);
  }

  private RefactorInfo(
      MoveType moveType, RefactoredItemType refactoredItemType, Resource[] resources) {
    this.moveType = moveType;
    this.refactoredItemType = refactoredItemType;
    this.resources = resources;
  }

  private RefactorInfo(RefactoredItemType refactoredItemType, Resource[] resources) {
    moveType = null;
    this.refactoredItemType = refactoredItemType;
    this.resources = resources;
  }

  public Resource[] getResources() {
    return resources;
  }

  public MoveType getMoveType() {
    return moveType;
  }

  public RefactoredItemType getRefactoredItemType() {
    return refactoredItemType;
  }
}
