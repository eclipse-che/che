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

import com.google.gwt.user.client.ui.IsWidget;

/**
 * The factory creates instances of {@link SplitEditorPartView} to display view representation of
 * editors in {@link EditorMultiPartStackView}.
 *
 * @author Roman Nikitenko
 */
public interface SplitEditorPartViewFactory {

  /**
   * Creates implementation of {@link SplitEditorPartView}.
   *
   * @param specimen widget which is regarded as a specimen when splitting
   * @return an instance of {@link SplitEditorPartView}
   */
  SplitEditorPartView create(IsWidget specimen);
}
