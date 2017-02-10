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
package org.eclipse.che.ide.ext.java.client.resource;

import com.google.common.base.Optional;
import com.google.gwtmockito.GwtMockitoTestRunner;

import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Resource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * @author Valeriy Svydenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class JavaSourceRenameValidatorTest {
    @InjectMocks
    private JavaSourceRenameValidator validator;

    @Mock
    private Resource resource;
    @Mock
    private File file;

    @Test
    public void renameShouldBeAllowedIfResourceDoesNotHaveSourceParentFolder() throws Exception {
        when(resource.getParentWithMarker(SourceFolderMarker.ID)).thenReturn(Optional.absent());

        assertTrue(validator.isRenameAllowed(resource));
    }

    @Test
    public void renameShouldBeDisabledIfResourceIsFolderAndHasSourceParentFolder() throws Exception {
        when(resource.getParentWithMarker(SourceFolderMarker.ID)).thenReturn(Optional.of(resource));
        when(resource.isFolder()).thenReturn(true);

        assertFalse(validator.isRenameAllowed(resource));
    }

    @Test
    public void renameShouldBeDisabledIfResourceIsJavaClassAndHasSourceParentFolder() throws Exception {
        when(file.getParentWithMarker(SourceFolderMarker.ID)).thenReturn(Optional.of(resource));
        when(file.isFile()).thenReturn(true);

        when(file.getExtension()).thenReturn("java");

        assertFalse(validator.isRenameAllowed(file));
    }

    @Test
    public void renameShouldBeEnabledIfResourceIsNotJavaClassAndHasSourceParentFolder() throws Exception {
        when(file.getParentWithMarker(SourceFolderMarker.ID)).thenReturn(Optional.of(resource));
        when(file.isFile()).thenReturn(true);

        when(file.getExtension()).thenReturn("txt");

        assertTrue(validator.isRenameAllowed(file));
    }
}
