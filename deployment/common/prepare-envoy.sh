apt update
apt install -y gettext-base
# $CEPTA_ENVOY_INGRESS_PORT,$CEPTA_ENVOY_GRPC_PORT
cat /etc/envoy/envoy.yaml.tmpl | envsubst > /etc/envoy/envoy.yaml
echo "Did substitute config file"
exec /docker-entrypoint.sh "$@"