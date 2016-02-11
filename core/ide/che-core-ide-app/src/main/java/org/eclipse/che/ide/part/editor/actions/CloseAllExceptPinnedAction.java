/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.part.editor.actions;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.analytics.client.logger.AnalyticsEventLogger;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.part.editor.event.CloseNonPinnedEditorsEvent;

/**
 * Performs closing editor tabs with unpinned status.
 *
 * @author Vlad Zhukovskiy
 */
@Singleton
public class CloseAllExceptPinnedAction extends EditorAbstractAction {

    @Inject
    public CloseAllExceptPinnedAction(EditorAgent editorAgent,
                                      EventBus eventBus,
                                      CoreLocalizationConstant locale,
                                      AnalyticsEventLogger eventLogger) {
        super(locale.editorTabCloseAllButPinned(), locale.editorTabCloseAllButPinnedDescription(), null, editorAgent, eventBus,
              eventLogger);
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        eventLogger.log(this);
        eventBus.fireEvent(new CloseNonPinnedEditorsEvent());
    }
}
