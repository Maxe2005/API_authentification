# API Combat

## Description

The Authentication API for the Gatcha API project.

## Installation

This service is launched **exclusively** through the orchestrator repo [GatchaApi](https://github.com/Maxe2005/GatchaApi), which wires it into the full stack via its root `docker-compose.yaml` (there is no standalone `docker-compose.yml` in this repo anymore).

```bash
$ git clone --recurse-submodules https://github.com/Maxe2005/GatchaApi.git
$ cd GatchaApi
$ cp .env.exemple .env   # then fill in AUTH_SECRET / AUTH_SALT (and the optional DEFAULT_* accounts)
$ make up                # or: docker compose up -d --build
```

All configuration (Mongo connection, `AUTH_SECRET`, `AUTH_SALT`, optional default accounts) is provided by the orchestrator's `docker-compose.yaml` and root `.env`. The `.env.example` file in this repo only documents the variables needed to run the app locally with `./mvnw spring-boot:run` (against the Mongo exposed by the root stack on `localhost:27019`).

## Usage

You can access the API documentation at `http://localhost:8081/swagger-ui/index.html` (host port mapped by the orchestrator stack).

> [!WARNING]
> Do not deploy this project for production environments.
