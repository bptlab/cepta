module.exports = {
  lintOnSave: false,
  css: {
    loaderOptions: {
      sass: {
        prependData: '@import "@/style/global.sass";'
      }
    }
  }
}
