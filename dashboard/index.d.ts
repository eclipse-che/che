// remove this declaration after upgrading typescript version > 3.5
import * as _angular from 'angular';
import * as _jsyaml from 'js-yaml';

declare global {
  const angular: typeof _angular;
  const jsyaml: typeof _jsyaml;
}
