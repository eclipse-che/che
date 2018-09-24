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
package org.eclipse.che.commons.xml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * The handler is designed for using at xml document parsing and logs messages at the DEBUG level
 * only.
 *
 * @author Roman Nikitenko
 */
public class QuietXmlErrorHandler implements ErrorHandler {
  private static final Logger LOG = LoggerFactory.getLogger(QuietXmlErrorHandler.class);

  @Override
  public void warning(SAXParseException exception) throws SAXException {
    LOG.debug("Warning at parsing xml document: " + exception.getLocalizedMessage(), exception);
  }

  @Override
  public void error(SAXParseException exception) throws SAXException {
    LOG.debug("Error at parsing xml document: " + exception.getLocalizedMessage(), exception);
  }

  @Override
  public void fatalError(SAXParseException exception) throws SAXException {
    LOG.debug("Fatal error at parsing xml document: " + exception.getLocalizedMessage(), exception);
    throw exception;
  }
}
