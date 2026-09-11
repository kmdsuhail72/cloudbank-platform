#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(
    cd "$(
        dirname "${BASH_SOURCE[0]}"
    )" &&
    pwd
)"

REPO_ROOT="$(
    cd "$SCRIPT_DIR/../.." &&
    pwd
)"

BASE="${CLOUDBANK_OBSERVABILITY_RUNTIME_DIR:-/tmp/cloudbank-observability-147c}"

PROM_IMAGE="prom/prometheus:v3.2.1"
GRAF_IMAGE="grafana/grafana:13.2.0"

PROM_EXE="$BASE/prometheus/prometheus"
PROMTOOL="$BASE/prometheus/promtool"

GRAF_HOME="$BASE/grafana"
GRAF_EXE="$GRAF_HOME/bin/grafana"

PASSWORD_FILE="/tmp/cloudbank-grafana-admin-password"

cleanup_on_failure() {
    local rc="$?"

    if [ "$rc" -ne 0 ]; then
        "$SCRIPT_DIR/stop-local.sh" \
            >/dev/null 2>&1 || true
    fi

    exit "$rc"
}

trap cleanup_on_failure EXIT

for port in 19090 3000; do
    if ss -ltnp |
       grep -q ":$port "
    then
        echo "STOP: port $port already occupied"
        exit 1
    fi
done

NOTIFICATION_CODE="$(
    curl -sS \
        -o /dev/null \
        -w '%{http_code}' \
        http://127.0.0.1:8086/actuator/health \
        2>/dev/null || true
)"

if [ "$NOTIFICATION_CODE" != "200" ]; then
    echo "STOP: Notification must be healthy before observability starts"
    exit 1
fi

ensure_image() {
    local image="$1"

    if ! docker image inspect \
         "$image" \
         >/dev/null 2>&1
    then
        docker pull "$image"
    fi
}

ensure_image "$PROM_IMAGE"
ensure_image "$GRAF_IMAGE"

if [ ! -x "$PROM_EXE" ] ||
   [ ! -x "$PROMTOOL" ]
then
    mkdir -p "$BASE/prometheus"

    cid="$(
        docker create "$PROM_IMAGE"
    )"

    trap \
        'docker rm -f "$cid" >/dev/null 2>&1 || true' \
        RETURN

    docker cp \
        "$cid:/bin/prometheus" \
        "$PROM_EXE"

    docker cp \
        "$cid:/bin/promtool" \
        "$PROMTOOL"

    docker rm -f "$cid" \
        >/dev/null

    trap - RETURN

    chmod +x \
        "$PROM_EXE" \
        "$PROMTOOL"
fi

if [ ! -x "$GRAF_EXE" ]; then
    mkdir -p "$GRAF_HOME"

    cid="$(
        docker create "$GRAF_IMAGE"
    )"

    trap \
        'docker rm -f "$cid" >/dev/null 2>&1 || true' \
        RETURN

    docker cp \
        "$cid:/usr/share/grafana/." \
        "$GRAF_HOME/"

    docker rm -f "$cid" \
        >/dev/null

    trap - RETURN

    chmod +x "$GRAF_EXE"
fi

"$PROMTOOL" \
    check config \
    "$REPO_ROOT/deploy/observability/prometheus/prometheus.yml"

mkdir -p \
    "$BASE/prometheus-data" \
    "$BASE/grafana-data" \
    "$BASE/grafana-logs" \
    "$BASE/grafana-plugins"

nohup \
    "$PROM_EXE" \
    --config.file="$REPO_ROOT/deploy/observability/prometheus/prometheus.yml" \
    --storage.tsdb.path="$BASE/prometheus-data" \
    --storage.tsdb.retention.time=24h \
    --web.listen-address="127.0.0.1:19090" \
    > /tmp/cloudbank-prometheus-147c.log \
    2>&1 &

echo "$!" \
    > /tmp/cloudbank-prometheus-147c.pid

for _ in $(seq 1 30); do
    code="$(
        curl -sS \
            -o /dev/null \
            -w '%{http_code}' \
            http://127.0.0.1:19090/-/ready \
            2>/dev/null || true
    )"

    [ "$code" = "200" ] && break

    sleep 1
done

if [ "${code:-}" != "200" ]; then
    echo "STOP: Prometheus failed to become ready"
    exit 1
fi

if [ ! -f "$PASSWORD_FILE" ]; then
    umask 077

    {
        cat /proc/sys/kernel/random/uuid
        cat /proc/sys/kernel/random/uuid
    } |
    tr -d '\n-' \
        > "$PASSWORD_FILE"
fi

chmod 600 "$PASSWORD_FILE"

export CLOUDBANK_PROMETHEUS_URL="http://127.0.0.1:19090"

export CLOUDBANK_GRAFANA_DASHBOARDS_PATH="$REPO_ROOT/deploy/observability/grafana/dashboards"

export GF_SERVER_HTTP_ADDR="127.0.0.1"
export GF_SERVER_HTTP_PORT="3000"

export GF_PATHS_DATA="$BASE/grafana-data"
export GF_PATHS_LOGS="$BASE/grafana-logs"
export GF_PATHS_PLUGINS="$BASE/grafana-plugins"
export GF_PATHS_PROVISIONING="$REPO_ROOT/deploy/observability/grafana/provisioning"

export GF_SECURITY_ADMIN_USER="admin"
export GF_SECURITY_ADMIN_PASSWORD="$(
    cat "$PASSWORD_FILE"
)"

export GF_USERS_ALLOW_SIGN_UP="false"
export GF_ANALYTICS_REPORTING_ENABLED="false"
export GF_ANALYTICS_CHECK_FOR_UPDATES="false"

nohup \
    "$GRAF_EXE" \
    server \
    --homepath "$GRAF_HOME" \
    > /tmp/cloudbank-grafana-147c.log \
    2>&1 &

echo "$!" \
    > /tmp/cloudbank-grafana-147c.pid

for _ in $(seq 1 60); do
    code="$(
        curl -sS \
            -o /dev/null \
            -w '%{http_code}' \
            http://127.0.0.1:3000/api/health \
            2>/dev/null || true
    )"

    [ "$code" = "200" ] && break

    sleep 1
done

if [ "${code:-}" != "200" ]; then
    echo "STOP: Grafana failed to become healthy"
    exit 1
fi

trap - EXIT

echo "NOTIFICATION_HTTP=200"
echo "PROMETHEUS_HTTP=200"
echo "GRAFANA_HTTP=200"
echo "CLOUDBANK_OBSERVABILITY_STARTED=true"
