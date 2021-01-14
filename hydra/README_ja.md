<!--
 Copyright 2020 Google LLC
 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
-->

# 概要
**FDA MyStudies** は、 [OAuth 2.0](https://oauth.net/2/) と [OpenID Connect](https://openid.net/connect/) (OIDC) *Certified&copy;* 技術として [ORY Hydra](https://www.ory.sh/hydra/) を使用しており、安全なトークンの生成と管理を容易にし、多様なIDプロバイダとの統合をサポートします。FDA MyStudiesプラットフォームは、Hydra APIを使用して電子メールとパスワードのログインを実装するために [SCIM](https://en.wikipedia.org/wiki/System_for_Cross-domain_Identity_Management) [`Auth server`](../auth-server) を使用しています。必要に応じてコードを変更することで、`Auth server` をOIDCに準拠したIDプロバイダに置き換えたり、補完したりすることができます。

[`Hydra server`](../hydra/) サーバーは、以下の機能を提供します。
1. クライアントの資格情報管理 (`client_id` と `client_secret`)
1. クライアントの認証情報の検証
1. トークンの生成・管理
1. トークンイントロスペクション
1. OAuth 2.0 フロー

 [`/hydra/Dockerfile`](./Dockerfile) は [Hydra container](https://github.com/ory/hydra) を構築し、 [`entrypoint.bash`](./entrypoint.bash) を使ってHydraを起動します。この entrypoint スクリプトは必要なすべての環境変数を設定し、バックエンドデータベースのスキーマを更新するために [`migrate`](https://www.ory.sh/hydra/docs/cli/hydra-migrate-sql/) を実行します。

# デプロイ
> **_注:_** Terraform と infrastructure-as-code を使用した **FDA MyStudies** プラットフォームの全体的なデプロイは、このコンポーネントをデプロイするために推奨されるアプローチです。半自動デプロイのステップバイステップガイドは、 [`deployment/`](/deployment) にあります。次の手順は、VM での手動デプロイが必要な場合に提供されています。Google Cloud インフラストラクチャが示されていますが、同等の代替インフラストラクチャを使用することもできます。デプロイする組織は、選択したサービスを構成する際に、アイデンティティとアクセス制御の選択を考慮することが重要です。手動デプロイを追求する場合、便利な順序は、[`hydra/`](/hydra)&rarr;[`auth-server/`](/auth-server/)&rarr;[`participant-datastore/`](/participant-datastore/)&rarr;[`participant-manager-datastore/`](/participant-manager-datastore/)&rarr;[`participant-manager/`](/participant-manager/)&rarr;[`study-datastore/`](/study-datastore/)&rarr;[`response-datastore/`](/response-datastore/)&rarr;[`study-builder/`](/study-builder/)&rarr;[`Android/`](/Android/)&rarr;[`iOS/`](/iOS/)


 [`Hydra`](/hydra) を手動でデプロイするには:
1. お好みのマシンタイプとOS（例えば、e2-mediumやDebian 10など）でCompute Engine VMインスタンスを [作成](https://cloud.google.com/compute/docs/instances/create-start-instance) し、 [static IPを予約します](https://cloud.google.com/compute/docs/ip-addresses/reserve-static-internal-ip-address) 。
1. [FDA MyStudies repository](https://github.com/GoogleCloudPlatform/fda-mystudies/) リポジトリから最新のコードをチェックアウトしてください。
1. MySQL v5.7を使用してクラウドSQLインスタンスを作成します ([instructions](https://cloud.google.com/sql/docs/mysql/create-instance))
1. クラウドSQLインスタンス上で `Hydra` データベースを構成する
    -    `Hydra` アプリケーションがこのインスタンスにアクセスするために使用するユーザーアカウントを作成します ([instructions](https://cloud.google.com/sql/docs/mysql/create-manage-users))
    -    [`create_hydra_db_script.sql`](sqlscript/create_hydra_db_script.sql) スクリプトで `hydra` という名前のデータベースを作成します ([instructions](https://cloud.google.com/sql/docs/mysql/import-export/importing#importing_a_sql_dump_file))
    -   VMと同じネットワークでデータベースのプライベートIP接続を有効にする ([instructions](https://cloud.google.com/sql/docs/mysql/configure-private-ip))
1. `https` を有効にするには、認証局から証明書を取得するか、自己署名の証明書を用意します。
    -   例えば、[`cert.config`](cert.config) をHydraデプロイメントのIPまたはドメインで設定し、`openssl req -newkey rsa:2048 -x509 -nodes -days 365 -config cert.config -keyout mystudies-private.key -out mystudies-cert.pem` を実行することで、自己署名証明書を生成することができます。
1. 例えば、`export SYSTEM_SECRET=$(export LC_CTYPE=C; cat /dev/urandom | tr -dc 'a-zA-Z0-9' | fold -w 32 | head -n 1)` を使用して [system secret](https://www.ory.sh/hydra/docs/configure-deploy/#deploy-ory-hydra) を設定します(このsecretはHydraデータベースを暗号化するために使用され、毎回同じ値にする必要があります)。
1. [Hydra](https://github.com/ory/hydra) をVMにデプロイする
    -   ルートディレクトリ `fda-mystudies/` から `sudo docker build -t hydra-image hydra` を使って Docker イメージを作成します（[Docker をインストール](https://docs.docker.com/engine/install/debian/) する必要があるかもしれません）
    -    Docker 環境ファイル [`variables.env`](variables.env) をデプロイ用の値で更新します。
    -    `sudo docker run --detach -v ~/certs:/certs --env-file variables.env -p 4444:4444 -p 4445:4445 --name hydra hydra-image` を使用してVM上でコンテナを実行します。
1. アプリケーションが動作しているかどうかをテストする `curl -k https://0.0.0.0:4445/health/ready`

# Hydra クライアントの設定

FDA MyStudies プラットフォームのコンポーネントには、`client_id` と `client_secret` が設定されています。各コンポーネントの grant type と値の例を以下の表に示します。 `Auth server`、 `Participant manager`、 `Android` と `iOS` アプリケーションは、1つの認証情報のセットを共有しています。 `client_secret` の値を生成して管理する責任があります。これらの値は、POSTリクエストを行うことで `Hydra` で設定することができます。

```shell
 curl    --location --request POST ‘<HYDRA_ADMIN_BASE_URL>/clients’ \
         --header 'Content-Type: application/json' \
         --header 'Accept: application/json' \
         --data-raw '{
         "client_id": "<CLIENT_ID>",
         "client_name": "<CLIENT_NAME>",
         "client_secret": "<CLIENT_SECRET>",
         "client_secret_expires_at": 0,
         "grant_types": ["authorization_code","refresh_token","client_credentials"],
         "token_endpoint_auth_method": "client_secret_basic",
         "redirect_uris": ["<AUTH_SERVER_BASE_URL>/callback"]
         }’
```

例えば、*<HYDRA_ADMIN_BASE_URL>* は `https://10.128.0.2:4445` 、*<AUTH_SERVER_BASE_URL>* は `https://10.128.0.3` とすることができます。これらのリソースを効率的に作成する方法については、[`/deployment/scripts/register_clients_in_hydra.sh`](/deployment/scripts/register_clients_in_hydra.sh) を参照してください。

Platform component | Grant type | client_id | client_name
----------------------------|---------------|---------------|-------------------
[`Participant datastore user module`](../participant-datastore/user-mgmt-module/) | `client_credentials` | `participant_user_datastore_hydra_client` | `participant_user_datastore`
[`Participant datastore enrollment module`](../participant-datastore/enroll-mgmt-module/) | `client_credentials` | `participant_enroll_datastore_hydra_client` | `participant_enroll_datastore`
[`Participant datastore consent module`](../participant-datastore/consent-mgmt-module/) | `client_credentials` | `participant_consent_datastore_hydra_client` | `participant_consent_datastore`
[`Participant manager datastore`](../participant-manager-datastore) | `client_credentials` | `participant_manager_datastore_hydra_client` | `participant_manager_datastore`
[`Study builder`](../study-builder/) | `client_credentials` | `study_builder_hydra_client` | `study_builder`
[`Study datastore`](../study-datastore/) | `client_credentials` | `study_datastore_hydra_client` | `study_datastore`
[`Auth server`](../auth-server/)<br/>[`Participant manager`](../participant-manager/)<br/>[`iOS mobile application`](../iOS/)<br/>[`Android mobile application`](../Android/) | `client_credentials`<br/>`refresh_token`<br/>`authorization_code` | `mystudies_hydra_client` | `mystudies`

***
<p align="center">Copyright 2020 Google LLC</p>
