# AWSリソース構築手順(ユーザー実行用)

このファイルはClaude Codeの実行環境にAWS認証情報がないため、**ユーザー自身のAWSアカウントで**手動実行するコマンド集です。AWS CLI v2がローカルにインストール・認証済み(`aws sts get-caller-identity`が通る)であることを前提とします。

上から順に実行してください。各コマンドは冪等ではないため、既に作成済みのリソースがあれば読み替えてください。

## 0. 変数設定

```bash
export AWS_REGION=ap-northeast-1
export AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
export GITHUB_REPO="ichi-269/Bukuro"          # OIDC信頼ポリシーで使う owner/repo
export ECR_REPO_NAME="bukuro"
export S3_BUCKET="bukuro-deploy-${AWS_ACCOUNT_ID}"   # S3バケット名はグローバルに一意である必要あり
export SG_NAME="bukuro-ec2-sg"
export EC2_ROLE_NAME="bukuro-ec2-role"
export GHA_ROLE_NAME="bukuro-github-actions-role"
export PROJECT_TAG="bukuro"
```

## 1. ECRリポジトリ作成

```bash
aws ecr create-repository \
  --repository-name "$ECR_REPO_NAME" \
  --region "$AWS_REGION" \
  --image-scanning-configuration scanOnPush=true

export ECR_REPOSITORY="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${ECR_REPO_NAME}"
echo "$ECR_REPOSITORY"
```

## 2. S3バケット作成(デプロイ設定配置用)

```bash
aws s3api create-bucket \
  --bucket "$S3_BUCKET" \
  --region "$AWS_REGION" \
  --create-bucket-configuration LocationConstraint="$AWS_REGION"

aws s3api put-public-access-block \
  --bucket "$S3_BUCKET" \
  --public-access-block-configuration BlockPublicAcls=true,IgnorePublicAcls=true,BlockPublicPolicy=true,RestrictPublicBuckets=true
```

## 3. GitHub Actions用OIDC Provider作成

同一AWSアカウントで他リポジトリ用に作成済みの場合はスキップしてください(1アカウントにつき1つでよい)。

```bash
aws iam create-open-id-connect-provider \
  --url https://token.actions.githubusercontent.com \
  --client-id-list sts.amazonaws.com \
  --thumbprint-list 6938fd4d98bab03faadb97b34396831e3780aea1
```

## 4. GitHub Actions用IAMロール作成(OIDCフェデレーション、mainブランチのみ許可)

