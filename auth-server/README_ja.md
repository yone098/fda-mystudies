<!--
 Copyright 2020 Google LLC
 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
-->

# 概要
**FDAのMyStudies** [`Auth server`](../auth-server/) は、MyStudiesプラットフォームの様々なクライアントアプリケーションのための集中認証メカニズムです。

クライアントアプリケーションは
1. [`Android mobile application`](../Android/)
1. [`iOS mobile application`](../iOS/)
1. [`Participant manager`](../participant-manager/)

`Auth server` は以下の機能を提供する。
1. ユーザー登録
1. ユーザー認証情報の管理
1. ユーザー認証
1. ユーザーログアウト
1. サーバ間認証のサポート

`Auth server` のID管理アプリケーションは、ユーザのログインと同意のフローを実装するSpring Bootアプリケーションとして構築されています。これは、トークンの生成と管理のためにデプロイされた [ORY Hydra](https://www.ory.sh/hydra/) のインスタンスと統合します。

# デプロイ
> **_注:_** Terraform と infrastructure-as-code を使用した **FDA MyStudies** プラットフォームの全体的なデプロイは、このコンポーネントをデプロイするために推奨されるアプローチです。半自動デプロイのステップバイステップガイドは、 [`deployment/`](/deployment) にあります。次の手順は、VM での手動デプロイが必要な場合に提供されています。Google Cloud インフラストラクチャが示されていますが、同等の代替インフラストラクチャを使用することもできます。デプロイする組織は、選択したサービスを構成する際に、アイデンティティとアクセス制御の選択を考慮することが重要です。手動デプロイを追求する場合、便利な順序は、[`hydra/`](/hydra)&rarr;[`auth-server/`](/auth-server/)&rarr;[`participant-datastore/`](/participant-datastore/)&rarr;[`participant-manager-datastore/`](/participant-manager-datastore/)&rarr;[`participant-manager/`](/participant-manager/)&rarr;[`study-datastore/`](/study-datastore/)&rarr;[`response-datastore/`](/response-datastore/)&rarr;[`study-builder/`](/study-builder/)&rarr;[`Android/`](/Android/)&rarr;[`iOS/`](/iOS/)

 [`Auth server`](/auth-server/) を手動でデプロイするには:
1. お好みのマシンタイプとOS（例えば、e2-mediumやDebian 10など）でCompute Engine VMインスタンスを [作成](https://cloud.google.com/compute/docs/instances/create-start-instance) し、 [static IPを予約します](https://cloud.google.com/compute/docs/ip-addresses/reserve-static-internal-ip-address) 。
1. VM インスタンスが `Stackdriver Logging API` の書き込み [access scope](https://cloud.google.com/compute/docs/access/service-accounts#accesscopesiam) をオン（デフォルトではオン）に設定し、VM の [service account](https://cloud.google.com/compute/docs/access/service-accounts#default_service_account) が [`Logs Writer`](https://cloud.google.com/logging/docs/access-control) ロールを持っていること（デフォルトではオフ）を確認してください。
1. [FDA MyStudies repository](https://github.com/GoogleCloudPlatform/fda-mystudies/) リポジトリから最新のコードをチェックアウトしてください。
1. MySQL v5.7を使用してクラウドSQLインスタンスを作成します ([instructions](https://cloud.google.com/sql/docs/mysql/create-instance))
1. Cloud SQL インスタンス上で `Auth server` データベースを構成する。
    -    `Auth server` アプリケーションがこのインスタンスにアクセスするために使用するユーザアカウントを作成します ([instructions](https://cloud.google.com/sql/docs/mysql/create-manage-users))
    -    [`mystudies_oauth_server_hydra_db_script.sql`](sqlscript/mystudies_oauth_server_hydra_db_script.sql) スクリプトで `oauth_server_hydra` という名前のデータベースを作成します ([instructions](https://cloud.google.com/sql/docs/mysql/import-export/importing#importing_a_sql_dump_file))
    -   VMと同じネットワークでデータベースのプライベートIP接続を有効にする ([instructions](https://cloud.google.com/sql/docs/mysql/configure-private-ip))
1. `https` を有効にするには、認証局から証明書を取得するか、自己署名の証明書を用意します。
    -   例えば、`Hydra` デプロイ時に作成した証明書の *Subject Alternative Names* のリストにこの VM インスタンスのホスト名を含めた場合、`openssl pkcs12 -export -password pass:changeit -out mystudies-cert. p12 -inkey <path_to_mystudies-private.key> -in <path_to_mystudies-cert.pem>` を使用して、このデプロイメントで使用する証明書を変換します (ここで、`mystudies-private.key` と `mystudies-cert.pem` は `Hydra` デプロイメントで作成した証明書ファイルです)
1. `Auth server` コンテナをVMにデプロイする。
    -    Dockerイメージを作成するには、`sudo mvn -B package -Pprod com.google.cloud.tools:jib-maven-plugin:2.5.2:dockerBuild -Dimage=auth-server-image` で `auth-server/` ディレクトリから作成します。( [Docker](https://docs.docker.com/engine/install/debian/) とMavenをインストールする必要があるかもしれません。例えば `sudo apt install maven` )
    -    Docker環境ファイル [`variables.env`](variables.env) をデプロイメント用の [`application.properties`](oauth-scim-service/src/main/resources/application.properties) ファイルの値で更新します。
    -    VM上で `sudo docker run --detach -v ~/certs:/certs --env-file variables.env -p 443:8080 --name auth-server auth-server-image` を使用してコンテナを実行します( `http` を使用している場合はポートを 80 に調整する必要があるかもしれません)
    -    `Hydra` インスタンスが自己署名証明書を使用している場合は、その証明書をコンテナのキーストアに追加します。例えば `sudo docker exec -it auth-server bash -c "openssl s_client -connect <your_hydra_instance> | sed -ne '/-BEGIN CERTIFICATE/,/END CERTIFICATE/p' > hydra.crt; keytool -import -trustcacerts -alias hydra -file hydra.crt -keystore /usr/local/openjdk-11/lib/security/cacerts -storepass changeit"` でコンテナを再起動した後、`sudo docker restart auth-server` でコンテナを再起動します。
1. アプリケーションが動作しているかどうかをテストします `curl -k https://0.0.0.0/auth-server/healthCheck`
1. 指定したロギングディレクトリにあるアプリケーションログを確認するか、`sudo docker logs auth-server` で、監査ログは [Cloud Logging](https://cloud.google.com/logging)で確認できます。

***
<p align="center">Copyright 2020 Google LLC</p>
