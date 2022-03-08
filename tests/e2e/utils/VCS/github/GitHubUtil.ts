import { injectable } from 'inversify';
import axios from 'axios';

@injectable()
export class GitHubUtil {
  private static readonly GITHUB_API_ENTRIPOINT_URL = 'https://api.github.com/';
  /**
   * add public part of ssh key to the defied github account
   * @param authToken
   * @param title
   * @param key
   */
  async addPublicSshKeyToUserAccount(authToken: string, title: string, key: string) {
    const gitHubApiSshURL: string = GitHubUtil.GITHUB_API_ENTRIPOINT_URL + 'user/keys';
    const authHeader = { headers: { 'Authorization': 'token ' + authToken, 'Content-Type': 'application/json' } };

    const data = {
      title: `${title}`,
      key: `${key}`
    };

    try { await axios.post(gitHubApiSshURL, JSON.stringify(data), authHeader); } catch (error) {
      console.error('Cannot add the public key to the GitHub account: ');
      console.error(error);
      throw error;
    }
  }

  async getRawContentFromFile(pathToFile: string): Promise<string> {
    const gitHubContentEntryPointUrl: string = 'https://raw.githubusercontent.com/';
    const pathToRawContent: string = `${gitHubContentEntryPointUrl}${pathToFile}`;
    const authorization: string = 'Authorization';
    const contentType: string = 'Content-Type';

    try {
      delete axios.defaults.headers.common[authorization];
      delete axios.defaults.headers.common[contentType];
      const response = await axios.get(`${gitHubContentEntryPointUrl}${pathToFile}`);
      return response.data;
    } catch (error) {
      console.error('Cannot get content form the raw github content: ' + pathToRawContent);
      console.error(error);
      throw error;
    }
  }

  async getPublicSshKeys(authToken: string): Promise<Array<string>> {
    const gitHubApiSshURL: string = GitHubUtil.GITHUB_API_ENTRIPOINT_URL + 'user/keys';
    const authHeader = { headers: { 'Authorization': 'token ' + authToken, 'Content-Type': 'application/json' } };
    try {
      const response = await axios.get(gitHubApiSshURL, authHeader);
      const stringified = JSON.stringify(response.data);
      const arrayOfWorkspaces = JSON.parse(stringified);
      const idOfRunningWorkspace: Array<string> = new Array();
      for (let entry of arrayOfWorkspaces) {
        idOfRunningWorkspace.push(entry.id);
      }
      return idOfRunningWorkspace;
    } catch (error) {
      console.error('Cannot get public Keys from github: ' + gitHubApiSshURL);
      console.error(error);
      throw error;
    }
  }

  async removePublicSshKey(authToken: string, keyId: string) {
    const gitHubApiSshURL: string = GitHubUtil.GITHUB_API_ENTRIPOINT_URL + 'user/keys/' + keyId;
    const authHeader = { headers: { 'Authorization': 'token ' + authToken, 'Content-Type': 'application/json' } };
    try { await axios.delete(gitHubApiSshURL, authHeader); } catch (error) {
      console.error('Cannot delete the public key from the GitHub account: ');
      console.error(error);
      throw error;
    }
  }

  async deletePublicSshKeyByName(authToken: string, keyName: string) {
    const gitHubApiSshURL: string = GitHubUtil.GITHUB_API_ENTRIPOINT_URL + 'user/keys';
    const authHeader = { headers: { 'Authorization': 'token ' + authToken, 'Content-Type': 'application/json' } };
    try {
      const response = await axios.get(gitHubApiSshURL, authHeader);
      const stringified = JSON.stringify(response.data);
      const arrayOfPublicKeys = JSON.parse(stringified);
      for (let entry of arrayOfPublicKeys) {
        if (entry.title === keyName) {
          this.removePublicSshKey(authToken, entry.id);
          break;
        }
      }
    } catch (error) {
      console.error('Cannot delete the ' + keyName + ' public key from the GitHub account');
      console.error(error);
      throw error;
    }
  }

  async removeAllPublicSshKeys(authToken: string) {
    try {
      const idList: string[] = await this.getPublicSshKeys(authToken);
      for (let id of idList) {
        this.removePublicSshKey(authToken, id);
      }

    } catch (error) {
      console.error('Cannot delete the public key from the GitHub account: ');
      console.error(error);
      throw error;
    }
  }

}
