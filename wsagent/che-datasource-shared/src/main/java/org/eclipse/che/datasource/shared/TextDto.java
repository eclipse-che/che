package org.eclipse.che.datasource.shared;

import org.eclipse.che.dto.shared.DTO;

/**
 * Created by test on 7/15/17.
 */
@DTO
public interface TextDto {
    String getValue();

    TextDto withValue(String text);

    void setValue(String text);
}

