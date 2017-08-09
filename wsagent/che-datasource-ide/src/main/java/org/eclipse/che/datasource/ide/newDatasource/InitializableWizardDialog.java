package org.eclipse.che.datasource.ide.newDatasource;

public interface InitializableWizardDialog<T> {

    void initData(final T configuration);
}