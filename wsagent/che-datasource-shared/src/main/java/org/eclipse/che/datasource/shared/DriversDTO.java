package org.eclipse.che.datasource.shared;

import java.util.List;
import org.eclipse.che.dto.shared.DTO;

/**
 * @Author "Sudaraka Jayathilaka"
 */
@DTO
public interface DriversDTO {
    void setDrivers(List<String> drivers);

    DriversDTO withDrivers(List<String> drivers);
    List<String> getDrivers();
}
