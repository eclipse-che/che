/*******************************************************************************
 * Copyright (c) 2017 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.che.plugin.languageserver.ide.editor.quickassist;

import org.eclipse.che.ide.api.editor.EditorPartPresenter;

/**
 * @author Thomas Mäder
 */
public interface LanguageServerQuickAssistProcessorFactory {
    LanguageServerQuickAssistProcessor create(EditorPartPresenter editor);
}
