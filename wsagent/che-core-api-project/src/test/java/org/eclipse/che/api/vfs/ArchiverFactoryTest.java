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
package org.eclipse.che.api.vfs;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

public class ArchiverFactoryTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private ArchiverFactory archiverFactory;

    @Before
    public void setUp() {
        archiverFactory = new ArchiverFactory();
    }

    @Test
    public void createsZipArchiver() {
        VirtualFile folder = mock(VirtualFile.class);
        assertNotNull(archiverFactory.createArchiver(folder, "zip"));
    }

    @Test
    public void createsTarArchiver() {
        VirtualFile folder = mock(VirtualFile.class);
        assertNotNull(archiverFactory.createArchiver(folder, "tar"));
    }

    @Test
    public void archiverTypeArgumentIsCaseInsensitive() {
        VirtualFile folder = mock(VirtualFile.class);
        assertNotNull(archiverFactory.createArchiver(folder, "tAr"));
    }

    @Test
    public void doesNotAcceptNullArchiverType() {
        VirtualFile folder = mock(VirtualFile.class);
        thrown.expect(IllegalArgumentException.class);
        archiverFactory.createArchiver(folder, null);
    }

    @Test
    public void failsCreateArchiverOfUnknownType() {
        VirtualFile folder = mock(VirtualFile.class);
        thrown.expect(IllegalArgumentException.class);
        archiverFactory.createArchiver(folder, "xxx");
    }
}