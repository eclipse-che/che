/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.projecttype;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

/** @author Artem Zatsarynnyi */
@RunWith(MockitoJUnitRunner.class)
public class BlankProjectWizardRegistrarTest {

  @InjectMocks private BlankProjectWizardRegistrar wizardRegistrar;

  @Test
  public void shouldReturnCorrectProjectTypeId() throws Exception {
    assertThat(wizardRegistrar.getProjectTypeId(), equalTo(BlankProjectWizardRegistrar.BLANK_ID));
  }

  @Test
  public void shouldReturnCorrectCategory() throws Exception {
    assertThat(wizardRegistrar.getCategory(), equalTo(BlankProjectWizardRegistrar.BLANK_CATEGORY));
  }

  @Test
  public void shouldNotReturnAnyPages() throws Exception {
    assertTrue(wizardRegistrar.getWizardPages().isEmpty());
  }
}
