package org.eclipse.che.datasource.shared;

import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/**
 * @Author "Sudaraka Jayathilaka"
 */
@DTO
public interface DriversDTO {
    DriversDTO withDrivers(List<String> drivers);

    List<String> getDrivers();

    void setDrivers(List<String> drivers);
}
