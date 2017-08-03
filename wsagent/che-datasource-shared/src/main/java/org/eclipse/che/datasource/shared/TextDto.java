package org.eclipse.che.datasource.shared;

import org.eclipse.che.dto.shared.DTO;

/**
 * Created by test on 7/15/17.
 */
@DTO
public interface TextDto {
    String getValue();

    void setValue(String text);

    TextDto withValue(String text);
}

