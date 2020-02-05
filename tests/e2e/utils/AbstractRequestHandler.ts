import { RequestType } from '..';
import axios from 'axios';
import { injectable } from 'inversify';
import { IRequestHandler } from './IRequestHandler';

@injectable()
export class AbstractRequestHandler implements IRequestHandler {
    setHeaders(): void {
        // by default nothing to set
    }

    async processRequest(reqType: RequestType, url: string, data?: string) {
        let response;
        await this.setHeaders();
        switch (reqType) {
            case RequestType.GET: {
                response = await axios.get(url);
                break;
            }
            case RequestType.DELETE: {
                response = await axios.delete(url);
                break;
            }
            case RequestType.POST: {
                if ( data === undefined ) {
                    response = axios.post(url);
                } else {
                    response = axios.post(url, data);
                }
                break;
            }
            default: {
                throw new Error('Unknown RequestType: ' + reqType);
            }
        }
        return response;
    }
}
