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
package org.eclipse.che.ide.projectimport.wizard;

import org.eclipse.che.ide.api.project.MutableProjectConfig;
import org.eclipse.che.ide.api.wizard.Wizard.CompleteCallback;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

/**
 * @author Artem Zatsarynnyi
 * @author Dmitry Shnurenko
 */
@RunWith(MockitoJUnitRunner.class)
public class ImportWizardTest {

    @Mock
    private ProjectImporter      importer;
    @Mock
    private MutableProjectConfig projectConfig;
    @Mock
    private CompleteCallback     completeCallback;

    @InjectMocks
    private ImportWizard wizard;

    @Test
    public void shouldCallImporterOnCompletion() {
        wizard.complete(completeCallback);

        verify(importer).importProject(completeCallback, projectConfig);
    }
}
