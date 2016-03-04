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
package org.eclipse.che.ide.ext.java.client.reference;

import org.eclipse.che.ide.ext.java.client.project.node.JavaFileNode;
import org.eclipse.che.ide.ext.java.client.project.node.PackageNode;
import org.eclipse.che.ide.ext.java.client.project.node.jar.ExternalLibrariesNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(MockitoJUnitRunner.class)
public class JavaFqnProviderTest {

    @Mock
    private PackageNode           packageNode;
    @Mock
    private JavaFileNode          fileNode;
    @Mock
    private ExternalLibrariesNode externalLibrariesNode;

    @InjectMocks
    private JavaFqnProvider provider;

    @Test
    public void fqnShouldBeReturnedFromJavaPackageNode() {
        provider.getFqn(packageNode);

        verify(packageNode).getPackage();
        verify(fileNode, never()).getFqn();
    }

    @Test
    public void fqnShouldBeReturnedFromJavaFileNode() {
        provider.getFqn(fileNode);

        verify(fileNode).getFqn();
        verify(packageNode, never()).getPackage();
    }

    @Test
    public void emptyStringShouldBeReturnedForNodeWhichDoesNotContainFqn() {
        String fqn = provider.getFqn(externalLibrariesNode);

        verify(fileNode, never()).getFqn();
        verify(packageNode, never()).getPackage();

        assertTrue(fqn.isEmpty());
    }

}