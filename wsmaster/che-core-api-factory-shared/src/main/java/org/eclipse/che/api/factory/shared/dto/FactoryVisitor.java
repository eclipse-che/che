package org.eclipse.che.api.factory.shared.dto;

public interface FactoryVisitor {
  FactoryDto visit(FactoryDto factoryDto);

  default FactoryDevfileV2Dto visit(FactoryDevfileV2Dto factoryDto) {
    // most likely nothing to do with Devfile v2 factory as we don't know or touch the structure
    return factoryDto;
  }
}