```bash
cat > /tmp/gha-trust-policy.json <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Federated": "arn:aws:iam::${AWS_ACCOUNT_ID}:oidc-provider/token.actions.githubusercontent.com"
      },
      "Action": "sts:AssumeRoleWithWebIdentity",
      "Condition": {
        "StringEquals": {
          "token.actions.githubusercontent.com:aud": "sts.amazonaws.com"
        },
        "StringLike": {
          "token.actions.githubusercontent.com:sub": "repo:${GITHUB_REPO}:ref:refs/heads/main"
        }
      }
    }
  ]
}
EOF

aws iam create-role \
  --role-name "$GHA_ROLE_NAME" \
  --assume-role-policy-document file:///tmp/gha-trust-policy.json

cat > /tmp/gha-permissions-policy.json <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "EcrAuth",
      "Effect": "Allow",
      "Action": "ecr:GetAuthorizationToken",
      "Resource": "*"
    },
    {
      "Sid": "EcrPush",
      "Effect": "Allow",
      "Action": [
        "ecr:BatchCheckLayerAvailability",
        "ecr:PutImage",
        "ecr:InitiateLayerUpload",
        "ecr:UploadLayerPart",
        "ecr:CompleteLayerUpload",
        "ecr:BatchGetImage"
      ],
      "Resource": "arn:aws:ecr:${AWS_REGION}:${AWS_ACCOUNT_ID}:repository/${ECR_REPO_NAME}"
    },
    {
      "Sid": "S3DeployUpload",
      "Effect": "Allow",
      "Action": ["s3:PutObject", "s3:GetObject"],
      "Resource": "arn:aws:s3:::${S3_BUCKET}/deploy/*"
    },
    {
      "Sid": "Ec2Describe",
      "Effect": "Allow",
      "Action": "ec2:DescribeInstances",
      "Resource": "*"
    },
    {
      "Sid": "Ec2Start",
      "Effect": "Allow",
      "Action": "ec2:StartInstances",
      "Resource": "arn:aws:ec2:${AWS_REGION}:${AWS_ACCOUNT_ID}:instance/*",
      "Condition": {
        "StringEquals": { "ec2:ResourceTag/Project": "${PROJECT_TAG}" }
      }
    },
    {
      "Sid": "SsmSendCommand",
      "Effect": "Allow",
      "Action": "ssm:SendCommand",
      "Resource": [
        "arn:aws:ec2:${AWS_REGION}:${AWS_ACCOUNT_ID}:instance/*",
        "arn:aws:ssm:${AWS_REGION}::document/AWS-RunShellScript"
      ],
      "Condition": {
        "StringEquals": { "ec2:ResourceTag/Project": "${PROJECT_TAG}" }
      }
    },
    {
      "Sid": "SsmObserve",
      "Effect": "Allow",
      "Action": [
        "ssm:GetCommandInvocation",
        "ssm:ListCommandInvocations",
        "ssm:DescribeInstanceInformation"
      ],
      "Resource": "*"
    }
  ]
}
EOF

aws iam put-role-policy \
  --role-name "$GHA_ROLE_NAME" \
  --policy-name bukuro-github-actions-permissions \
  --policy-document file:///tmp/gha-permissions-policy.json

export GHA_ROLE_ARN=$(aws iam get-role --role-name "$GHA_ROLE_NAME" --query 'Role.Arn' --output text)
echo "$GHA_ROLE_ARN"
```

## 5. EC2用IAMロール・インスタンスプロファイル作成

```bash
cat > /tmp/ec2-trust-policy.json <<'EOF'
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": { "Service": "ec2.amazonaws.com" },
      "Action": "sts:AssumeRole"
    }
  ]
}
EOF

aws iam create-role \
  --role-name "$EC2_ROLE_NAME" \
  --assume-role-policy-document file:///tmp/ec2-trust-policy.json

# SSM Session Manager / Send-Commandを受け付けるための管理ポリシー
aws iam attach-role-policy \
  --role-name "$EC2_ROLE_NAME" \
  --policy-arn arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore

cat > /tmp/ec2-permissions-policy.json <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "EcrAuth",
      "Effect": "Allow",
      "Action": "ecr:GetAuthorizationToken",
      "Resource": "*"
    },
    {
      "Sid": "EcrPull",
      "Effect": "Allow",
      "Action": [
        "ecr:BatchCheckLayerAvailability",
        "ecr:GetDownloadUrlForLayer",
        "ecr:BatchGetImage"
      ],
      "Resource": "arn:aws:ecr:${AWS_REGION}:${AWS_ACCOUNT_ID}:repository/${ECR_REPO_NAME}"
    },
    {
      "Sid": "S3DeployRead",
      "Effect": "Allow",
      "Action": "s3:GetObject",
      "Resource": "arn:aws:s3:::${S3_BUCKET}/deploy/*"
    }
  ]
}
EOF

aws iam put-role-policy \
  --role-name "$EC2_ROLE_NAME" \
  --policy-name bukuro-ec2-permissions \
  --policy-document file:///tmp/ec2-permissions-policy.json

aws iam create-instance-profile --instance-profile-name "$EC2_ROLE_NAME"
aws iam add-role-to-instance-profile \
  --instance-profile-name "$EC2_ROLE_NAME" \
  --role-name "$EC2_ROLE_NAME"
```

## 6. セキュリティグループ作成(80番のみ許可、22番は開放しない)

