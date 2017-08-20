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
package org.eclipse.che.datasource.client.store;

import com.google.inject.Inject;
import com.google.inject.Singleton;


import org.eclipse.che.datasource.shared.DatabaseConfigurationDTO;
import org.eclipse.che.datasource.shared.DatasourceConfigPreferences;
import org.eclipse.che.dto.server.DtoFactory;
//import org.eclipse.che.ide.api.preferences.PreferencesManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Singleton
public class DatasourceManagerPrefImpl implements DatasourceManager1 {

    private static final String                     PREFERENCE_KEY = "datasources";

//    private PreferencesManager                      preferencesManager;

    private DtoFactory                              dtoFactory;

    protected Map<String, DatabaseConfigurationDTO> inMemoryDatabaseConfigurations;

    @Inject
    public DatasourceManagerPrefImpl(
//    		final PreferencesManager prefManager,
                                     final DtoFactory dtoFactory) {
//        preferencesManager = prefManager;
        this.dtoFactory = dtoFactory;
        inMemoryDatabaseConfigurations = new HashMap<String, DatabaseConfigurationDTO>();
    }

    @Override
    public Iterator<DatabaseConfigurationDTO> getDatasources() {
        List<DatabaseConfigurationDTO> newList =
                                                 new ArrayList<DatabaseConfigurationDTO>(getDatasourceConfigPreferences().getDatasources()
                                                                                                                         .values());
        newList.addAll(inMemoryDatabaseConfigurations.values());
        return newList.iterator();
    }

    @Override
    public void add(final DatabaseConfigurationDTO configuration) {
        DatasourceConfigPreferences datasourcesPreferences = getDatasourceConfigPreferences();
        if (configuration.getRunnerProcessId() != null) {
            inMemoryDatabaseConfigurations.put(configuration.getDatasourceId(), configuration);
            // Workaround: avoid bug Fail to persist datasource
            saveDatasourceConfigPreferences(getDatasourceConfigPreferences());
            return;
        }
        datasourcesPreferences.getDatasources().put(configuration.getDatasourceId(), configuration);
        saveDatasourceConfigPreferences(datasourcesPreferences);
    }

    @Override
    public void remove(final DatabaseConfigurationDTO configuration) {
        inMemoryDatabaseConfigurations.remove(configuration.getDatasourceId());
        DatasourceConfigPreferences datasourcesPreferences = getDatasourceConfigPreferences();
        datasourcesPreferences.getDatasources().remove(configuration.getDatasourceId());
        saveDatasourceConfigPreferences(datasourcesPreferences);
    }

    @Override
    public DatabaseConfigurationDTO getByName(final String name) {
        if (inMemoryDatabaseConfigurations.containsKey(name)) {
            return inMemoryDatabaseConfigurations.get(name);
        }
        DatasourceConfigPreferences datasourcesPreferences = getDatasourceConfigPreferences();
        return datasourcesPreferences.getDatasources().get(name);
    }

    @Override
    public void persist() {
//        preferencesManager.flushPreferences();
    }

    @Override
    public Set<String> getNames() {
        DatasourceConfigPreferences datasourcesPreferences = getDatasourceConfigPreferences();
        HashSet<String> datasourceNames = new HashSet<String>(getDatasourceConfigPreferences().getDatasources().keySet());
        datasourceNames.addAll(inMemoryDatabaseConfigurations.keySet());
        return datasourceNames;
    }

    private DatasourceConfigPreferences getDatasourceConfigPreferences() {
//        String datasourcesJson = preferencesManager.getValue(PREFERENCE_KEY);
//        DatasourceConfigPreferences datasourcesPreferences;
//        if (datasourcesJson == null) {
//            datasourcesPreferences = dtoFactory.createDto(DatasourceConfigPreferences.class);
//        } else {
//            try {
//                datasourcesPreferences = dtoFactory.createDtoFromJson(datasourcesJson, DatasourceConfigPreferences.class);
//            } catch (Exception e) {
//                Log.error(DatasourceManagerPrefImpl.class, e);
//                datasourcesPreferences = dtoFactory.createDto(DatasourceConfigPreferences.class);
//            }
//        }
//        return datasourcesPreferences;
    	return null;
    }

    private void saveDatasourceConfigPreferences(final DatasourceConfigPreferences datasourcesPreferences) {
//        preferencesManager.setValue(PREFERENCE_KEY, dtoFactory.toJson(datasourcesPreferences));
    }

    @Override
    public String toString() {
//        return "DatasourceManagerPrefImpl[" + preferencesManager.getValue(PREFERENCE_KEY) + "]";
    	return "";
    }

    @Override
    public Iterator<DatabaseConfigurationDTO> iterator() {
        return getDatasources();
    }
}
