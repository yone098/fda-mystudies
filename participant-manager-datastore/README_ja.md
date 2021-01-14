<!--
 Copyright 2020 Google LLC
 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
-->

# 概要
**FDA MyStudies**の[`Participant manager datastore`](/participant-manager-datastore/)は、[`Participant manager`](/participant-manager/)ウェブアプリケーションが、参加者、治験、サイトを作成、維持するために使用するバックエンドAPIを提供します。`Participant manager datastore`は[`Participant datastore`](/participant-datastore/)とMySQLバックエンドデータベースを共有するJava Spring bootアプリケーションです。`Participant manager datastore`はクライアントアプリケーションに提供され、[`Hydra`](/hydra/)によって管理されるBasic認証`client_id`と`client_secret`を用います。
 
`Participant manager datastore` クライアントアプリケーションは[`Participant manager`](/participant-manager/)のユーザーインターフェースです。他のプラットフォームコンポーネントとの相互のやり取りは、共有された[`Participant datastore`](/participant-datastore/)データベースを介して行われます。

# デプロイ
> **_注:_** Terraformとinfrastructure-as-codeを使用した **FDA MyStudies**プラットフォームの全体的なデプロイは、このコンポーネントをデプロイするために推奨されるアプローチです。半自動デプロイのステップバイステップガイドは、[`deployment/`](/deployment)ディレクトリにあります。次の手順は、VMでの手動デプロイが必要な場合に提供されています。Google Cloud インフラストラクチャが示されていますが、同等の代替インフラストラクチャを使用することもできます。デプロイする組織は、選択したサービスを構成する際に、アイデンティティとアクセス制御の選択を考慮することが重要です。手動デプロイを追求する場合、便利な順序は、 [`hydra/`](/hydra)&rarr;[`auth-server/`](/auth-server/)&rarr;[`participant-datastore/`](/participant-datastore/)&rarr;[`participant-manager-datastore/`](/participant-manager-datastore/)&rarr;[`participant-manager/`](/participant-manager/)&rarr;[`study-datastore/`](/study-datastore/)&rarr;[`response-datastore/`](/response-datastore/)&rarr;[`study-builder/`](/study-builder/)&rarr;[`Android/`](/Android/)&rarr;[`iOS/`](/iOS/)です。
 
[`Participant manager datastore`](/participant-manager-datastore/)を手動でデプロイするには
1. Compute Engine VMインスタンスを[静的IP](https://cloud.google.com/compute/docs/ip-addresses/reserve-static-internal-ip-address)とCloud Storageの読み書き[アクセススコープ](https://cloud.google.com/compute/docs/access/service-accounts#accesscopesiam)で[作成](https://cloud.google.com/compute/docs/instances/create-start-instance)します(VMの[GCE service account](https://cloud.google.com/compute/docs/access/service-accounts#default_service_account)が、`Participant datastore`のデプロイ時に作成した同意フォームバケット用の[`Storage Object Admin`](https://cloud.google.com/storage/docs/access-control/iam-roles)のロールを持っていることを確認してください)。
1. VM インスタンスが`Stackdriver Logging API`の書き込み[アクセススコープ](https://cloud.google.com/compute/docs/access/service-accounts#accesscopesiam)をオン（デフォルトではオン）に設定し、VMの[サービスアカウント](https://cloud.google.com/compute/docs/access/service-accounts#default_service_account)が[`Logs Writer`](https://cloud.google.com/logging/docs/access-control)ロールを持っていること（デフォルトではオフ）を確認してください。
1. [FDA MyStudiesリポジトリ](https://github.com/GoogleCloudPlatform/fda-mystudies/)から最新のコードをチェックしてください。
1. `Participant manager datastore`コンテナをVMにデプロイします。
    -    `participant-manager-datastore/`ディレクトリから `sudo mvn -B package -Pprod com.google.cloud.tools:jib-maven-plugin:2.5.2:dockerBuild -Dimage=participant-manager-datastore-image` を使ってDockerイメージを作成します
    ([Docker](https://docs.docker.com/engine/install/debian/)とMavenインストールが必要かもしれません。例えば `sudo apt install maven` )。
    -    Dockerの環境ファイル [`variables.env`](variables.env)をデプロイ時に[`application.properties`](participant-manager-service/src/main/resources/application.properties)ファイルに設定する値で更新します。
    -    `sudo docker run --detach --env-file variables.env -p 80:8080 --name participant-manager-datastore participant-manager-datastore-image` を使用してVM上でコンテナを実行します。
    -    もし`Hydra`インスタンスが自己証明書を利用している場合は、例えば、 `sudo docker exec -it participant-manager-datastore bash -c "openssl s_client -connect <your_hydra_instance> | sed -ne '/-BEGIN CERTIFICATE/,/END CERTIFICATE/p' > hydra.crt; keytool -import -trustcacerts -alias hydra -file hydra.crt -keystore /usr/local/openjdk-11/lib/security/cacerts -storepass changeit"` でコンテナのキーストアにその証明書を追加し、 `sudo docker restart participant-manager-datastore` でコンテナを再起動します。
1. `curl http://0.0.0.0/participant-manager-datastore/healthCheck` でアプリケーションが curl で実行されているかどうかをテストします。 
1. 指定したロギングディレクトリ、または `sudo docker logs participant-manager-datastore` でアプリケーションのログを確認することができます。監査ログは[Cloud Logging](https://cloud.google.com/logging)で利用できます。

***
<p align="center">Copyright 2020 Google LLC</p>
