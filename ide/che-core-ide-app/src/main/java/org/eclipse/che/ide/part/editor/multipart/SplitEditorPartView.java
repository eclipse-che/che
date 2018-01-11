/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.part.editor.multipart;

import com.google.common.collect.BiMap;
import com.google.gwt.user.client.ui.IsWidget;
import org.eclipse.che.ide.api.constraints.Direction;
import org.eclipse.che.ide.api.parts.EditorMultiPartStackState;
import org.eclipse.che.ide.api.parts.EditorPartStack;

/**
 * View representation of editor to displaying in {@link EditorMultiPartStackView}.
 *
 * <p>Provides ability to split view corresponding to {@link Direction} on two areas and display
 * editor horizontally or vertically. Current view can be in two states:
 * <li>- split state
 * <li>- not split state In the split state view has two areas:
 * <li>- "specimen" area
 * <li>- "replica" area Otherwise view has only "specimen" area.
 *
 * @author Roman nikitenko
 */
public interface SplitEditorPartView extends IsWidget {

  /**
   * Split the view corresponding to {@code direction} on two areas and adds {@code replica} into
   * the created area.
   *
   * @param replica will be added into the created area
   * @param direction contains info about way how {@code replica} should be displayed
   * @param size
   */
  void split(IsWidget replica, Direction direction, double size);

  /** Returns view of the "Specimen" area. */
  SplitEditorPartView getSpecimen();

  /** Returns view of the "Replica" area. */
  SplitEditorPartView getReplica();

  /**
   * Removes given {@code child} from view. It allows to remove "Specimen" as well as "Replica".
   *
   * @param child child to remove
   */
  void removeChild(SplitEditorPartView child);

  /** Removes this view from its parent widget */
  void removeFromParent();

  /**
   * Get editor multi part stack state
   *
   * @param splitEditorParts split editor part view mapped to their part stack
   * @return the editor multi part stack state
   */
  EditorMultiPartStackState getState(BiMap<SplitEditorPartView, EditorPartStack> splitEditorParts);
}
