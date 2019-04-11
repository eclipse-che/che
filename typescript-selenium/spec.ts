import { e2eContainer } from "./inversify.config";
import { Driver } from "./driver/Driver";
import { TYPES } from "./types";


const driver: Driver = e2eContainer.get<Driver>(TYPES.Driver)

function doNavigation() {
    driver.get()
        .navigate()
        .to("https://google.com")
}

doNavigation()

