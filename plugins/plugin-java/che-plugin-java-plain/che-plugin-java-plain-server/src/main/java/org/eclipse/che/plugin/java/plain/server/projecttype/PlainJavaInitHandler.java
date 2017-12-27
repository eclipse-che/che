/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.java.plain.server.projecttype;

import static org.eclipse.che.ide.ext.java.shared.Constants.JAVAC;

import com.google.inject.Inject;
import org.eclipse.che.plugin.java.server.projecttype.AbstractJavaInitHandler;

/**
 * Init handler for simple java project.
 *
 * @author Evgen Vidolob
 * @author Valeriy Svydenko
 */
public class PlainJavaInitHandler extends AbstractJavaInitHandler {

  @Inject
  public PlainJavaInitHandler() {}

  @Override
  public String getProjectType() {
    return JAVAC;
  }
}
