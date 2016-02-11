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

/**
 * Contains the information about the operation.
 *
 * @author Roman Nikitenko
 */
public class OperationInfo {

    private StatusListener statusListener;
    private String         operation;
    private Status         status;

    public OperationInfo(String operation, Status status, StatusListener listener) {
        this(operation, status);
        this.statusListener = listener;
    }

    public OperationInfo(String operation, Status status) {
        this.operation = operation;
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
        if (statusListener != null) {
            statusListener.onStatusChanged(this);
        }
    }

    public String getOperationName() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public void setStatusListener(StatusListener listener) {
        this.statusListener = listener;
    }

    /** Status of a operation. */
    public enum Status {
        WAITING("WAITING"),
        IN_PROGRESS("LOADING"),
        SUCCESS("SUCCESS"),
        ERROR("ERROR");

        private final String value;

        private Status(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    /** Listener's method will be invoked when status is change. */
    interface StatusListener {
        void onStatusChanged(OperationInfo operation);
    }
}
