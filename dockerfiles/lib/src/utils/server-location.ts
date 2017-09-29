import {error, isUndefined} from "util";
import {RemoteIp} from "../spi/docker/remoteip";

/**
 * Defines location of a server, e.g. hostname, port, etc.
 *
 * @author Oleksandr Garagatyi
 */
export class ServerLocation {
    private static DEFAULT_HOSTNAME : string = new RemoteIp().getIp();
    private static DEFAULT_PORT : number = 8080;

    private port : number;
    private hostname : string;
    private secure: boolean;

    constructor(hostname : string, port : number, secure : boolean) {
        if (!port || !hostname || isUndefined(secure)) {
            error('ServerLocation object creation failed. Arguments should not be undefined')
        }
        this.port = port;
        this.secure = secure;
        this.hostname = hostname;
    }

    static parse(stringUrl : string): ServerLocation {
        if (!stringUrl) {
            return this.detectCheMasterLocation();
        }
        const url = require('url').parse(stringUrl);
        let port: number;
        let isSecured: boolean = false;
        // do we have a port ?
        if ('https:' === url.protocol || 'wss:' === url.protocol) {
            isSecured = true;
        }
        if (!url.port) {
            if ('http:' === url.protocol || 'ws:' === url.protocol) {
                port = 80;
            } else if ('https:' === url.protocol || 'wss:' === url.protocol) {
                port = 443;
            }
        } else {
            port = +url.port;
        }
        return new ServerLocation(url.hostname, port, isSecured);
    }

    static detectCheMasterLocation(): ServerLocation {
        // handle CHE_HOST if any
        let hostname: string;
        let port: number;
        let secured = false;

        if (process.env.CHE_HOST) {
            hostname = process.env.CHE_HOST;
        } else {
            hostname = ServerLocation.DEFAULT_HOSTNAME;
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
            port = ServerLocation.DEFAULT_PORT;
        }

        return new ServerLocation(hostname, port, secured);
    }

    isSecure(): boolean {
        return this.secure;
    }

    getHostname(): string {
        return this.hostname;
    }

    getPort(): number {
        return this.port;
    }

    setPort(port : number) {
        this.port = port;
    }
}