```bash
export VPC_ID=$(aws ec2 describe-vpcs --filters Name=isDefault,Values=true --query 'Vpcs[0].VpcId' --output text)

export SG_ID=$(aws ec2 create-security-group \
  --group-name "$SG_NAME" \
  --description "Bukuro EC2 (HTTP only, no SSH)" \
  --vpc-id "$VPC_ID" \
  --query 'GroupId' --output text)

aws ec2 authorize-security-group-ingress \
  --group-id "$SG_ID" \
  --protocol tcp --port 80 --cidr 0.0.0.0/0

echo "$SG_ID"
```

## 7. EC2インスタンス起動

```bash
# Amazon Linux 2023 (arm64/Graviton) の最新AMIをSSM Parameter Storeから取得
export AMI_ID=$(aws ssm get-parameters \
  --names /aws/service/ami-amazon-linux-latest/al2023-ami-kernel-default-arm64 \
  --region "$AWS_REGION" \
  --query 'Parameters[0].Value' --output text)

export INSTANCE_ID=$(aws ec2 run-instances \
  --region "$AWS_REGION" \
  --image-id "$AMI_ID" \
  --instance-type t4g.small \
  --iam-instance-profile Name="$EC2_ROLE_NAME" \
  --security-group-ids "$SG_ID" \
  --block-device-mappings '[{"DeviceName":"/dev/xvda","Ebs":{"VolumeSize":30,"VolumeType":"gp3"}}]' \
  --tag-specifications "ResourceType=instance,Tags=[{Key=Name,Value=bukuro},{Key=Project,Value=${PROJECT_TAG}}]" \
  --user-data file://deploy/ec2-init.sh \
  --query 'Instances[0].InstanceId' --output text)

echo "$INSTANCE_ID"
```

インスタンス起動後、数分待ってからSSM管理下に入ったことを確認します(SSHではなくSSMのみでアクセスするため、ここで疎通確認しておくと安心です):

```bash
aws ssm describe-instance-information \
  --filters "Key=InstanceIds,Values=${INSTANCE_ID}" \
  --region "$AWS_REGION"
```

## 8. 初回のみ: `.env`を手動作成

`ec2-init.sh`は機密情報を含む`.env`を自動生成しません。SSM Session Managerで接続し、`deploy/.env.example`を参考に一度だけ作成してください。

```bash
aws ssm start-session --target "$INSTANCE_ID" --region "$AWS_REGION"
```

接続後(EC2上のシェルで):

```bash
sudo mkdir -p /opt/bukuro
sudo tee /opt/bukuro/.env > /dev/null <<'EOF'
ECR_REPOSITORY=<手順1で確認した ECR_REPOSITORY の値>
DB_ROOT_PASSWORD=<強力なランダム文字列>
DB_USERNAME=bukuro_app
DB_PASSWORD=<強力なランダム文字列>
EOF
exit
```

## 9. GitHub Secrets/Variablesへの設定

GitHubリポジトリの Settings > Secrets and variables > Actions > **Variables**タブに、以下を登録してください(すべて非機密情報。OIDCフェデレーションのため長期のAWSアクセスキーは一切不要です)。

| 変数名 | 値 |
|---|---|
| `AWS_REGION` | `ap-northeast-1` |
| `AWS_ROLE_ARN` | 手順4で出力された `$GHA_ROLE_ARN` |
| `ECR_REPOSITORY` | 手順1で出力された `$ECR_REPOSITORY` |
| `S3_BUCKET` | `$S3_BUCKET` |
| `EC2_INSTANCE_ID` | 手順7で出力された `$INSTANCE_ID` |

## 10. 動作確認

- mainブランチにpushし、GitHub Actions(`deploy.yml`)が成功することを確認
- `aws ec2 describe-instances --instance-ids $INSTANCE_ID --query 'Reservations[0].Instances[0].PublicIpAddress' --output text` でパブリックIPを確認し、ブラウザで`http://<IP>/`にアクセス
- `aws ec2 stop-instances --instance-ids $INSTANCE_ID` → 後日 `aws ec2 start-instances --instance-ids $INSTANCE_ID` で、手動操作なしにサービスが復帰し、投稿データが保持されていることを確認
