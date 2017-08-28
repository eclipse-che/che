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
package org.eclipse.che.datasource.ide.newDatasource;


import org.eclipse.che.datasource.shared.DatabaseConfigurationDTO;

import javax.validation.constraints.NotNull;

public interface NewDatasourceWizardFactory {
    @NotNull
    NewDatasourceWizard create(DatabaseConfigurationDTO dataObject);
}
