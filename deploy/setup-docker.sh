#!/usr/bin/env bash
set -euo pipefail

echo "Updating system packages..."
sudo dnf update -y

echo "Installing Docker..."
sudo dnf install -y docker

echo "Starting Docker service..."
sudo systemctl start docker
sudo systemctl enable docker

echo "Adding current user to docker group..."
sudo usermod -aG docker "$USER"

echo "Installing Docker Compose v2 plugin..."

DOCKER_CONFIG_DIR="/usr/local/lib/docker/cli-plugins"
sudo mkdir -p "$DOCKER_CONFIG_DIR"

COMPOSE_VERSION="v2.25.0"
sudo curl -SL \
  "https://github.com/docker/compose/releases/download/${COMPOSE_VERSION}/docker-compose-linux-x86_64" \
  -o "${DOCKER_CONFIG_DIR}/docker-compose"

sudo chmod +x "${DOCKER_CONFIG_DIR}/docker-compose"

echo "Installation complete."
echo
echo "IMPORTANT:"
echo "You must LOG OUT and LOG BACK IN for docker group permissions to take effect."
echo
echo "After re-login, verify with:"
echo "  docker --version"
echo "  docker compose version"
