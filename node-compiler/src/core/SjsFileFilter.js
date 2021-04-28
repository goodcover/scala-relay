import type {FileFilter} from "relay-compiler/codegen/CodegenWatcher";

const fs = require('fs');
const path = require('path');



function defaultGetFileFilter(baseDir: string): FileFilter {
  return (file: File) => {
    const filePath = path.join(baseDir, file.relPath);
    let text = '';
    try {
      text = fs.readFileSync(filePath, 'utf8');
    } catch {
      // eslint-disable no-console
      console.warn(
        `RelaySourceModuleParser: Unable to read the file "${filePath}". Looks like it was removed.`,
      );
      return false;
    }
    return text.indexOf('@graphql') >= 0 ||
      text.indexOf('graphqlGen') >= 0 ||
      path.extname(filePath) === '.gql';
  };
}

module.exports = {
  fileFilter: defaultGetFileFilter
}
