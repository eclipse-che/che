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
package org.eclipse.che.ide.ext.java.client.action;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.analytics.client.logger.AnalyticsEventLogger;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.ide.ext.java.client.editor.OpenDeclarationFinder;

/**
 * Invoke open declaration action ofr java element
 *
 * @author Evgen Vidolob
 */
@Singleton
public class OpenDeclarationAction extends JavaEditorAction {

    private OpenDeclarationFinder declarationFinder;

    private final AnalyticsEventLogger eventLogger;

    @Inject
    public OpenDeclarationAction(JavaLocalizationConstant constant,
                                 EditorAgent editorAgent,
                                 OpenDeclarationFinder declarationFinder,
                                 AnalyticsEventLogger eventLogger,
                                 JavaResources resources,
                                 FileTypeRegistry fileTypeRegistry) {
        super(constant.actionOpenDeclarationTitle(),
              constant.actionOpenDeclarationDescription(),
              resources.openDeclaration(),
              editorAgent,
              fileTypeRegistry);
        this.declarationFinder = declarationFinder;
        this.eventLogger = eventLogger;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        eventLogger.log(this);
        declarationFinder.openDeclaration();
    }
}
