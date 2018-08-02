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
package org.eclipse.che.ide.part.widgets;

import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.parts.EditorPartStack;
import org.eclipse.che.ide.api.parts.EditorTab;
import org.eclipse.che.ide.part.widgets.partbutton.PartButton;

/**
 * @author Dmitry Shnurenko
 * @author Vlad Zhukovskyi
 */
public interface TabItemFactory {

  PartButton createPartButton(@NotNull String title);

  EditorTab createEditorPartButton(
      @NotNull EditorPartPresenter relatedEditorPart, @NotNull EditorPartStack editorPartStack);
}
