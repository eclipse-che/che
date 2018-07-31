/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.requirejs;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * A JS overlay over requirejs modules instances.<br>
 * As this really is any object created by the modules, it's only here to provide semantic : "this
 * is a module loaded by requirejs".
 *
 * @author "Mickaël Leduque"
 */
public class RequirejsModule extends JavaScriptObject {

  protected RequirejsModule() {}
}
