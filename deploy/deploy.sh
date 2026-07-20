#!/usr/bin/env bash
# GitHub ActionsからAWS SSM Send-Command(AWS-RunShellScript)経由でEC2上で実行される。
# 環境変数 AWS_REGION / S3_BUCKET / ECR_REPOSITORY はSSMコマンドのパラメータとして渡す想定。
#
# /opt/bukuro/.env はこのスクリプトでは一切生成・上書きしない(初回セットアップ時に
# 手動作成した機密情報を保護するため)。
set -euo pipefail

: "${AWS_REGION:?AWS_REGION is required}"
: "${S3_BUCKET:?S3_BUCKET is required}"
: "${ECR_REPOSITORY:?ECR_REPOSITORY is required}"

BUKURO_DIR=/opt/bukuro
mkdir -p "$BUKURO_DIR"
cd "$BUKURO_DIR"

if [ ! -f .env ]; then
  echo "ERROR: ${BUKURO_DIR}/.env が存在しません。初回セットアップ手順(deploy/aws-setup-commands.md)に従い手動作成してください。" >&2
  exit 1
fi

echo "Syncing deploy files from s3://${S3_BUCKET}/deploy/ ..."
aws s3 cp "s3://${S3_BUCKET}/deploy/docker-compose.prod.yml" ./docker-compose.prod.yml
aws s3 cp "s3://${S3_BUCKET}/deploy/Caddyfile" ./Caddyfile
aws s3 cp "s3://${S3_BUCKET}/deploy/schema.sql" ./schema.sql

echo "Logging in to ECR..."
aws ecr get-login-password --region "$AWS_REGION" \
  | docker login --username AWS --password-stdin "${ECR_REPOSITORY%%/*}"

echo "Pulling latest image..."
docker compose -f docker-compose.prod.yml pull

echo "Starting containers (up -d)..."
docker compose -f docker-compose.prod.yml up -d

echo "Pruning old images..."
docker image prune -f

echo "Deploy completed."
