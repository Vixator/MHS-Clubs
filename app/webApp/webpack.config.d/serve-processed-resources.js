const path = require("path");

config.devServer.static = {
  directory: path.resolve(__dirname, "../../../../app/webApp/build/processedResources/js/main"),
};
