#!/bin/bash

# healthcheck func
check_services() {
    local compose_file=$1
    local timeout=60
    local elapsed=0

    echo "${compose_file} services check..."

    while [ $elapsed -lt $timeout ]; do
        if docker-compose -f "$compose_file" ps | grep -q "Up"; then
            echo "${compose_file} services are up"
            return 0
        fi
        sleep 5
        elapsed=$((elapsed + 5))
    done

    echo "Error: ${compose_file} services didn't start after ${timeout} seconds" >&2
    docker-compose -f "$compose_file" logs  # Check logs for debug
    exit 1
}

check_container_healthy() {
    local container_name=$1
    local status

    status=$(docker inspect --format='{{.State.Health.Status}}' "${container_name}" 2>/dev/null || echo "unhealthy")

    if [[ $status == "healthy" ]]; then
        echo "true"
        return 0
    else
        echo "false"
    fi
    return 1
}

core_healthcheck() {
    local timeout=120  # Max timeout
    local interval=5
    local elapsed=0
    local gateway_ready=false
    local config_ready=false

    echo "Checking core services..."
    while (( elapsed < timeout )); do
        gateway_ready=$(check_container_healthy "gateway-ms-container")
        config_ready=$(check_container_healthy "config-ms-container")

        if [[ $gateway_ready == true && $config_ready == true ]]; then
            echo "All core-services up and running"
            return 0
        fi

        sleep $interval
        elapsed=$((elapsed + interval))
        echo "Waiting... ${elapsed} seconds elapsed"
    done

    echo "Error: services not ready after $timeout seconds"
    echo "Current status: "
    echo "  gateway-ms-container: $(docker inspect --format='{{.State.Health.Status}}' gateway-ms-container 2>/dev/null || echo 'Not ready')"
    echo "  config-ms-container: $(docker inspect --format='{{.State.Health.Status}}' config-ms-container 2>/dev/null || echo 'Not ready')"
    printf "\nTry to check logs"
    return 1;
}


cd "../docker" || { echo "Error change dir"; exit 1; }

echo "Creating network for containers"
docker network create bd-network || { echo "Error creating 'bd-network' network"; return 1; }

docker_files=("infrastructure" "core" "business-logic")

for cur_file in "${docker_files[@]}"; do
  echo "Loading ${cur_file}..."
  docker-compose -f docker-compose."${cur_file}".yml up -d || { echo "Error starting ${cur_file}"; return 1; }

  if [[ ${cur_file} == "core" ]]; then
    core_healthcheck
  else
    check_services "docker-compose.${cur_file}.yml"
  fi
done

echo "All services up and running"