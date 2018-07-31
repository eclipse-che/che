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
package test36;

import org.eclipse.osgi.util.NLS;
public class A extends NLS {
    private static final String BUNDLE_NAME = "test36.messages"; //$NON-NLS-1$
    public static String g;
    static {
        NLS.initializeMessages(BUNDLE_NAME, A.class);
    }
    private A() {}
}
