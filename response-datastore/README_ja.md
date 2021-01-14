<!--
 Copyright 2020 Google LLC
 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
-->
 
# 概要
**FDA MyStudies** [`Response datastore`](/response-datastore/)は、匿名化された治験のレスポンスおよびアクティビティデータを保存および管理するためのAPIを提供します。 `Response datastore`は、[`Android`](/Android)および[`iOS`](/iOS)モバイルアプリケーションから匿名化された治験レスポンスを受信し、ドキュメント指向データベースに書き込まれます。 研究者やデータサイエンティストは、このデータベースにアクセスして、治験データの分析を実行したり、ダウンストリームシステムにエクスポートしたりします。 `Response datastore`は、リレーショナルデータベースに書き込まれる匿名化されたアクティビティデータも受信します。 このアプリケーションは、SpringBootアプリケーションとして構築されています。 バックエンドデータベースは、レスポンスデータ用のCloud Firestoreと、アクティビティデータ用のMySQLです。 `Response datastore`は、基本認証`client_id`および`client_secret`を使用します。

`Responsedatastore`クライアントアプリケーションは次のとおりです。

1. [`Androidモバイルアプリケーション`](/Android/)
1. [`iOSモバイルアプリケーション`](/iOS/)
1. [`Study builder`](/study-builder)
1. [`Participant datastore`](/participant-datastore/)
 
# デプロイ
> **_注_**：TerraformとInfrastructure-as-Codeを使用した**FDA MyStudies**プラットフォームの全体的なデプロイは、このコンポーネントをデプロイするための推奨されるアプローチです。 半自動デプロイのステップバイステップガイドは、[`deployment/`](/deployment)ディレクトリにあります。 次の手順は、VMへの手動デプロイが必要な場合に提供されます。 Google Cloudインフラストラクチャが示されていますが、同等の代替インフラストラクチャを使用することもできます。 デプロイする組織にとって、選択したサービスを構成するときに行われるIDとアクセス制御の選択を考慮することが重要です。 手動デプロイを行う場合、便利なシーケンスは [`hydra/`](/hydra)&rarr;[`auth-server/`](/auth-server/)&rarr;[`participant-datastore/`](/participant-datastore/)&rarr;[`participant-manager-datastore/`](/participant-manager-datastore/)&rarr;[`participant-manager/`](/participant-manager/)&rarr;[`study-datastore/`](/study-datastore/)&rarr;[`response-datastore/`](/response-datastore/)&rarr;[`study-builder/`](/study-builder/)&rarr;[`Android/`](/Android/)&rarr;[`iOS/`](/iOS/) です。

[`Response datastore`](/response-datastore/)を手動でデプロイするには：
1. Cloud Datastoreの[静的IP](https://cloud.google.com/compute/docs/ip-addresses/reserve-static-internal-ip-address)と読み取り/書き込み[アクセススコープ](https://cloud.google.com/compute/docs/access/service-accounts#accesscopesiam)を使用してCompute Engine VMインスタンスを[作成](https://cloud.google.com/compute/docs/instances/create-start-instance)します。
1. VMインスタンスに`Stackdriver Logging API`書き込み[アクセススコープ](https://cloud.google.com/compute/docs/access/service-accounts#accesscopesiam)（デフォルトでオン）があり、VMの[サービスアカウント](https://cloud.google.com/compute/docs/access/service-accounts#default_service_account)に[`Logs Writer`](https://cloud.google.com/logging/docs/access-control)ロール（デフォルトでオフ）があることを確認します。
1. [FDA MyStudies リポジトリ](https://github.com/GoogleCloudPlatform/fda-mystudies/)から最新のコードをチェックアウトします。
1. MySQL v5.7でCloud SQLインスタンスを作成します（[手順](https://cloud.google.com/sql/docs/mysql/create-instance)）
1. Cloud SQLインスタンスで `Response datastore`データベースを構成します。
    - `Response datastore`アプリケーションがこのインスタンスにアクセスするために使用するユーザアカウントを作成します（[手順](https://cloud.google.com/sql/docs/mysql/create-manage-users)）
    -    [`mystudies_response_server_db_script.sql`](sqlscript/mystudies_response_server_db_script.sql)スクリプトを実行して、`mystudies_response_server`という名前のデータベースを作成します（[手順](https://cloud.google.com/sql/docs/mysql/import-export/importing#importing_a_sql_dump_file)）
    -    VMと同じネットワークでデータベースのプライベートIP接続を有効にします（[手順](https://cloud.google.com/sql/docs/mysql/configure-private-ip)）
1. [*ネイティブモード*](https://cloud.google.com/firestore/docs/quickstart-servers)で動作するCloud Firestoreデータベースを作成し、`Response datastore`がデータの読み取り/書き込みに使用するサービスアカウントにIAMロール[`roles/datastore.user`](https://cloud.google.com/datastore/docs/access/iam#iam_roles)を付与します（これはVMの[デフォルトのサービス](https://cloud.google.com/compute/docs/access/service-accounts#default_service_account)アカウントである可能性があります）
1. `Response datastore`コンテナをVMにデプロイします。
    -    `sudo mvn -B package -Pprod com.google.cloud.tools:jib-maven-plugin:2.5.2:dockerBuild -Dimage=response-datastore-image` を使用して、`response-datastore/` ディレクトリからDockerイメージを作成します（[Docker](https://docs.docker.com/engine/install/debian/)とMavenのインストールが必要な場合、たとえば `sudo apt install maven` のようにします）
    -    Docker環境ファイル[`variables.env`](variables.env)を、デプロイメント用に[`application.properties`](response-server-service/src/main/resources/application.properties)ファイルを構成する値で更新します。
    -    `sudo docker run --detach --env-file variables.env -p 80:8080 --name response-datastore response-datastore-image` を使用してVMでコンテナを起動します。
    -    `Hydra`インスタンスが自己署名証明書を使用している場合は、その証明書をコンテナのキーストアに追加します。たとえば `sudo docker exec -it response-datastore bash -c "openssl s_client -connect <your_hydra_instance> | sed -ne '/-BEGIN CERTIFICATE/,/END CERTIFICATE/p' > hydra.crt; keytool -import -trustcacerts -alias hydra -file hydra.crt -keystore /usr/local/openjdk-11/lib/security/cacerts -storepass changeit"` として、その後 `sudo docker restart response-datastore` でコンテナを再起動します。
1. `curl http://0.0.0.0/response-datastore/healthCheck` でアプリケーションが実行されているかどうかをテストします。
1. 指定したログディレクトリで、または `sudo docker logs response-datastore` を使用してアプリケーションログを確認できます。 監査ログは[Cloud Logging](https://cloud.google.com/logging)で利用できます。

***
<p align="center">Copyright 2020 Google LLC</p>
