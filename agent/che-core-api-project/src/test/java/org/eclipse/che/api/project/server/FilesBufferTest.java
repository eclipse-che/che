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
package org.eclipse.che.api.project.server;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

/**
 * @author Dmitry Shnurenko
 */
public class FilesBufferTest {

    private FilesBuffer buffer;

    @Before
    public void setUp() {
        buffer = FilesBuffer.get();
    }

    @Test
    public void bufferShouldBeSingleton() {
        FilesBuffer test = FilesBuffer.get();
        FilesBuffer test1 = FilesBuffer.get();

        assertThat(test, is(sameInstance(test1)));
    }

    @Test
    public void somePathShouldBeAddToBuffer() {
        String testPath = "path/";

        assertThat(buffer.isContainsPath(testPath), is(false));

        buffer.addToBuffer(testPath);

        assertThat(buffer.isContainsPath(testPath), is(true));
    }

    @Test
    public void pathShouldBeValidated() {
        String wrongPath = "/path";
        String rightPath = "path";

        buffer.addToBuffer(wrongPath);

        assertThat(buffer.isContainsPath(rightPath), is(true));
    }
}