package org.eclipse.che.datasource.api;

import com.google.inject.AbstractModule;
import org.eclipse.che.datasource.api.ssl.KeyStoreObject;
import org.eclipse.che.datasource.api.ssl.SslKeyStoreService;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.datasource.api.ssl.TrustStoreObject;
/**
 * Created by sudaraka on 7/7/17.
 */
@DynaModule
public class DatasourceModule extends AbstractModule{
    @Override
    protected void configure() {
        bind(JdbcConnectionFactory.class);
        bind(KeyStoreObject.class);
        bind(TrustStoreObject.class);
        bind(SslKeyStoreService.class);
        bind(AvailableDriversService.class);
        bind(TestConnectionService.class);
        bind(EncryptTextService.class);
    }

}
