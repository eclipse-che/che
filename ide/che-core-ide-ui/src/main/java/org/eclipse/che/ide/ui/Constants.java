/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ui;

import org.eclipse.che.ide.util.browser.UserAgent;

/** Constants that we can use in CssResource expressions. */
public final class Constants {
    public static final int SCROLLBAR_SIZE = UserAgent.isFirefox() ? 7 : 5;

    /** A timer delay for actions that happen after a "hover" period. */
    public static final int MOUSE_HOVER_DELAY = 600;

    private Constants() {
    } // COV_NF_LINE
}
