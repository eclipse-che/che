package org.eclipse.che.datasource.shared;

import org.eclipse.che.dto.shared.DTO;

/**
 * Created by test on 7/15/17.
 */
@DTO
public interface ConnectionTestResultDTO {

    /**
     * The test result, success or failure.
     *
     * @return true for success, false for failure.
     */
    Status getTestResult();

    String getFailureMessage();

    void setTestResult(Status result);

    void setFailureMessage(String message);

    ConnectionTestResultDTO withTestResult(Status result);

    ConnectionTestResultDTO withFailureMessage(String message);

    enum Status {
        SUCCESS,
        FAILURE
    }
}
