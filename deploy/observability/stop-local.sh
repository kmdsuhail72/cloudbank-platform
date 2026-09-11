#!/usr/bin/env bash
set -euo pipefail

BASE="${CLOUDBANK_OBSERVABILITY_RUNTIME_DIR:-/tmp/cloudbank-observability-147c}"

stop_owned_process() {
    local name="$1"
    local pid_file="$2"
    local expected_exe="$3"

    if [ ! -f "$pid_file" ]; then
        echo "$name=NOT_RUNNING"
        return
    fi

    local pid
    pid="$(cat "$pid_file")"

    if ! [[ "$pid" =~ ^[0-9]+$ ]]; then
        echo "STOP: malformed PID file for $name"
        exit 1
    fi

    if [ ! -d "/proc/$pid" ]; then
        rm -f "$pid_file"
        echo "$name=NOT_RUNNING"
        return
    fi

    local actual_exe
    actual_exe="$(
        readlink -f "/proc/$pid/exe"
    )"

    if [ "$actual_exe" != "$expected_exe" ]; then
        echo "STOP: $name ownership not proven"
        exit 1
    fi

    kill -TERM "$pid"

    for _ in $(seq 1 30); do
        if [ ! -d "/proc/$pid" ]; then
            break
        fi

        sleep 1
    done

    if [ -d "/proc/$pid" ]; then
        echo "STOP: $name failed to terminate"
        exit 1
    fi

    rm -f "$pid_file"

    echo "$name=STOPPED"
}

stop_owned_process \
    "GRAFANA" \
    "/tmp/cloudbank-grafana-147c.pid" \
    "$BASE/grafana/bin/grafana"

stop_owned_process \
    "PROMETHEUS" \
    "/tmp/cloudbank-prometheus-147c.pid" \
    "$BASE/prometheus/prometheus"
