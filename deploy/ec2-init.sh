#!/usr/bin/env bash
# EC2インスタンス起動時のuser-dataとして実行する想定のスクリプト(Amazon Linux 2023, 初回起動時のみ)。
# Docker / Docker Composeプラグインの導入、swap作成、/opt/bukuroの用意、
# systemdサービス(bukuro-app.service)の登録までを行う。
#
# 意図的にやらないこと:
#   /opt/bukuro/.env の作成(DB_PASSWORD等の機密情報を含むため)。
#   初回のみ、SSM Session Managerで接続し deploy/.env.example を参考に手動作成すること。
#   (手順は deploy/aws-setup-commands.md を参照)
set -euo pipefail

dnf install -y docker
systemctl enable --now docker

# Docker Composeプラグイン(AL2023の標準リポジトリには無いためGitHub Releaseから取得)
ARCH=$(uname -m)
COMPOSE_VERSION="v2.29.7"
mkdir -p /usr/libexec/docker/cli-plugins
curl -fsSL "https://github.com/docker/compose/releases/download/${COMPOSE_VERSION}/docker-compose-linux-${ARCH}" \
  -o /usr/libexec/docker/cli-plugins/docker-compose
chmod +x /usr/libexec/docker/cli-plugins/docker-compose

# 2GBメモリ想定インスタンス向けのswap(1GB)。アプリ+DB+Caddy同居によるOOMを防止
if [ ! -f /swapfile ]; then
  fallocate -l 1G /swapfile
  chmod 600 /swapfile
  mkswap /swapfile
  swapon /swapfile
  echo '/swapfile none swap sw 0 0' >> /etc/fstab
fi

mkdir -p /opt/bukuro

# systemdユニットを配置(内容は deploy/bukuro-app.service と同一に保つこと)
cat <<'EOS' > /etc/systemd/system/bukuro-app.service
[Unit]
Description=Bukuro app (docker compose)
Requires=docker.service
After=docker.service network-online.target
Wants=network-online.target

[Service]
Type=oneshot
RemainAfterExit=yes
WorkingDirectory=/opt/bukuro
ExecStart=/usr/bin/docker compose -f /opt/bukuro/docker-compose.prod.yml up -d
ExecStop=/usr/bin/docker compose -f /opt/bukuro/docker-compose.prod.yml down
TimeoutStartSec=300

[Install]
WantedBy=multi-user.target
EOS

systemctl daemon-reload
# docker-compose.prod.yml等はまだ存在しない(初回デプロイ時にS3から配置される)ため
# enableのみ行い、startは初回デプロイ(deploy.sh)に委ねる。
systemctl enable bukuro-app.service

echo "ec2-init.sh completed. Next: SSM Session Managerで接続し /opt/bukuro/.env を作成してください。"
