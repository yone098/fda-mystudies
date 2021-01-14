<!--
 Copyright 2020 Google LLC
 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
-->
 
# 概要
**FDA MyStudies**の[`Participant manager`](/participant-manager/)は、研究者、臨床医、その他の治験コーディネーターが、施設や治験全体の参加者登録の進捗状況を追跡・管理するためのノーコードのユーザーインターフェースを提供するウェブアプリケーションです。治験コーディネーターは`Participant manager`を使用して、治験に参加者を追加し、参加者の登録と同意の状態を表示します。また、治験コーディネーターは、治験施設を作成したり、治験施設を治験に割り当てたりするためにも、`Participant manager`を使用します。 `Participant manager`は、アプリケーションのバックエンドとして[Participant manager datastore](../participant-manager-datastore)を使用するAngularウェブアプリケーションです。`Participant manager`は[`Hydra`](/hydra/)によって管理されるBasic認証`client_id`と`client_secret`を使用して`Participant manager datastore`と対話します。

`Participant manager`は以下の機能を提供します。:
1. 治験コーディネーターアカウントの作成と管理
1. 治験施設を作成・管理
1. 治験を施設に割り当てる
1. 参加者を治験に追加
1. 参加者の登録状況を見る
1. 参加者の同意書を見る
1. 治験と施設の登録状況を可視化

<!-- 最初の学習を設定する方法の詳細なユーザーガイドは、以下を参照してください。 [here](TODO) --->
 
<!-- `Participant manager`アプリケーションのデモを見ることができます。 [here](TODO). --->
 
画面(例):
![画面(例)](../documentation/images/participant-manager-screens.png "画面(例)")
 
# デプロイ
> **_注:_** Terraformとinfrastructure-as-codeを使用した **FDA MyStudies**プラットフォームの全体的なデプロイは、このコンポーネントをデプロイするために推奨されるアプローチです。半自動デプロイのステップバイステップガイドは、[`deployment/`](/deployment)ディレクトリにあります。次の手順は、VMでの手動デプロイが必要な場合に提供されています。Google Cloud インフラストラクチャが示されていますが、同等の代替インフラストラクチャを使用することもできます。デプロイする組織は、選択したサービスを構成する際に、アイデンティティとアクセス制御の選択を考慮することが重要です。手動デプロイを追求する場合、便利な順序は、 [`hydra/`](/hydra)&rarr;[`auth-server/`](/auth-server/)&rarr;[`participant-datastore/`](/participant-datastore/)&rarr;[`participant-manager-datastore/`](/participant-manager-datastore/)&rarr;[`participant-manager/`](/participant-manager/)&rarr;[`study-datastore/`](/study-datastore/)&rarr;[`response-datastore/`](/response-datastore/)&rarr;[`study-builder/`](/study-builder/)&rarr;[`Android/`](/Android/)&rarr;[`iOS/`](/iOS/)です。
 
[`Participant manager`](/participant-manager/)を手動でデプロイするには
1. お好みのマシンタイプとOSでCompute Engine VMインスタンスを[作成](https://cloud.google.com/compute/docs/instances/create-start-instance)し(例えば、e2-mediumやDebian 10など)、[静的IPを予約](https://cloud.google.com/compute/docs/ip-addresses/reserve-static-internal-ip-address)します。
1. VMインスタンスが`Stackdriver Logging API`の書き込み[アクセススコープ](https://cloud.google.com/compute/docs/access/service-accounts#accesscopesiam)をオン（デフォルトではオン）に設定し、VMの[サービスアカウント](https://cloud.google.com/compute/docs/access/service-accounts#default_service_account)が[`Logs Writer`](https://cloud.google.com/logging/docs/access-control)ロールを持っていること（デフォルトではオフ）を確認してください。
1. [FDA MyStudies リポジトリ](https://github.com/GoogleCloudPlatform/fda-mystudies/)から最新のコードをチェックアウトしてください。
1. VMに`Participant manager`コンテナをデプロイします。
    -    [`environment.prod.ts`](src/environments/environment.prod.ts)ファイルをデプロイ用の値で更新します(このファイルを変更する場合は、新しいDockerイメージを作成する必要があります)。
    -    `participant-manager/` ディレクトリから `sudo docker build --build-arg basehref=/  -t participant-manager-image .` を使用してDockerイメージを作成します([Docker](https://docs.docker.com/engine/install/debian/)のインストールが必要かもしれません)。
    -    `sudo docker run --detach -p 80:80 --name participant-manager participant-manager-image` を用いてVM上でコンテナを実行します。
1. ブラウザで `http://<CLOUD_VM_INSTANCE_IP>/` にアクセスして、アプリケーションが動作しているかどうかをテストします。デプロイが成功した場合は、ログインページにリダイレクトされるはずです。
    -    VMへのアクセスがVPCネットワーク内のIPに制限されている場合は、VPC内のマシンに[リモートデスクトップ接続](https://cloud.google.com/solutions/chrome-desktop-remote-on-compute-engine)を行うことができます（代わりに `curl -i http://0.0.0.0/` を使用して `200 OK` のレスポンスを確認してください）。
    -    ドメインとセキュリティの設定方法によっては、`Participant manager`インスタンスのドメインを Chrome の insecure origin allowlist に追加する必要がある場合があります。(*chrome://flags/#unsafely-treat-insecure-origin-as-secure*)
1. `Participant datastore`デプロイ時に実行した`create_superadmin.sh`スクリプトを用いて作成したユーザー名とパスワードを使用してsuper admin userとしてログインします。
1. `Participant manager`のユーザーインターフェースを使用して、必要に応じて追加の管理者アカウントを作成します。
1. 指定したロギングディレクトリにあるアプリケーションログを確認するか、 `sudo docker logs participant-manager` で確認できます。監査ログは[Cloud Logging](https://cloud.google.com/logging)で確認できます。

***
<p align="center">Copyright 2020 Google LLC</p>
