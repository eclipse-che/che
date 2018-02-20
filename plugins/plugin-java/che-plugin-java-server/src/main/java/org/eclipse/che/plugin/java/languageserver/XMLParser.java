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
package org.eclipse.che.plugin.java.languageserver;

import java.util.HashMap;
import java.util.Map;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/** @author Roman Nikitenko */
public class XMLParser extends DefaultHandler {

  private Map<String, String> settings;

  public Map<String, String> getSettings() {
    return settings;
  }

  @Override
  public void startDocument() throws SAXException {
    settings = new HashMap<>();
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes)
      throws SAXException {
    if (qName.equals("setting")) {
      String id = attributes.getValue("id");
      String value = attributes.getValue("value");
      settings.put(id, value);
    }
  }
}
