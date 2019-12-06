var express = require('express');
const mustacheExpress = require('mustache-express');
var app = express();
app.engine('mustache', mustacheExpress());
app.set('view engine', 'mustache');

const ceptaEnvRegex = new RegExp("^CEPTA_.*$");

function ceptaEnvironmentVariables(obj) {
  return Object.entries(obj)
      .filter(([key]) => key.match(ceptaEnvRegex))
      .map(([key, value]) => {return {"@name": key,"@port": value}});
}

app.get('/', function (req, res) {
  res.render('front', { services: ceptaEnvironmentVariables(process.env) });
});

app.listen(3000, function () {
  console.log('Example app listening on port 3000!');
});