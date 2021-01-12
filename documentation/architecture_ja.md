<!--
 Copyright 2020 Google LLC
 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
-->

# プラットフォームの概要

FDA MyStudiesは、プラットフォームとして機能するいくつかのコンポーネントで構成されています。これらのコンポーネントには、治験を構築して参加者を登録するためのウェブベースの UI、データの流れを管理するためのバックエンドサービス、参加者が治験の発見、登録、参加に使用するモバイルアプリケーションが含まれます。

このドキュメントでは、FDA MyStudiesのアーキテクチャについて説明します。様々なプラットフォームコンポーネントの概要と、それらがどのように連携して動作するかについて説明しています。

## アーキテクチャ

![Applications diagram](images/apps-reference-architecture.svg)

上図は、FDA MyStudiesプラットフォームを構成する様々なアプリケーションを示しています。AndroidとiOSのモバイルアプリケーションは示されていません。下の図は、これらのアプリケーションが、セキュリティ、DevOps、データガバナンスを考慮した本番環境にどのように適合するかを示しています。

![Deployment diagram](images/deployment-reference-architecture.svg)

## 専門用語

本文書で使用されている用語には、以下のものがあります。

1.  *参加者*: モバイルアプリのユーザーは、治験に登録すると参加者と呼ばれ、一意の参加者IDに関連付けられます。1人のモバイルアプリユーザーを複数の治験に関連付けることができ、各治験では一意の参加者となります。
1.  *管理者*: `Study builder` UIと `Participant manager` UIのユーザーは、管理者と呼ばれる。これらの管理者は、研究者、臨床コーディネーター、治験依頼者、治験責任医師、スタッフなどです。
1.  *治験内容*: 治験を実施するために必要なすべての内容で、治験の参加資格基準、同意書、アンケート、回答の種類などが含まれます。
1.  *回答データ*: 治験の一環として提示されたアンケートや活動に対して、参加者から提供された回答。

## プラットフォームコンポーネント

プラットフォームの構成要素は以下の通りです。

-  管理用インターフェイス
   1. [Study builder](/study-builder/) (UI) 治験を作成して設定
   1. [Participant manager](/participant-manager) (UI) サイトと参加者を登録
-  セキュリティとアクセスコントロール
   1. [Hydra](/hydra/) トークン管理と OAuth 2.0 用
   1. [Auth server](/auth-server/) ログインと資格情報管理用
-  データ管理
   1.  [Study datastore](/study-datastore/) 治験設定データの管理
   1.  [Participant manager datastore](/participant-manager-datastore/) 参加と同意の手続き
   1.  [Participant datastore](/participant-datastore/) 参加者の機密情報を管理
   1.  [Response datastore](/response-datastore/) 仮名化された治験回答を管理
-  参加者インターフェース
   1.  [Android](/Android/) 治験に参加するための Android モバイルアプリケーション (UI)
   1.  [iOS](/iOS/) 治験に参加するための iOS モバイルアプリケーション (UI)

