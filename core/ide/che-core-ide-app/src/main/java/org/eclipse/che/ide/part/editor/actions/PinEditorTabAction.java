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
import org.eclipse.che.ide.part.editor.event.PinEditorTabEvent;

/**
 * Pin/Unpin current selected editor tab.
 *
 * @author Vlad Zhukovskiy
 */
@Singleton
public class PinEditorTabAction extends EditorAbstractAction {

    public static final String PROP_PIN = "pin";

    @Inject
    public PinEditorTabAction(EditorAgent editorAgent,
                              EventBus eventBus,
                              CoreLocalizationConstant locale,
                              AnalyticsEventLogger eventLogger) {
        super(locale.editorTabPin(), locale.editorTabPinDescription(), null, editorAgent, eventBus, eventLogger);
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        eventLogger.log(this);
        eventBus.fireEvent(new PinEditorTabEvent(getEditorFile(e), !isPinned(e)));
    }

    private boolean isPinned(ActionEvent e) {
        Object o = e.getPresentation().getClientProperty(PROP_PIN);

        return o instanceof Boolean && (Boolean)o;
    }
}
