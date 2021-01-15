<!--
 Copyright 2020 Google LLC
 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
-->
 
# 概要
**FDA MyStudies** [`Study builder`](/study-builder/)は、研究者、臨床医、およびその他の研究管理者が治験を作成して開始するためのノーコードユーザインターフェイスを提供するWebアプリケーションです。 治験管理者が`Study builder`を使用して治験に変更を加えると、それらの変更は、コードの変更やアプリの更新を必要とせずに、参加者のモバイルアプリケーションに伝達されます。`Study builder`は、組織の治験のコンテンツを設定します。参加者データは処理しません。`Study builder`は、Springフレームワーク上に構築されたJavaアプリケーションです。 バックエンドデータベースはMySQLデータベースであり、[`Study datastore`](/study-datastore/)と共有されます。`Study datastore`は、治験のコンテンツと設定を治験のモバイルアプリケーションに提供します。モバイルアプリケーションは、治験参加者が治験をやり取りするために使用します。`Study builder`アプリケーションは、組み込みの認証と承認を使用します。  
 
`Study builder`は、次の機能を提供します。:
1. 治験管理者の登録、ログイン・ログアウト
1. 新しい治験の作成と設定（適格性、eConsent、活動、スケジュールを含む）
1. モバイルアプリケーションへの治験の割り当て
1. 既存の治験の内容と設定の編集
1. 治験の開始、一時停止、終了
1. 治験参加者への通知の送信
 
<!-- A detailed user-guide for how to configure your first study can be found [here](TODO) --->
 
<!--TODO A demonstration of the `Study builder` application can be found [here](todo). --->
 
画面(例):
![Example screens](../documentation/images/study-builder-screens.png "Example screens")
 
# デプロイ
> **_NOTE:_** TerraformとInfrastructure-as-Codeを使用した**FDA MyStudies**プラットフォームの全体的なデプロイは、このコンポーネントをデプロイするための推奨されるアプローチです。 半自動デプロイのステップバイステップガイドは、[`deployment/`](/deployment)ディレクトリにあります。 次の手順は、VMへの手動デプロイが必要な場合に提供されます。 Google Cloudインフラストラクチャが示されていますが、同等の代替インフラストラクチャを使用することもできます。 デプロイする組織にとって、選択したサービスを構成するときに行われるIDとアクセス制御の選択を考慮することが重要です。 手動デプロイを行う場合、便利なシーケンスは[`hydra/`](/hydra)&rarr;[`auth-server/`](/auth-server/)&rarr;[`participant-datastore/`](/participant-datastore/)&rarr;[`participant-manager-datastore/`](/participant-manager-datastore/)&rarr;[`participant-manager/`](/participant-manager/)&rarr;[`study-datastore/`](/study-datastore/)&rarr;[`response-datastore/`](/response-datastore/)&rarr;[`study-builder/`](/study-builder/)&rarr;[`Android/`](/Android/)&rarr;[`iOS/`](/iOS/)です。
 
[`Study builder`](/study-builder/)を手動でデプロイするには：
1. Cloud Storageの[静的IP](https://cloud.google.com/compute/docs/ip-addresses/reserve-static-internal-ip-address)と読み取り/書き込み[アクセススコープ](https://cloud.google.com/compute/docs/access/service-accounts#accesscopesiam)を使用してCompute Engine VMインスタンスを[作成](https://cloud.google.com/compute/docs/instances/create-start-instance)します（VMの[GCEサービスアカウント](https://cloud.google.com/compute/docs/access/service-accounts#default_service_account)に、`Study datastore`のデプロイ中に作成したバケットの[`Storage Object Admin`](https://cloud.google.com/storage/docs/access-control/iam-roles)の役割があることを確認してください）
1. VMインスタンスに`Stackdriver Logging API`書き込み[アクセススコープ](https://cloud.google.com/compute/docs/access/service-accounts#accesscopesiam)（デフォルトでオン）があり、VMの[サービスアカウント](https://cloud.google.com/compute/docs/access/service-accounts#default_service_account)に[`Logs Writer`](https://cloud.google.com/logging/docs/access-control)ロール（デフォルトでオフ）があることを確認します。
1. [FDA MyStudiesリポジトリ](https://github.com/GoogleCloudPlatform/fda-mystudies/)から最新のコードをチェックアウトしてください。
1. 必要に応じてユーザ向けのテキストを更新します。たとえば、[`termsAndCondition.jsp`](fdahpStudyDesigner/src/main/webapp/WEB-INF/view/termsAndCondition.jsp)や[`privacypolicy.jsp`](fdahpStudyDesigner/src/main/webapp/WEB-INF/view/privacypolicy.jsp)などです。
1. `Study builder`コンテナーをVMにデプロイします。
    -   `sudo mvn -B package -Pprod com.google.cloud.tools:jib-maven-plugin:2.5.2:dockerBuild -f fdahpStudyDesigner/pom.xml -Dimage=study-builder-image` を使用してDockerイメージを作成します（[install Docker](https://docs.docker.com/engine/install/debian/)や、`sudo apt install maven` でMavenのインストールが必要な場合があります）
    -    Docker環境ファイル[`variables.env`](variables.env)をデプロイメント用に[`application.properties`](fdahpStudyDesigner/src/main/resources/application.properties)ファイルの設定値で更新します。
    -    `sudo docker run --detach --env-file variables.env -p 80:8080 --name study-builder study-builder-image` を使用して、VMでコンテナーを実行します。
    -    `Auth server`インスタンスが自己署名証明書を使用している場合は、その証明書をコンテナのキーストアに追加します。たとえば、 `sudo docker exec -it study-builder bash -c "openssl s_client -connect <your_auth_server_instance:port> | sed -ne '/-BEGIN CERTIFICATE/,/END CERTIFICATE/p' > auth-server.crt; keytool -import -trustcacerts -alias auth-server -file auth-server.crt -keystore /usr/local/openjdk-11/lib/security/cacerts -storepass changeit"` 、次に `sudo docker restart study-builder` でコンテナを再起動します。
1. ブラウザで`http://<CLOUD_VM_INSTANCE_IP>/studybuilder`にアクセスして、アプリケーションが実行されているかどうかをテストします。正常にデプロイされた場合は、ログインページにリダイレクトされます。
    -   VMへのアクセスがVPCネットワーク内のIPに制限されている場合は、VPC内のマシンに[リモートデスクトップ接続](https://cloud.google.com/solutions/chrome-desktop-remote-on-compute-engine)を確立できます（または、 `curl -i http://0.0.0.0/studybuilder` を使用して`302`リダイレクト応答を確認します）
1. `create_superadmin.sh`スクリプトで作成したユーザ名とパスワードを使用してスーパー管理者ユーザとしてログインします。
1. `Study builder`のユーザインターフェイスを使用して、必要に応じて追加の管理者アカウントを作成します。
1. 指定したログディレクトリで、または`sudo docker logs study-builder`を使用してアプリケーションログを確認できます。 監査ログは[Cloud Logging](https://cloud.google.com/logging)で利用できます。
 
> **_NOTE:_** モバイルアプリケーションが参加者アカウントを作成できるようにするには、`App ID`を使用して少なくとも1つの治験を公開する必要があります。 他のプラットフォームサービスの展開を完了する前に治験を公開した場合は、その設定をプラットフォーム全体に反映させるために、治験を一時停止してから再開する必要がある場合があります。  

***
<p align="center">Copyright 2020 Google LLC</p>
