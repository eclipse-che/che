package org.eclipse.che.plugin.languageserver.shared;

import java.util.List;

import org.eclipse.che.dto.shared.DTO;

@DTO
public interface FileTypeDTO {
    String getId();

    List<String> getMimeTypes();

    String getExtension();

    String getNamePattern();

    String getContentDescription();
}
