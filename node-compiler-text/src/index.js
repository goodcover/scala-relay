const scalajsPlugin = require("relay-compiler-language-scalajs");

function plugin() {
  return scalajsPlugin({mode: "gql"});
}

module.exports = plugin
