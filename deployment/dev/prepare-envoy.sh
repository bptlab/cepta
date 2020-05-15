apt update
apt install -y gettext-base iproute2

# echo $(ip -4 addr show docker0 | grep -Po 'inet \K[\d.]+')
# export ENVOY_HOST=$(/sbin/ip route|awk '/default/ { print $3 }')
# export ENVOY_HOST=$(ip route show | awk '/default/ {print $3}')

# This might be a good direction for a nice solution but currently not in use
# Also, services might need to bind to 0.0.0.0 and also the host firewall must allow incoming traffic from the docker0 bridge
HOST_DOMAIN="host.docker.internal"
ping -q -c1 $HOST_DOMAIN > /dev/null 2>&1
if [ $? -ne 0 ]; then
  HOST_IP=$(ip route | awk 'NR==1 {print $3}')
  echo -e "$HOST_IP\t$HOST_DOMAIN" >> /etc/hosts
fi
cat /etc/hosts

# $CEPTA_ENVOY_INGRESS_PORT,$CEPTA_ENVOY_GRPC_PORT
cat /etc/envoy/envoy.yaml.tmpl | envsubst > /etc/envoy/envoy.yaml
echo "Did substitute config file"
cat /etc/envoy/envoy.yaml
exec /docker-entrypoint.sh "$@"