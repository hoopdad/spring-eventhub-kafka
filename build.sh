#!/bin/bash
ACR_NAME="<my acr name>"
IMAGE_NAME="<app name>"
TAG="latest"

# Login to ACR

mvn clean install -U

mvn clean package -DskipTests && \
    docker build -t $IMAGE_NAME:$TAG . && \
    docker tag $IMAGE_NAME:$TAG $ACR_NAME.azurecr.io/$IMAGE_NAME:$TAG && \
    az acr login --name $ACR_NAME && \
    docker push $ACR_NAME.azurecr.io/$IMAGE_NAME:$TAG

echo `date`