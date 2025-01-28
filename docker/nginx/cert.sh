docker-compose run --rm --entrypoint "\
  certbot certonly \
  -d example.com \
  -d *.example.com \
  --email aaa@gmail.com \
  --manual --preferred-challenges dns \
  --server https://acme-v02.api.letsencrypt.org/directory \
  --force-renewal" certbot