.DEFAULT_GOAL := help

# Ce service se lance exclusivement via le docker-compose.yaml du projet
# orchestrateur (GatchaApi), dont ce dépôt est un sous-module.
# Il n'y a plus de docker-compose local : toutes les cibles ci-dessous
# pilotent la stack racine, restreinte à ce service.
COMPOSE = docker compose -f ../docker-compose.yaml
SVC = api-authentification

.PHONY: help up down down-v reset-volumes ps logs build restart

help: ## Show this help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-28s\033[0m %s\n", $$1, $$2}'

up: ## Build (if needed) and start this service (via the orchestrator stack)
	$(COMPOSE) up -d --build $(SVC)

down: ## Stop and remove this service (keeps volumes)
	$(COMPOSE) down $(SVC)

down-v: ## Stop this service and remove the stack volumes (destructive: wipes DB data)
	$(COMPOSE) down -v $(SVC)

reset-volumes: ## Reset volumes and restart this service fresh
	$(COMPOSE) down -v $(SVC)
	$(COMPOSE) up -d $(SVC)

ps: ## Show status of this service's container
	$(COMPOSE) ps $(SVC)

logs: ## Tail logs for this service
	$(COMPOSE) logs -f $(SVC)

build: ## Build this service's image
	$(COMPOSE) build $(SVC)

restart: ## Rebuild and restart this service (config/code change)
	$(COMPOSE) down $(SVC)
	$(COMPOSE) up -d --build $(SVC)