各コンポーネントはそれぞれ独自のDockerコンテナで動作します。Blob ストレージ、リレーショナルデータベース、ドキュメントストアがデータ管理機能を提供します。集中管理されたロギングは監査を可能にし、IDとアクセス制御はデータの流れを区画化します。これらの役割を果たすために使用される具体的な技術は、デプロイする組織次第ですが、シンプルさの観点から、これらのガイドでは、Google Cloud Platform サービスを活用した実装について説明しています。[デプロイメント ガイド](/deployment/) および個々のコンポーネント [READMEs](/documentation/) には、これらのサービスを使用してプラットフォームをセットアップして実行する方法の詳細な説明が記載されています。次のクラウド技術の 1 つ以上を使用する場合があります。
- コンテナのデプロイ
  -  [Kubernetes Engine](https://cloud.google.com/kubernetes-engine) (Kubernetesによるデプロイのアプローチについては、自動化された [deployment guide](/deployment/) に記載されています)
  - [Compute Engine](https://cloud.google.com/compute) (デプロイに対するVMのアプローチについては、個々のコンポーネントを参照 [READMEs](/documentation/))
- Blob ストレージ
  - [Cloud Storage](https://cloud.google.com/storage) (1)研究内容と(2)参加者同意書のためのバケット
- リレーショナルデータベース
  - [Cloud SQL](https://cloud.google.com/sql/) (1) 治験構成データ、(2) 機密性の高い参加者データ、(3) 仮名化された参加者の活動データ、(4) Hydra クライアントデータ、(5) ユーザーアカウントの資格情報のためのデータベース
- ドキュメントストア
  -  [Cloud Firestore](https://cloud.google.com/firestore) 仮名化された参加者回答データ用
- 監査ロギング
  -  [Operations Logging](https://cloud.google.com/logging) 監査ログの作成とその後の分析用
- アイデンティティおよびアクセス管理
  - [Cloud IAM](https://cloud.google.com/iam) サービスアカウントの作成と管理、個々のリソースへのロールベースのアクセス
- ネットワーク
  -  [Cloud DNS](https://cloud.google.com/dns) ドメイン管理
  -  [Virtual Private Cloud](https://cloud.google.com/vpc) イングレスの制御
- DevOps
  -  [Secret Manager](https://cloud.google.com/secret-manager) シークレットの生成・ローテーション・配布用
  -  [Cloud Build](https://cloud.google.com/cloud-build) CI/CD 用
  -  [Container Registry](https://cloud.google.com/container-registry) コンテナイメージの管理用

コンポーネントの詳細情報や設定方法は、[各ディレクトリ](/documentation/) のREADMEを参照してください。以下に、各プラットフォームコンポーネントの関係を説明します。

### 治験の設定

[`Study builder`](/study-builder/) アプリケーションは、治験管理者が治験を作成・開始したり、治験の過程で治験内容を管理したりするためのユーザーインターフェースを提供します。患者や参加者の情報を扱うことはありません。治験の内容と設定のみを扱います。


`Study builder` は、すべてのダウンストリームアプリケーションの治験設定のソースとなります。管理者がUIを使って治験をオーサリングすると、その治験設定データはMySQLデータベースに書き込まれ、 [`Study datastore`] (/study-dataastore/) と共有されます。管理者が治験を公開すると、 `Study builder` は新しい治験情報が利用可能になったことを [`Participant datastore`](/participant-datastore/) と [`Response datastore`](/response-datastore/) に通知します。これらのデータストアは更新された治験設定データを  `Study datastore` から取得します。治験管理者がPDF文書や治験画像などのバイナリファイルを `Study builder` にアップロードすると、それらのファイルはBlobストレージに保存されます。参加者のモバイルアプリケーションは、 `Study datastore` から治験設定データを取得し、必要なバイナリをBlobストレージから直接取得します。 `Study builder` は、組み込みの認証を使用して、アカウントの作成と復旧を目的として、治験管理者に電子メールを送信します。

### 参加者登録

[`Participant manager`](/participant-manager/) アプリケーションは、治験管理者が治験サイトを作成したり、特定の知見への参加者を招待したりするためのユーザーインターフェイスを提供します。[`PParticipant manager datastore`](/participant-manager-datastore/) は `Participant manager` UI のバックエンドコンポーネントです。`Participant manager datastore` は `Participant datastore` と MySQL データベースを共有しています。管理者が UI を使ってサイトを変更したり参加者を管理したりすると、変更は共有データベースを通じて `Participant datastore` に伝わります。

`Participant manager` を使って新しい参加者が追加されると、 `Participant manager datastore` は参加者に治験に登録するためのリンクを記載したメールを送信します。*open enrollment* 治験の場合、参加者は特定の招待状なしに治験を発見して参加することができます。参加者はアカウントを作成するためにモバイルアプリケーションにアクセスし、必要なバックエンドサービスを提供するために [`Auth server`](/auth-server/) を使用します。 `Auth server` はアカウント作成のリクエストを `Participant datastore` に送り、そのモバイルアプリケーションに関連する治験があることを確認し、確認された場合、`Auth server` は参加者のメールを検証してアカウントを作成します。

モバイルアプリケーションは `Study datastore` にリクエストを行うことで、利用可能な治験のリストを作成する。参加者が参加する治験を選択すると、モバイルアプリケーションは `Study datastore` から治験参加資格質問票を取得する。`Participant manager` を使って参加者を招待した場合、モバイルアプリケーションは `Participant datastore` で招待が有効であることを確認します。`Participant datastore` が参加者が治験に参加する資格があると判断すると、モバイルアプリケーションは `Study datastore` から治験の同意書を取得する。完了後、モバイルアプリケーションは `Participant datastore` に同意書を送信し、`Participant datastore` は同意PDFをBlobストレージに書き込む。これで参加者は治験に登録され、`Participant datastore` と `Response datastore` の両方に参加者の記録が作成されます。

### 継続参加

モバイルアプリケーションは、`Study datastore`から治験活動のリストと治験スケジュールを取得する。モバイルアプリケーションは、参加者が治験活動を開始したり、一時停止したり、再開したり、完了したりすると、 `Response datastore` に更新情報を投稿します。`Response datastore` は、この治験活動データをMySQLデータベースに書き込みます。参加者が治験活動を完了すると、モバイルアプリケーションはその活動の結果を `Response datastore` に投稿し、そのレスポンスデータを Cloud Firestore に書き込みます。

参加者がモバイルアプリケーションのコンタクトフォームでメッセージを送信すると、そのメッセージは `Participant datastore` に投稿され、設定した宛先に電子メールが送信される。`Participant datastore` は、モバイルアプリケーションを介して参加者に参加リマインダーやその他のタイプの通知を送信することができます。参加者がモバイルアプリケーションのダッシュボードセクションに移動すると、モバイルアプリケーションは `Response datastore` に、設定されたダッシュボードを表示するために使用される必要な治験回答を要求します。

## デプロイと運用

詳しいデプロイ方法は、[deployment guide](/deployment/) と各 [directory READMEs](/documentation/) に記載されています。
***
<p align="center">Copyright 2020 Google LLC</p>
