import { echo, exec, ShellString } from 'shelljs';

export class ShellExecutor {

    wait(seconds: number): void {
        this.execWithLog(`sleep ${seconds}s`);
    }

    protected execWithLog(command: string): ShellString {
        echo(command);
        return exec(command);
    }
}
