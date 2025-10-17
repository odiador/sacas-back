#!/bin/bash

# ACAS Backend - Docker Deployment Script

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Functions
print_header() {
    echo -e "\n${BLUE}========================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}========================================${NC}\n"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ $1${NC}"
}

# Check if .env exists
check_env_file() {
    if [ ! -f ".env" ]; then
        print_warning ".env file not found!"
        echo ""
        read -p "Do you want to create it from .env.example? (y/n): " -n 1 -r
        echo ""
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            cp .env.example .env
            print_success ".env file created from .env.example"
            print_warning "Please edit .env and update the values before continuing!"
            print_info "Run: nano .env or vim .env"
            exit 0
        else
            print_error "Cannot continue without .env file"
            exit 1
        fi
    fi
    print_success ".env file found"
}

# Check if Docker is installed
check_docker() {
    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed"
        exit 1
    fi
    print_success "Docker is installed"
}

# Check if Docker Compose is installed
check_docker_compose() {
    if ! command -v docker compose &> /dev/null; then
        print_error "Docker Compose is not installed"
        exit 1
    fi
    print_success "Docker Compose is installed"
}

# Determine which compose file to use
get_compose_file() {
    if [ "$USE_DB" = "true" ]; then
        echo "-f docker-compose.db.yml"
    else
        echo ""
    fi
}

# Build the Docker image
build() {
    print_header "Building Docker Image"
    local compose_file=$(get_compose_file)
    docker compose $compose_file build
    print_success "Docker image built successfully"
}

# Start the services
start() {
    print_header "Starting ACAS Backend"
    local compose_file=$(get_compose_file)
    docker compose $compose_file up -d
    print_success "ACAS Backend started"
    if [ "$USE_DB" = "true" ]; then
        print_info "Backend + PostgreSQL started"
    fi
    print_info "View logs: docker compose $compose_file logs -f"
    print_info "Check status: docker compose $compose_file ps"
}

# Stop the services
stop() {
    print_header "Stopping ACAS Backend"
    local compose_file=$(get_compose_file)
    docker compose $compose_file down
    print_success "ACAS Backend stopped"
}

# Restart the services
restart() {
    print_header "Restarting ACAS Backend"
    local compose_file=$(get_compose_file)
    docker compose $compose_file restart
    print_success "ACAS Backend restarted"
}

# View logs
logs() {
    print_header "Viewing Logs"
    local compose_file=$(get_compose_file)
    docker compose $compose_file logs -f
}

# Check status
status() {
    print_header "Service Status"
    local compose_file=$(get_compose_file)
    docker compose $compose_file ps
    echo ""
    print_info "Health Check:"
    curl -s http://localhost:3001/api/actuator/health || print_warning "Service not responding"
}

# Clean everything
clean() {
    print_header "Cleaning Up"
    local compose_file=$(get_compose_file)
    read -p "This will remove all containers and volumes. Continue? (y/n): " -n 1 -r
    echo ""
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        docker compose $compose_file down -v
        print_success "Cleanup completed"
    else
        print_info "Cleanup cancelled"
    fi
}

# Show help
show_help() {
    cat << EOF
ACAS Backend Docker Deployment Script

Usage: ./deploy.sh [command] [options]

Commands:
  build      Build the Docker image
  start      Start the services
  stop       Stop the services
  restart    Restart the services
  logs       View service logs
  status     Check service status
  clean      Remove containers and volumes
  help       Show this help message

Options:
  --with-db  Use docker-compose.db.yml (includes PostgreSQL)

Examples:
  ./deploy.sh build
  ./deploy.sh start
  ./deploy.sh start --with-db   # Start with PostgreSQL
  ./deploy.sh logs --with-db
  ./deploy.sh status

For more information, see DOCKER-DEPLOYMENT.md and NGROK-GUIDE.md
EOF
}

# Main script
print_header "ACAS Backend Deployment"

# Check for --with-db flag
USE_DB=false
for arg in "$@"; do
    if [ "$arg" = "--with-db" ]; then
        USE_DB=true
    fi
done

# Parse command
case "${1:-}" in
    build)
        check_docker
        check_docker_compose
        check_env_file
        build
        ;;
    start)
        check_docker
        check_docker_compose
        check_env_file
        start
        ;;
    stop)
        check_docker
        check_docker_compose
        stop
        ;;
    restart)
        check_docker
        check_docker_compose
        restart
        ;;
    logs)
        check_docker
        check_docker_compose
        logs
        ;;
    status)
        check_docker
        check_docker_compose
        status
        ;;
    clean)
        check_docker
        check_docker_compose
        clean
        ;;
    help|--help|-h)
        show_help
        ;;
    "")
        print_error "No command specified"
        echo ""
        show_help
        exit 1
        ;;
    *)
        print_error "Unknown command: $1"
        echo ""
        show_help
        exit 1
        ;;
esac

print_success "Done!"
