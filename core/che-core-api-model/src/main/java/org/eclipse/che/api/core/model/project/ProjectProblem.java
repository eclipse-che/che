package org.eclipse.che.api.core.model.project;

/**
 * @author Vitalii Parfonov
 */

public interface ProjectProblem {

    int getCode();

    String getMessage();
}
