#!/usr/bin/env bash

# Kathena Container Management Script
# Manages Docker containers for MongoDB, Elasticsearch, and Redpanda (Kafka)

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Container configurations
MONGO_NAME="kathena-mongo"
MONGO_PORT="27017"
MONGO_IMAGE="mongo:7"

ES_NAME="kathena-es"
ES_PORT="9200"
ES_IMAGE="docker.elastic.co/elasticsearch/elasticsearch:8.15.5"

KAFKA_NAME="kathena-redpanda"
KAFKA_PORT="9092"
KAFKA_ADMIN_PORT="9644"
KAFKA_IMAGE="redpandadata/redpanda:latest"

print_status() {
    echo -e "${BLUE}ℹ${NC}  $1"
}

print_success() {
    echo -e "${GREEN}✅${NC} $1"
}

print_error() {
    echo -e "${RED}❌${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠️${NC}  $1"
}

check_container() {
    local name=$1
    if docker ps -a --format '{{.Names}}' | grep -q "^${name}$"; then
        if docker ps --format '{{.Names}}' | grep -q "^${name}$"; then
            return 0  # Running
        else
            return 1  # Exists but not running
        fi
    else
        return 2  # Does not exist
    fi
}

start_mongo() {
    print_status "Starting MongoDB..."
    
    if check_container "$MONGO_NAME"; then
        print_warning "MongoDB container already running"
    elif [ $? -eq 1 ]; then
        docker start "$MONGO_NAME"
        print_success "MongoDB container started"
    else
        docker run --name "$MONGO_NAME" -p "$MONGO_PORT:$MONGO_PORT" -d "$MONGO_IMAGE"
        print_success "MongoDB container created and started on port $MONGO_PORT"
    fi
}

start_elasticsearch() {
    print_status "Starting Elasticsearch..."
    
    if check_container "$ES_NAME"; then
        print_warning "Elasticsearch container already running"
    elif [ $? -eq 1 ]; then
        docker start "$ES_NAME"
        print_success "Elasticsearch container started"
    else
        docker run --name "$ES_NAME" \
            -p "$ES_PORT:$ES_PORT" \
            -e "discovery.type=single-node" \
            -e "xpack.security.enabled=false" \
            -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" \
            -d "$ES_IMAGE"
        print_success "Elasticsearch container created and started on port $ES_PORT"
    fi
}

start_kafka() {
    print_status "Starting Redpanda (Kafka)..."
    
    if check_container "$KAFKA_NAME"; then
        print_warning "Redpanda container already running"
    elif [ $? -eq 1 ]; then
        docker start "$KAFKA_NAME"
        print_success "Redpanda container started"
    else
        docker run --name "$KAFKA_NAME" \
            -p "$KAFKA_PORT:$KAFKA_PORT" \
            -p "$KAFKA_ADMIN_PORT:$KAFKA_ADMIN_PORT" \
            -d "$KAFKA_IMAGE" \
            redpanda start --smp 1 --memory 1G --overprovisioned --node-id 0 --check=false \
            --kafka-addr PLAINTEXT://0.0.0.0:$KAFKA_PORT \
            --advertise-kafka-addr PLAINTEXT://localhost:$KAFKA_PORT
        print_success "Redpanda container created and started on port $KAFKA_PORT"
    fi
}

stop_container() {
    local name=$1
    if check_container "$name"; then
        docker stop "$name"
        print_success "$name stopped"
    else
        print_warning "$name is not running"
    fi
}

remove_container() {
    local name=$1
    if docker ps -a --format '{{.Names}}' | grep -q "^${name}$"; then
        docker rm -f "$name"
        print_success "$name removed"
    else
        print_warning "$name does not exist"
    fi
}

show_status() {
    echo ""
    echo "Container Status:"
    echo "================="
    
    for container in "$MONGO_NAME" "$ES_NAME" "$KAFKA_NAME"; do
        if check_container "$container"; then
            status="${GREEN}●${NC} Running"
        elif [ $? -eq 1 ]; then
            status="${YELLOW}●${NC} Stopped"
        else
            status="${RED}●${NC} Not created"
        fi
        echo -e "  $container: $status"
    done
    echo ""
}

show_logs() {
    local name=$1
    if docker ps -a --format '{{.Names}}' | grep -q "^${name}$"; then
        docker logs --tail 50 -f "$name"
    else
        print_error "$name does not exist"
    fi
}

case "${1:-}" in
    start)
        echo "🐳 Starting all Kathena containers..."
        echo ""
        start_mongo
        start_elasticsearch
        start_kafka
        echo ""
        print_status "Waiting for services to be ready..."
        sleep 5
        show_status
        print_success "All containers started!"
        echo ""
        echo "You can now run the application with: ./gradlew bootRun"
        ;;
    
    stop)
        echo "🛑 Stopping all Kathena containers..."
        echo ""
        stop_container "$MONGO_NAME"
        stop_container "$ES_NAME"
        stop_container "$KAFKA_NAME"
        show_status
        ;;
    
    restart)
        echo "🔄 Restarting all Kathena containers..."
        echo ""
        $0 stop
        sleep 2
        $0 start
        ;;
    
    remove|clean)
        echo "🗑️  Removing all Kathena containers..."
        echo ""
        remove_container "$MONGO_NAME"
        remove_container "$ES_NAME"
        remove_container "$KAFKA_NAME"
        show_status
        print_success "All containers removed!"
        ;;
    
    status)
        show_status
        ;;
    
    logs)
        if [ -z "${2:-}" ]; then
            print_error "Please specify a container: mongo, es, or kafka"
            exit 1
        fi
        case "$2" in
            mongo)
                show_logs "$MONGO_NAME"
                ;;
            es|elasticsearch)
                show_logs "$ES_NAME"
                ;;
            kafka|redpanda)
                show_logs "$KAFKA_NAME"
                ;;
            *)
                print_error "Unknown container: $2"
                print_status "Available containers: mongo, es, kafka"
                exit 1
                ;;
        esac
        ;;
    
    *)
        echo "Kathena Container Management"
        echo "============================"
        echo ""
        echo "Usage: $0 {start|stop|restart|remove|status|logs}"
        echo ""
        echo "Commands:"
        echo "  start    - Start all containers (MongoDB, Elasticsearch, Redpanda)"
        echo "  stop     - Stop all running containers"
        echo "  restart  - Restart all containers"
        echo "  remove   - Remove all containers (will delete data)"
        echo "  status   - Show status of all containers"
        echo "  logs     - Show logs for a container (e.g., logs mongo)"
        echo ""
        echo "Examples:"
        echo "  $0 start              # Start all containers"
        echo "  $0 status             # Check container status"
        echo "  $0 logs mongo         # View MongoDB logs"
        echo "  $0 logs kafka         # View Redpanda logs"
        echo "  $0 remove             # Clean up all containers"
        exit 1
        ;;
esac

