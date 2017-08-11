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
package org.eclipse.che.commons.lang;

import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertTrue;

/**
 * @author Anatolii Bazko
 */
public class IoUtilTest {
    @Test
    public void shouldListResources() throws Exception {
        List<String> resources = new ArrayList<>();
        IoUtil.listResources(getClass().getResource("/").toURI(), path -> resources.add(path.getFileName().toString()));

        assertTrue(resources.contains("logback-test.xml"));
        assertTrue(resources.contains("findbugs-exclude.xml"));
    }
}
