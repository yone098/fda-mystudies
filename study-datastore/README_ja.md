<!--
 Copyright 2020 Google LLC
 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
-->

# 概要
 **FDA MyStudies** [`Study datastore`](/study-datastore/)は、[`Study builder`](/study-builder/) Webアプリケーションで構成された治験コンテンツを取得するためのAPIをクライアントアプリケーションで利用できるようにします。 たとえば、[`iOS`](/iOS/)および[`Android`](/Android/)モバイルアプリケーションは、`Study datastore`と対話して、治験スケジュールとタスクを取得します。 `Study datastore`は、組織の治験コンテンツと構成を提供します。参加者データは処理しません。 `Study datastore`は、Springフレームワーク上に構築されたJavaアプリケーションです。 バックエンドデータベースはMySQLであり、`Study builder` Webアプリケーションと共有されます。 `Study datastore`は、基本認証`bundle_id`と`app_token`を使用して、クライアントアプリケーションからの呼び出しを認証します。

`Study datastore`のクライアントアプリケーションは次のとおりです。

1. [`Androidモバイルアプリケーション`](/Android/)
1. [`iOSモバイルアプリケーション`](/iOS/)
1. [`Response datastore`](/response-datastore/)
 
`Study datastore`は次の機能を提供します。
1. 治験設定をクライアントアプリケーションに提供します。
1. 治験の適格性と同意データをクライアントアプリケーションに提供します。
1. 治験スケジュールをクライアントアプリケーションに提供します。
1. クライアントアプリケーションに治験活動を提供します。
1. 治験ステータスをクライアントアプリケーションに提供します。
 
# デプロイ
> **_注_**：TerraformとInfrastructure-as-Codeを使用した**FDA MyStudies**プラットフォームの全体的なデプロイは、このコンポーネントをデプロイするための推奨されるアプローチです。 半自動デプロイのステップバイステップガイドは、[`deployment/`](/deployment)ディレクトリにあります。 次の手順は、VMへの手動デプロイが必要な場合に提供されます。 Google Cloudインフラストラクチャが示されていますが、同等の代替インフラストラクチャを使用することもできます。 デプロイする組織にとって、選択したサービスを構成するときに行われるIDとアクセス制御の選択を考慮することが重要です。 手動デプロイを行う場合、便利なシーケンスは [`hydra/`](/hydra)&rarr;[`auth-server/`](/auth-server/)&rarr;[`participant-datastore/`](/participant-datastore/)&rarr;[`participant-manager-datastore/`](/participant-manager-datastore/)&rarr;[`participant-manager/`](/participant-manager/)&rarr;[`study-datastore/`](/study-datastore/)&rarr;[`response-datastore/`](/response-datastore/)&rarr;[`study-builder/`](/study-builder/)&rarr;[`Android/`](/Android/)&rarr;[`iOS/`](/iOS/) です。

[`Study datastore`](/study-datastore/)を手動でデプロイするには：

1. ご希望のマシンタイプとOS（e2-mediumやDebian 10など）を使用してCompute Engine VMインスタンスを[作成](https://cloud.google.com/compute/docs/instances/create-start-instance)し、静的IPを予約[静的IPを予約](https://cloud.google.com/compute/docs/ip-addresses/reserve-static-internal-ip-address)する
1. VMインスタンスに`Stackdriver Logging API`書き込み[アクセススコープ](https://cloud.google.com/compute/docs/access/service-accounts#accesscopesiam)（デフォルトでオン）があり、VMの[サービスアカウント](https://cloud.google.com/compute/docs/access/service-accounts#default_service_account)に[`Logs Writer`](https://cloud.google.com/logging/docs/access-control)ロール（デフォルトでオフ）があることを確認します。
1. [FDA MyStudies リポジトリ](https://github.com/GoogleCloudPlatform/fda-mystudies/)から最新のコードをチェックアウトします。
1. MySQL v5.7でCloud SQLインスタンスを作成します（[手順](https://cloud.google.com/sql/docs/mysql/create-instance)）
1. Cloud SQLインスタンスで `Study datastore`データベースを構成します。
    -    `Study datastore`および`Study Builder`アプリケーションがこのインスタンスにアクセスするために使用するユーザーアカウントを作成します（[手順](https://cloud.google.com/sql/docs/mysql/create-manage-users)）
    -    `/study-builder/sqlscript/`ディレクトリで`sudo ./create_superadmin.sh <email> <password>`を実行して`sb-superadmin.sql`を作成する。これを使用して、最初の`Study Builder`ユーザを作成する（[htpasswd](https://httpd.apache.org/docs/2.4/programs/htpasswd.html)のインストールが必要な場合、たとえば `sudo apt-get install apache2-utils` のようにします）
    -    [`/study-builder/HPHC_My_Studies_DB_Create_Script.sql`](/study-builder/sqlscript/HPHC_My_Studies_DB_Create_Script.sql)スクリプトを実行して、`fda_hphc`という名前のデータベースを作成します（[手順](https://cloud.google.com/sql/docs/mysql/import-export/importing#importing_a_sql_dump_file)）
    -    [`/study-builder/version_info_script.sql`](/study-builder/sqlscript/version_info_script.sql)スクリプトを実行します。
    -    [`/study-builder/procedures.sql`](/study-builder/sqlscript/procedures.sql)スクリプトを実行します。
    -    上の手順で作成した`sb-superadmin.sql`スクリプトを実行します。
    -    VMと同じネットワークでデータベースのプライベートIP接続を有効にします（[手順](https://cloud.google.com/sql/docs/mysql/configure-private-ip)）
1. Google Cloud Storageバケットを[作成](https://cloud.google.com/storage/docs/creating-buckets)し、VMの[GCEサービスアカウント](https://cloud.google.com/compute/docs/access/service-accounts#default_service_account)に[`Storage Object Admin`](https://cloud.google.com/storage/docs/access-control/iam-roles)のロールを付与し、[public read access](https://cloud.google.com/storage/docs/access-control/making-data-public#buckets)を設定することで、パブリック治験リソース用のBLOBストレージを構成します。
1. `Study datastore`コンテナをVMにデプロイします。
    -    `sudo mvn -B package -Pprod com.google.cloud.tools:jib-maven-plugin:2.5.2:dockerBuild -Dimage=study-datastore-image` を使用して、`study-datastore/`ディレクトリからDockerイメージを作成します（[Docker](https://docs.docker.com/engine/install/debian/)とMavenのインストールが必要な場合は、たとえば `sudo apt install maven` のようにします）
    -    Docker環境ファイル[`variables.env`](variables.env)を、デプロイメント用に[`application.properties`](src/main/resources/application.properties)ファイルを構成する値で更新します。
    -    `sudo docker run --detach --env-file variables.env -p 80:8080 --name study-datastore study-datastore-image` を使用して、VMでコンテナを起動します。
1. `curl 0.0.0.0/study-datastore/healthCheck` を実行して、アプリケーションが実行されているかどうかをテストします。
1. 指定したログディレクトリで、または `sudo docker logs study-datastore` を使用してアプリケーションログを確認できます。 監査ログは[Cloud Logging](https://cloud.google.com/logging)で利用できます。

***
<p align="center">Copyright 2020 Google LLC</p>
