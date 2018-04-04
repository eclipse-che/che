import {error, isUndefined} from "util";

/**
 * Defines location of a server, e.g. hostname, port, etc.
 *
 * @author Oleksandr Garagatyi
 */
export class ServerLocation {
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
            error('Server location parsing failed. Server url should not be undefined')
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
