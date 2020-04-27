## Generating your own JWT auth keys...

#### using the provided utility

```bash
python keygen.py ./sample/auth
```

#### manually

```bash
# Don't add passphrase
ssh-keygen -t rsa -b 4096 -m PEM -f rs256-jwt-sample.key
# Convert to jwk
npx ssh-to-jwk rs256-jwt-sample.key.pub > rs256-jwt-sample.jwk.json
# Finally, set "alg" and "kid" in rs256-jwt-sample.jwk.json
# Wrap the entry in rs256-jwt-sample.jwk.json inside a "keys" list
```

#### Using different signing algorithm

You can change the default signing algorithm to be different than `RS256`.
If you do, make sure to to generate the key with this algorithm, change the `alg` in the `jwk.json` key store and
change the signing algorithm used in the `auth` microservice when signing the jwt tokens.