<!--
 Copyright 2020 Google LLC
 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
-->
 
# 概要
**FDA MyStudies** [`Participant datastore`](/participant-datastore/)は、参加者の登録、同意を保存・管理するためのAPIを提供する3つのモジュールのセットです。[`Android`](/Android)と[`iOS`](/iOS)のモバイルアプリケーションは、参加者に固有のプロファイル情報を保存して取得し、登録と同意を確立するために`Participant datastore`と対話します。[`Response datastore`](/response-datastore/)は、参加者の登録状態を決定するために、`Participant datastore`と相互作用します。偽名化された治験応答データは、参加者の識別子なしで`Response datastore`に保存されます（例えば、アンケート質問への回答や活動データなど）。識別可能な参加者データは、治験応答データなしで`Participant datastore`に保存されます（同意書や参加者の連絡先情報など）。この分離は、導入組織がデータのクラスごとに異なるアクセス制御を構成できるように設計されています。`Participant datastore`は、クライアントアプリケーションに提供され、[`Hydra`](/hydra/)によって管理される基本的な認証`client_id`と`client_secret`を使用します。

Participant datastoreは、共通のデータベースを共有する3つのアプリケーションで構成されています。

モジュール | 目的 | クライアントアプリケーション | ディレクトリ
---------------------|-----------------------------------------|-------------------|------------
`User module` | 参加者の状態と情報を維持する | [`Study builder`](/study-builder/)<br/>[`Study datastore`](/study-datastore/)<br/>[`iOS application`](/iOS/)<br/>[`Android application`](/Android) | [/participant-datastore/user-mgmt-module/](/participant-datastore/user-mgmt-module/)
`Enrollment module` | 参加者の登録ステータスを維持する  | [`Response datastore`](/response-datastore/)<br/>[`iOS application`](/iOS/)<br/>[`Android application`](/Android) | [/participant-datastore/enroll-mgmt-module/](/participant-datastore/enroll-mgmt-module/)
`Consent module` | 参加者の同意バージョンのステータスを維持し、生成された同意ドキュメントへのアクセスを提供する | [`iOS application`](/iOS/)<br/>[`Android application`](/Android) | [/participant-datastore/consent-mgmt-module/](/participant-datastore/consent-mgmt-module/)
 
# デプロイ
> **_NOTE：_** TerraformとInfrastructure-as-Codeを使用した **FDA MyStudies** プラットフォームの全体的なデプロイは、このコンポーネントをデプロイするための推奨されるアプローチです。 半自動デプロイのステップバイステップガイドは、[`deployment/`](/deployment)ディレクトリにあります。 次の手順は、VMへの手動デプロイが必要な場合に提供されます。 Google Cloudインフラストラクチャが示されていますが、同等の代替インフラストラクチャを使用することもできます。 デプロイする組織にとって、選択したサービスを構成するときに行われるIDとアクセス制御の選択を考慮することが重要です。 手動デプロイを行う場合、便利なシーケンスは[`hydra/`](/hydra)&rarr;[`auth-server/`](/auth-server/)&rarr;[`participant-datastore/`](/participant-datastore/)&rarr;[`participant-manager-datastore/`](/participant-manager-datastore/)&rarr;[`participant-manager/`](/participant-manager/)&rarr;[`study-datastore/`](/study-datastore/)&rarr;[`response-datastore/`](/response-datastore/)&rarr;[`study-builder/`](/study-builder/)&rarr;[`Android/`](/Android/)&rarr;[`iOS/`](/iOS/) です。
 
