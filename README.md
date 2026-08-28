# FleetOps - Trip & Telemetry Service

Part of the **FleetOps Fleet & Logistics Dispatch System**, submitted for the
Enterprise Cloud Architecture (ITS 2130) capstone project.

## Student Information
- **Name:** K.D. Chanuth Dewhan
- **Student ID:** 241722017
- **Slack Handle:** @chanuthdewhan
- **GCP Project ID:** fleet-ops-506803

## Project Description
Manages the delivery trip lifecycle for FleetOps — starting a trip once a
driver and vehicle are assigned, logging location and status events as a
delivery progresses, capturing proof-of-delivery photos, and completing a
trip. On completion, this service communicates directly with the Order &
Dispatch Service (via Eureka-discovered `RestClient` calls, not through the
API Gateway) to transition the order through `IN_TRANSIT` to `DELIVERED`,
and notifies the Notification Service — demonstrating genuine inter-service
communication as required by the platform architecture.

Trip data is stored in MongoDB, chosen specifically because a trip's GPS and
status events form a naturally document-shaped, append-as-you-go structure
rather than a relational one. Proof-of-delivery images are uploaded to
Google Cloud Storage and served back to clients through an authenticated
proxy endpoint, since the storage bucket is not publicly accessible.

## Technology Stack
- Java 25
- Spring Boot 4.1
- Spring Data MongoDB
- Spring Cloud Config Client, Eureka Client
- Google Cloud Storage client library
- Spring RestClient (load-balanced, Eureka-discovered inter-service calls)
- MapStruct, Lombok
- RFC 9457 Problem Details for structured error responses

## Setup / Getting Started

```bash
git clone https://github.com/chanuthdewhan/fleetops-trip-telemetry-service.git
cd fleetops-trip-telemetry-service
./mvnw spring-boot:run
```

Runs on port `8001` locally. Requires `fleetops-service-registry` and
`fleetops-config-server` running first, along with a local MongoDB instance.
By default, uses local disk storage for file uploads (`dev` Spring profile);
in production, switches automatically to Google Cloud Storage.

## Key Endpoints
- `POST /api/v1/trips` — start a trip
- `GET /api/v1/trips/{id}`, `GET /api/v1/trips?orderId={id}`
- `POST /api/v1/trips/{id}/events` — log a GPS/status event
- `POST /api/v1/trips/{id}/proof-of-delivery` — upload delivery photo
- `GET /api/v1/trips/{id}/proof-of-delivery/file` — authenticated file retrieval
- `PATCH /api/v1/trips/{id}/complete`

## Live Deployment
- **GCP Project ID:** fleet-ops-506803
- **Region:** asia-southeast1
- **Deployment model:** IaaS — Compute Engine, managed via PM2
- **Cloud Storage bucket:** fleetops-cloud-storage
- **Accessed via API Gateway:** http://34.21.225.166:80