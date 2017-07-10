package org.eclipse.che.datasource.api;

import com.google.inject.AbstractModule;
import org.eclipse.che.inject.DynaModule;
/**
 * Created by sudaraka on 7/7/17.
 */
@DynaModule
public class DatasourceModule extends AbstractModule{
    @Override
    protected void configure() {
        bind(DatasourceService.class);
        bind(AvailableDriversService.class);
    }

}
