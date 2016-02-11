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
package org.eclipse.che.ide.ext.java.client.document;

import org.eclipse.che.ide.ext.java.jdt.text.AbstractDocument;
import org.eclipse.che.ide.ext.java.jdt.text.ConfigurableLineTracker;
import org.eclipse.che.ide.jseditor.client.document.Document;

/**
 * This class realises adapter for manipulation object with type
 * Document
 *
 * @author Andrienko Alexander
 */
public class FormatterDocument extends AbstractDocument {

    private static final String[] delimeters = {"\n"};

    /**
     * Creates a new document with Document inside
     * for realization adapter
     *
     * @param document document for apply changes
     */
    public FormatterDocument(Document document) {
        super();
        ConfigurableLineTracker lineTracker = new ConfigurableLineTracker(delimeters);
        setTextStore(new FormatterStore(document));
        setLineTracker(lineTracker);
        getTracker().set(document.getContents());
        completeInitialization();
    }
}
