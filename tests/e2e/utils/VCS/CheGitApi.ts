import { injectable, inject } from 'inversify';
import { CLASSES } from '../../inversify.types';
import { CheApiRequestHandler } from '../../utils/requestHandlers/CheApiRequestHandler';


@injectable()
export class CheGitApi {
  static readonly GIT_API_ENTRIPOINT_URL = 'api/ssh/vcs';

  constructor(@inject(CLASSES.CheApiRequestHandler) private readonly processRequestHandler: CheApiRequestHandler) { }



  public async  getPublicSSHKey(): Promise<string> {

    try {
      const responce = await this.processRequestHandler.get(CheGitApi.GIT_API_ENTRIPOINT_URL);
      return responce.data[0].publicKey;
    } catch (error) {
      console.error('Cannot get public ssh key with API \n' + error);
      throw error;
    }

  }

}
