import { injectable } from 'inversify';
import { IRequestHandler } from './IRequestHandler';
import { AbstractRequestHandler } from './AbstractRequestHandler';

@injectable()
export class SingleUserRequestHandler extends AbstractRequestHandler implements IRequestHandler {

}
