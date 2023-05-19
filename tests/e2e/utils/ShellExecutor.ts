import { echo, exec, ShellString } from 'shelljs';

export class ShellExecutor {

    static wait(seconds: number): void {
        this.execWithLog(`sleep ${seconds}s`);
    }

    static curl(link: string): ShellString {
        return this.execWithLog(`curl -k ${link}`);
    }

    protected static execWithLog(command: string): ShellString {
        echo(command);
        return exec(command);
    }
}
