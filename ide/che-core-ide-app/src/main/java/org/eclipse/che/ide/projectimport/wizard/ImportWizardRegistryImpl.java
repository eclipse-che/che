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

import org.eclipse.che.ide.api.project.wizard.ImportWizardRegistrar;
import org.eclipse.che.ide.api.project.wizard.ImportWizardRegistry;
import org.eclipse.che.ide.util.loging.Log;
import com.google.inject.Inject;

import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Implementation for {@link ImportWizardRegistry}.
 *
 * @author Artem Zatsarynnyi
 */
public class ImportWizardRegistryImpl implements ImportWizardRegistry {
    private final Map<String, ImportWizardRegistrar> registrars;

    public ImportWizardRegistryImpl() {
        registrars = new HashMap<>();
    }

    @Inject(optional = true)
    private void register(Set<ImportWizardRegistrar> registrars) {
        for (ImportWizardRegistrar registrar : registrars) {
            final String id = registrar.getImporterId();
            if (this.registrars.containsKey(id)) {
                Log.warn(ImportWizardRegistryImpl.class, "Wizard for project importer " + id + " already registered.");
            } else {
                this.registrars.put(id, registrar);
            }
        }
    }

    @Nullable
    @Override
    public ImportWizardRegistrar getWizardRegistrar(@NotNull String importerId) {
        return registrars.get(importerId);
    }
}