参加者データストアを手動でデプロイするには：
1. Cloud Storageの[静的IP](https://cloud.google.com/compute/docs/ip-addresses/reserve-static-internal-ip-address)と読み取り/書き込み[アクセススコープ](https://cloud.google.com/compute/docs/access/service-accounts#accesscopesiam)を使用してCompute Engine VMインスタンスを[作成](https://cloud.google.com/compute/docs/instances/create-start-instance)します。
1. VMインスタンスに`StackdriverLogging API`書き込み[アクセススコープ](https://cloud.google.com/compute/docs/access/service-accounts#accesscopesiam)（デフォルトでオン）があり、VMの[サービスアカウント](https://cloud.google.com/compute/docs/access/service-accounts#default_service_account)に[`Logs Writer`](https://cloud.google.com/logging/docs/access-control)（デフォルトでオフ）があることを確認します。
1. [FDA MyStudies リポジトリ](https://github.com/GoogleCloudPlatform/fda-mystudies/)から最新のコードをチェックアウトします。
1. MySQL v5.7を使用してCloud SQLインスタンスを作成します。（[手順](https://cloud.google.com/sql/docs/mysql/create-instance)）
1. Cloud SQLインスタンスで`Participant datastore`データベースを構成する
    -   `User module`、`Enrollment module`、および`Consent module`がこのインスタンスにアクセスするために使用するユーザーアカウントを[作成](https://cloud.google.com/sql/docs/mysql/create-manage-users)します。
    -   `sqlscript /`ディレクトリで `./create_superadmin.sh <email> <password>` を実行して`pm-superadmin.sql`を作成します。これを使用して、参加者マネージャーの最初の管理者ユーザーを作成します（たとえば、 `sudo apt-get install apache2-utils` などの[htpasswd](https://httpd.apache.org/docs/2.4/programs/htpasswd.html)をインストールする必要がある場合があります）
    -   [`mystudies_participant_datastore_db_script.sql`](sqlscript/mystudies_participant_datastore_db_script.sql)スクリプトを実行して、`mystudies_participant_datastore`という名前のデータベースを作成します（[手順](https://cloud.google.com/sql/docs/mysql/import-export/importing#importing_a_sql_dump_file)）
    -   上記の手順で作成した`pm-superadmin.sql`スクリプトを実します。
    -   VMと同じネットワークでデータベースのプライベートIP接続を有効にします（[手順](https://cloud.google.com/sql/docs/mysql/configure-private-ip)）
1. Google Cloud Storageバケットを[作成](https://cloud.google.com/storage/docs/creating-buckets)し、VMの[GCEサービスアカウント](https://cloud.google.com/compute/docs/access/service-accounts#default_service_account)に[`Storage Object Admin`](https://cloud.google.com/storage/docs/access-control/iam-roles)の役割を付与して、参加者同意フォームのBLOBストレージを構成します。
1. デプロイ用にFirebase Cloud Messaging APIを構成します（[ドキュメント](https://firebase.google.com/docs/cloud-messaging/http-server-ref)）
1. 各`Participant datastore`のコンテナをVMにデプロイします。
    -   モジュールごとにDockerイメージを作成します（たとえば、 `sudo apt install maven` のように[install Docker](https://docs.docker.com/engine/install/debian/)とMavenが必要な場合があります）
         ```bash
         sudo mvn -B package -Pprod com.google.cloud.tools:jib-maven-plugin:2.5.2:dockerBuild -f user-mgmt-module/pom.xml -Dimage=user-mgmt-image && \
         sudo mvn -B package -Pprod com.google.cloud.tools:jib-maven-plugin:2.5.2:dockerBuild -f enroll-mgmt-module/pom.xml -Dimage=enroll-mgmt-image && \
         sudo mvn -B package -Pprod com.google.cloud.tools:jib-maven-plugin:2.5.2:dockerBuild -f consent-mgmt-module/pom.xml -Dimage=consent-mgmt-image
         ```

    -   Docker環境ファイル[`user-mgmt-module / variables.env`](user-mgmt-module/variables.env)、[`enroll-mgmt-module /variables.env`](enroll-mgmt-module/variables.env)および[`consent-mgmt-module / variables.env`](consent-mgmt-module/variables.env)を更新します。これにより、デプロイメント用に`application.properties`ファイルが構成されます。
    -   下記を使用してVMでコンテナを実行します。
         ```bash
         sudo docker run --detach --env-file user-mgmt-module/variables.env -p 8080:8080 --name user-mgmt user-mgmt-image && \
         sudo docker run --detach --env-file enroll-mgmt-module/variables.env -p 8081:8080 --name enroll-mgmt enroll-mgmt-image && \
         sudo docker run --detach --env-file consent-mgmt-module/variables.env -p 8082:8080 --name consent-mgmt consent-mgmt-image
         ```
    - `Hydra`インスタンスが自己署名証明書を使用している場合は、その証明書を各コンテナーキーストアに追加します。たとえば、 `sudo docker exec -it <container_name> bash -c "openssl s_client -connect <your_hydra_instance> | sed -ne '/-BEGIN CERTIFICATE/,/END CERTIFICATE/p' > hydra.crt; keytool -import -trustcacerts -alias hydra -file hydra.crt -keystore /usr/local/openjdk-11/lib/security/cacerts -storepass changeit"` 、 次に、コンテナを再起動します。
1. アプリケーションがで実行されているかどうかをテストします。
     ```bash
    curl http://0.0.0.0:8080/participant-user-datastore/healthCheck
    curl http://0.0.0.0:8081/participant-enroll-datastore/healthCheck
    curl http://0.0.0.0:8082/participant-consent-datastore/healthCheck
    ````
1. 指定したログディレクトリで、または `sudo docker logs <container_name>` を使用してアプリケーションログを確認できます。 監査ログは[Cloud Logging](https://cloud.google.com/logging)で利用できます。

***
<p align="center">Copyright 2020 Google LLC</p>
