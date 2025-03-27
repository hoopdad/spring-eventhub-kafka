# Authenticating Container App to Event Hub with Kafka API

## pre-requisites

- Azure Container Registry (can be adapted to your container registry)
- Azure Event Hubs
- Azure Container Apps Environment
- Azure Container App
- User Assigned Managed Identity assigned to Azure Container App, with Azure Event Hubs Data Owner on your Event Hub and AcrPull & Reader on the ACR
- No env variables set by me in the ACA so that DefaultAzureCredential can pick the right one for Managed Identity.

## To Start

- Replace \<managedIdentityClientId\> in CustomAuthenticateCallbackHander.java
- TestDataReporter is set up so you can run this as a container and it will retry every 5 minutes
- Default topic is TOPIC="test" in TestProducer.java. Change if you need to.
- Config file is loaded from filesystem not jar, tweak that if you prefer or just add the config file to your DOckerfile with given example
- IN producer.config, change \<your event hub namespace\> to the value for yours. It builds a URL which is displayed on the Event Hub Namespace overview page.

## Build and Deploy

- in build.sh, set ACR_NAME="\<my acr name\>" and IMAGE_NAME="\<app name\>"

