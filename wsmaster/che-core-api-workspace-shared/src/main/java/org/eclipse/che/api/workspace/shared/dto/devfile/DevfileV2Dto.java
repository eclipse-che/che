package org.eclipse.che.api.workspace.shared.dto.devfile;

public interface DevfileV2Dto extends DevfileMetaDto {

  @Override
  default String version() {
    return "2.0.0";
  }

  Object getContent();
  DevfileV2Dto withContent(Object devfileContent);
}
