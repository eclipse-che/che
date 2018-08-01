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
package org.eclipse.che.ide.ext.java.client.resource;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import com.google.common.base.Optional;
import com.google.gwtmockito.GwtMockitoTestRunner;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Resource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

/** @author Valeriy Svydenko */
@RunWith(GwtMockitoTestRunner.class)
public class JavaSourceRenameValidatorTest {
  @InjectMocks private JavaSourceRenameValidator validator;

  @Mock private Resource resource;
  @Mock private File file;

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
  public void renameShouldBeDisabledIfResourceIsJavaClassAndHasSourceParentFolder()
      throws Exception {
    when(file.getParentWithMarker(SourceFolderMarker.ID)).thenReturn(Optional.of(resource));
    when(file.isFile()).thenReturn(true);

    when(file.getExtension()).thenReturn("java");

    assertFalse(validator.isRenameAllowed(file));
  }

  @Test
  public void renameShouldBeEnabledIfResourceIsNotJavaClassAndHasSourceParentFolder()
      throws Exception {
    when(file.getParentWithMarker(SourceFolderMarker.ID)).thenReturn(Optional.of(resource));
    when(file.isFile()).thenReturn(true);

    when(file.getExtension()).thenReturn("txt");

    assertTrue(validator.isRenameAllowed(file));
  }
}
