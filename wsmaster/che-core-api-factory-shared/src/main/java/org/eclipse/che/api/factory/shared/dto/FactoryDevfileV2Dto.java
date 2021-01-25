package org.eclipse.che.api.factory.shared.dto;

import static org.eclipse.che.api.core.factory.FactoryParameter.Obligation.MANDATORY;

import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.factory.FactoryParameter;
import org.eclipse.che.api.core.rest.shared.dto.Hyperlinks;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.dto.shared.DTO;

@DTO
public interface FactoryDevfileV2Dto extends FactoryMetaDto, Hyperlinks {

  @Override
  FactoryDevfileV2Dto withV(String v);

  @FactoryParameter(obligation = MANDATORY)
  Map<String, Object> getDevfile();

  void setDevfile(Map<String, Object> devfile);

  FactoryDevfileV2Dto withDevfile(Map<String, Object> devfile);

  @Override
  FactoryDevfileV2Dto withSource(String source);

  @Override
  FactoryDevfileV2Dto withLinks(List<Link> links);
}
