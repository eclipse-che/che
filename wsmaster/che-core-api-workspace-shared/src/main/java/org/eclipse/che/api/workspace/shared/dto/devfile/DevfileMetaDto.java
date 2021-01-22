package org.eclipse.che.api.workspace.shared.dto.devfile;

import org.eclipse.che.dto.shared.DTO;

@DTO
public interface DevfileMetaDto {
  default String version() {
    return null;
  }
}
