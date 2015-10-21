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
package example;

import org.eclipse.che.ide.theme.DarkTheme;

/**
 * @author Evgen Vidolob
 */
public class MyTheme extends DarkTheme {

    @Override
    public String getId() {
        return "My Theme id";
    }

    @Override
    public String getDescription() {
        return "My new Che Theme";
    }

    @Override
    public String getMainFontColor() {
        return "red";
    }

    @Override
    public String getPartBackground() {
        return "white";
    }

    @Override
    public String getTabsPanelBackground() {
        return "white";
    }
}
