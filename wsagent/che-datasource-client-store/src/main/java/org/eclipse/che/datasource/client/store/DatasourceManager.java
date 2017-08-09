package org.eclipse.che.datasource.client.store;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.eclipse.che.api.user.shared.dto.ProfileDto;
import org.eclipse.che.datasource.shared.DatabaseConfigurationDTO;
import java.util.Iterator;
import java.util.Set;;
import org.eclipse.che.api.user.shared.dto.ProfileDto;

/**
 * Created by test on 7/23/17.
 */

public interface DatasourceManager extends Iterable<DatabaseConfigurationDTO> {

    Iterator<DatabaseConfigurationDTO> getDatasources();

    void add(final DatabaseConfigurationDTO configuration);

    public void remove(final DatabaseConfigurationDTO configuration);

    public DatabaseConfigurationDTO getByName(final String name);

    public Set<String> getNames();

    public void persist();
}

