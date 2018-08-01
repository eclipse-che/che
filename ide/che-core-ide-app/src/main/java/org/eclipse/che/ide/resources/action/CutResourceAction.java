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
package org.eclipse.che.ide.resources.action;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.part.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.parts.ActivePartChangedEvent;
import org.eclipse.che.ide.api.parts.ActivePartChangedHandler;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.resources.modification.ClipboardManager;
import org.eclipse.che.ide.api.selection.Selection;

/**
 * Cut resources action. Move selected resources from the application context into clipboard
 * manager.
 *
 * @author Vlad Zhukovskiy
 * @see ClipboardManager
 * @see ClipboardManager#getCutProvider()
 * @since 4.4.0
 */
@Singleton
public class CutResourceAction extends AbstractPerspectiveAction {

  private final ClipboardManager clipboardManager;
  private final AppContext appContext;
  private PartPresenter partPresenter;

  @Inject
  public CutResourceAction(
      CoreLocalizationConstant localization,
      Resources resources,
      ClipboardManager clipboardManager,
      AppContext appContext,
      EventBus eventBus) {
    super(
        singletonList(PROJECT_PERSPECTIVE_ID),
        localization.cutItemsActionText(),
        localization.cutItemsActionDescription(),
        resources.cut());
    this.clipboardManager = clipboardManager;
    this.appContext = appContext;

    eventBus.addHandler(
        ActivePartChangedEvent.TYPE,
        new ActivePartChangedHandler() {
          @Override
          public void onActivePartChanged(ActivePartChangedEvent event) {
            partPresenter = event.getActivePart();
          }
        });
  }

  /** {@inheritDoc} */
  @Override
  public void updateInPerspective(@NotNull ActionEvent event) {
    event.getPresentation().setVisible(true);
    event
        .getPresentation()
        .setEnabled(
            clipboardManager.getCutProvider().isCutEnable(appContext)
                && !(partPresenter instanceof TextEditor)
                && !(partPresenter.getSelection() instanceof Selection.NoSelectionProvided));
  }

  /** {@inheritDoc} */
  @Override
  public void actionPerformed(ActionEvent e) {
    checkState(clipboardManager.getCutProvider().isCutEnable(appContext), "Cut is not enabled");

    clipboardManager.getCutProvider().performCut(appContext);
  }
}
