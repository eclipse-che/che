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
package org.eclipse.che.ide.ui.loaders.initialization;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.ArrayList;
import java.util.List;

import static org.eclipse.che.ide.ui.loaders.initialization.OperationInfo.Status;
import static org.eclipse.che.ide.ui.loaders.initialization.OperationInfo.Status.IN_PROGRESS;
import static org.eclipse.che.ide.ui.loaders.initialization.OperationInfo.Status.WAITING;
import static org.eclipse.che.ide.ui.loaders.initialization.InitialLoadingInfo.Operations.WORKSPACE_BOOTING;

/**
 * Contains information about the operations of initial loading in IDE.
 *
 * @author Roman Nikitenko
 */
@Singleton
public class InitialLoadingInfo implements LoadingInfo {

    private List<OperationInfo> operations   = new ArrayList<>(3);
    private List<String>        displayNames = new ArrayList<>(3);

    @Inject
    public InitialLoadingInfo(LoaderPresenter loader) {
        for (Operations operation : Operations.values()) {
            String operationName = operation.getValue();
            OperationInfo operationInfo = new OperationInfo(operationName, WAITING, loader);

            operations.add(operationInfo);
            displayNames.add(operationName);
        }
    }

    @Override
    public void setOperationStatus(String operationName, Status status) {
        if (IN_PROGRESS == status && WORKSPACE_BOOTING.getValue().equals(operationName)) {
            reset();
        }

        for (OperationInfo operationInfo : operations) {
            if (operationInfo.getOperationName().equals(operationName)) {
                operationInfo.setStatus(status);
            }
        }
    }

    @Override
    public List<OperationInfo> getOperations() {
        return operations;
    }

    @Override
    public List<String> getDisplayNames() {
        return displayNames;
    }

    private void reset() {
        for (OperationInfo operationInfo : operations) {
            operationInfo.setStatus(WAITING);
        }
    }

    /** The set of operations required for the initial loading in IDE. */
    public enum Operations {
        WORKSPACE_BOOTING("Initializing workspace"),
        MACHINE_BOOTING("Booting developer machine"),
        EXTENSION_SERVER_BOOTING("Starting workspace agent");

        private final String value;

        private Operations(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
