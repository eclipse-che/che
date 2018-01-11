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
