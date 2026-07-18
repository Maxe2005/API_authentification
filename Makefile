.DEFAULT_GOAL := help

.PHONY: help up down down-v reset-volumes ps logs build \
	global-up global-down global-down-v global-reset-volumes global-ps global-logs global-build global-restart \

help: ## Show this help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-28s\033[0m %s\n", $$1, $$2}'

# ===== Self-contained =====

up: ## Build (if needed) and start the service
	docker compose up -d --build

down: ## Stop and remove this service (keeps volumes)
	docker compose down

down-v: ## Stop and remove this service and its volumes (destructive: wipes all DB/minio data)
	docker compose down -v

reset-volumes: ## Reset all volumes and restart the service fresh
	docker compose down -v
	docker compose up -d

ps: ## Show status of all containers in the service
	docker compose ps

logs: ## Tail logs for this service
	docker compose logs -f

build: ## Build this service images
	docker compose build

# ===== As a Submodule =====

global-up: ## Start the service (if not already running)
	docker compose -f ../docker-compose.yaml up -d --build api-authentification

global-down: ## Stop the service (if running)
	docker compose -f ../docker-compose.yaml down api-authentification

global-down-v: ## Stop the service and remove its volumes (destructive: wipes all DB/minio data)
	docker compose -f ../docker-compose.yaml down -v api-authentification

global-reset-volumes: ## Reset all volumes and restart the service fresh
	docker compose -f ../docker-compose.yaml down -v api-authentification
	docker compose -f ../docker-compose.yaml up -d api-authentification

global-ps: ## Show status of all containers in the service
	docker compose -f ../docker-compose.yaml ps api-authentification

global-logs: ## Tail logs for this service
	docker compose -f ../docker-compose.yaml logs -f api-authentification

global-build: ## Build this service images
	docker compose -f ../docker-compose.yaml build api-authentification

global-restart: ## Rebuild and restart this service (config/code change)
	docker compose -f ../docker-compose.yaml down api-authentification
	docker compose -f ../docker-compose.yaml up -d --build api-authentification
