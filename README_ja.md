<!--
 Copyright 2020 Google LLC
 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
-->

![FDA MyStudies](documentation/images/MyStudies_banner.svg "FDA MyStudies") 

## 概要

FDAのMyStudiesプラットフォームを利用すると、組織はiOSおよびAndroid上で専用のアプリを使用して参加者と対話する研究を迅速に構築し、展開することができます。MyStudiesアプリは、個人的に参加者に配布することも、App StoreやGoogle Playで利用できるようにすることもできます。

このオープンソースのリポジトリには、すべてのウェブおよびモバイルアプリケーションを含む、完全な FDA MyStudies インスタンスを実行するために必要なコードが含まれています。

Google Cloud Platform (GCP) への半自動デプロイのためのオープンソースの [デプロイツール](deployment) が含まれています。これらのツールを使用して、FDA MyStudies プラットフォームをわずか数時間でデプロイすることができます。これらのツールはコンプライアンスガイドラインに準拠しており、エンドツーエンドのコンプライアンスジャーニーを簡素化します。他のプラットフォームやオンプレミスのシステムへのデプロイは、手動で行うことができます。

![Platform Illustration](documentation/images/platform_illustration.png "Platform Illustration")

## ドキュメントおよびガイド

FDA Mystudies のデプロイと運用に関連する情報は、各ディレクトリの README や以下のガイドに記載されています。

<!--TODO * [Feature and functionality demonstrations](documentation/demo.md)-->
* [プラットフォームの詳細な概要](documentation/architecture.md)
* [半自動デプロイの説明書](deployment/README.md)
<!-- TODO
* User guides study builder, participant manager and mobile applications(documentation/user-guides.md)
* API reference(documentation/api-reference.md)
-->

FDA MyStudiesのドキュメントの完全なリストは、 [`documentation/README.md`](/documentation/README.md) を参照してください。

## プラットフォームコンポーネントとレポ組織

コンポーネント | 対象ユーザ | 目的 | ディレクトリ
----------------|----------------------|------------|----------------
Study builder | 研究者・臨床医 | 治験をオーサリングするためのノーコードのユーザーインターフェース ([デモ画面](documentation/images/study-builder-screens.png)) | [`study-builder/`](study-builder/)<br/>[`study-datastore/`](study-datastore/)
Participant manager | 治験コーディネーター | 参加者の登録を管理するためのコードなしのユーザーインターフェース ([デモ画面](documentation/images/participant-manager-screens.png)) | [`participant-manager/`](participant-manager/)<br/>[`participant-manager-datastore/`](participant-manager-datastore/)
Mobile applications | 治験参加者 | 治験の発見、登録、参加のためのアプリ ([demo screens](documentation/images/mobile-screens.png)) | [`iOS/`](iOS/)<br/>[`Android/`](Android/)
Response datastore | 研究者・アナリスト | ダウンストリーム分析のために参加者の応答データを収集し、保存します | [`response-datastore/`](response-datastore/)
Participant datastore |  | 連絡先や同意書などの参加者データの管理 | [`participant-datastore/`](participant-datastore/)
Auth |  | アカウント作成、ログイン、ログアウト、リソースリクエストの管理 | [`hydra/`](/hydra/)<br/>[`auth-server/`](/auth-server/)
Deployment | システム管理者 | プラットフォームを構築・保守するためのコードとしてのインフラストラクチャ | [`deployment/`](deployment/)

各上位ディレクトリには、README.mdと必要なデプロイ設定ファイルが含まれています。

プラットフォーム アーキテクチャの詳細については、[プラットフォームの概要](documentation/architecture.md)を参照してください。このアーキテクチャを Google Cloud 上でどのように展開できるかの例を以下に図示します。

![Example architecture](documentation/images/apps-reference-architecture.svg "Example architecture")

## データおよびコンプライアンス

FDA MyStudiesは、すべてのデータが導入組織の環境内に留まるように設計されています（その組織がデータのエクスポートを選択しない限り）。識別可能なデータは、研究データや回答データとは別に保存されるため、組織が機密データへのアクセスを最小限に抑えることができます。

FDA MyStudiesプラットフォームは、21 CFR Part 11に準拠するための監査要件をサポートするように設計されており、このプラットフォームを治験薬（IND）の監督下での試験に使用することができます。組織が Google Cloud 上で FDA MyStudies を実行することを選択した場合、HIPAA やその他のコンプライアンス要件をサポートするさまざまなインフラストラクチャ オプションを利用できます。Google Cloud 上でのコンプライアンスの詳細および BAA の対象となる製品の最新リストは、 [こちら](https://cloud.google.com/security/compliance/hipaa/) をご覧ください。

プラットフォーム自体に加えて、オープンソースの [デプロイツール](deployment) は、エンドツーエンドのコンプライアンスジャーニーで組織を支援するように設計されています。コンプライアンスの達成はデプロイする組織の責任ですが、これらのツールキットを使用することで、組織はコンプライアンス要件を満たすために役立つ方法で FDA MyStudies をデプロイすることができます。これらの自動化ツールで使用されるデプロイパターンの詳細については、 [こちら](https://cloud.google.com/solutions/architecture-hipaa-aligned-project) を参照してください。

Google クラウドは、関連するデータやワークロードを処理するために所定の方法で GCP サービスを使用する場合、21 CFR Part 11 規制への準拠をサポートすることができます。Google は多くの 21 CFR Part 11 準拠ワークロードに対応したクラウド技術スタックを用意していますが、最終的なコンプライアンスの判断は、導入する組織が選択した構成に依存します。

## リリースノート

FDA MyStudies コードベースの変更点の詳細については、*[What’s new](/documentation/whats-new.md)* を参照してください。

## フィードバック

機能リクエストやバグレポートは [Github Issues](https://github.com/GoogleCloudPlatform/fda-mystudies/issues/new/choose) として提出してください。すべてのフィードバックは大歓迎です。

***
<p align="center">Copyright 2020 Google LLC</p>
