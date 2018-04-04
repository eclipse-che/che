import {ServerLocation} from "../../utils/server-location";
import {RemoteIp} from "../../spi/docker/remoteip";

/**
 * ServerLocation implementation for Che master with ability of automatic detection of the Che WS master URL
 *
 * @author Oleksandr Garagatyi
 */
export class WsMasterLocation extends ServerLocation {
    private static DEFAULT_HOSTNAME : string = new RemoteIp().getIp();
    private static DEFAULT_PORT : number = 8080;

    /**
     * Automatically detects ServerLocation of Che WS master if provided url is undefined
     * @param {string} url of Che WS master
     */
    constructor(url?: string) {
        if (url) {
            let server = ServerLocation.parse(url);
            super(server.getHostname(), server.getPort(), server.isSecure());
            return;
        }

        // handle CHE_HOST if any
        let hostname: string;
        let port: number;
        let secured = false;

        if (process.env.CHE_HOST) {
            hostname = process.env.CHE_HOST;
        } else {
            hostname = WsMasterLocation.DEFAULT_HOSTNAME;
        }

        // handle CHE_HOST_PROTOCOL if any
        let hostProtocol :string = process.env.CHE_HOST_PROTOCOL;
        if (hostProtocol && hostProtocol === "https") {
            secured = true;
        }

        // handle CHE_PORT if any
        if (process.env.CHE_PORT) {
            port = process.env.CHE_PORT;
        } else if (secured) {
            port = 443;
        } else {
            port = WsMasterLocation.DEFAULT_PORT;
        }

        super(hostname, port, secured);
    }
}