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
package org.eclipse.che.ide.resources.action;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.event.ActivePartChangedEvent;
import org.eclipse.che.ide.api.event.ActivePartChangedHandler;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.resources.modification.ClipboardManager;
import org.eclipse.che.ide.api.selection.Selection;

import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * Cut resources action.
 * Move selected resources from the application context into clipboard manager.
 *
 * @author Vlad Zhukovskiy
 * @see ClipboardManager
 * @see ClipboardManager#getCutProvider()
 * @since 4.4.0
 */
@Singleton
public class CutResourceAction extends AbstractPerspectiveAction {

    private final ClipboardManager clipboardManager;
    private final AppContext       appContext;
    private       PartPresenter    partPresenter;

    @Inject
    public CutResourceAction(CoreLocalizationConstant localization,
                             Resources resources,
                             ClipboardManager clipboardManager,
                             AppContext appContext,
                             EventBus eventBus) {
        super(singletonList(PROJECT_PERSPECTIVE_ID),
              localization.cutItemsActionText(),
              localization.cutItemsActionDescription(),
              null,
              resources.cut());
        this.clipboardManager = clipboardManager;
        this.appContext = appContext;

        eventBus.addHandler(ActivePartChangedEvent.TYPE, new ActivePartChangedHandler() {
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
        event.getPresentation().setEnabled(clipboardManager.getCutProvider().isCutEnable(appContext)
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
