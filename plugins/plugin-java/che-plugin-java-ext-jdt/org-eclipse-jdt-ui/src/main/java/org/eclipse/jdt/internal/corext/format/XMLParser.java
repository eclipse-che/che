/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.corext.format;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Roman Nikitenko
 */
public class XMLParser extends DefaultHandler {

    private Map<String, String> settings;

    public Map<String, String> getSettings() {
        return settings;
    }

    @Override
    public void startDocument()
            throws SAXException {
        settings = new HashMap<>();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (qName.equals("setting")) {
            String id = attributes.getValue("id");
            String value = attributes.getValue("value");
            settings.put(id, value);
        }
    }
}
