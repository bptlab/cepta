const webpack = require("webpack");

const fs = require("fs");
const packageJson = fs.readFileSync("./package.json");
const version = '"v' + JSON.parse(packageJson).version + '"' || '""';

module.exports = {
  lintOnSave: false,
  configureWebpack: {
    plugins: [
      new webpack.DefinePlugin({
        "process.env": {
          STABLE_VERSION: version,
          MONITORING_URL: '"' + process.env.MONITORING_URL + '"'
        }
      })
    ]
  },
  css: {
    loaderOptions: {
      sass: {
        prependData: '@import "@/style/global.sass";'
      }
    }
  }
};
