import { echo, exec, ShellString } from 'shelljs';

export class ShellExecutor {

    protected execWithLog(command: string): ShellString {
        echo(command);
        return exec(command);
    }
}
