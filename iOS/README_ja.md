<!--
 Copyright 2020 Google LLC
 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
-->

![Build iOS](https://github.com/GoogleCloudPlatform/fda-mystudies/workflows/Build%20iOS/badge.svg) 
![SwiftLint](https://github.com/GoogleCloudPlatform/fda-mystudies/workflows/SwiftLint/badge.svg)

# 概要
このディレクトリには、研究参加者向けの**FDA MyStudies** iOSアプリケーションを構築するために必要なすべてのコードが含まれています。 [`Default.xcconfig`](MyStudies/MyStudies/Default.xcconfig)ファイルと[`Branding.plist`](MyStudies/MyStudies/Branding/Generic/Branding.plist)ファイルをカスタマイズすると、iOSアプリケーションがデプロイした**FDA MyStudies**の他のコンポーネントと対話できるようになります。デフォルトのアプリケーション画像を独自の画像に置き換えることで、アプリのブランドをさらにカスタマイズできます。治験の作成と操作に関連するすべての構成は、コードの変更やモバイルアプリケーションの再デプロイを必要とせずに、[`Study builder`](../study-builder/)を使用して行われます。

<!--TODO A demonstration of the iOS mobile application can be found [here](todo). --->

![Example screens](../documentation/images/mobile-screens.png "Example screens")

# 要件
**FDA MyStudies** iOSアプリケーションには[Xcode 11](https://developer.apple.com/xcode/)以降が必要であり、iOSバージョン11以降で実行できます。

# プラットフォーム統合
**FDA MyStudies**モバイルアプリケーションは、すべての治験・スケジュール・アクティビティ・適格性・同意および通知情報を[`Study datastore`](../study-datastore/)からフェッチし、匿名化された参加者のレスポンスデータを[`Response datastore`](../response-datastore/)に投稿します。同意書およびその他の識別可能なデータは、[`Participant datastore`](../participant-datastore/)に投稿されます。電子メールとパスワードの認証は、OAuth2.0を使用するMyStudies [`Auth server`](../auth-server/)によって処理されます。

# 構成手順
1. Xcodeで[`iOS/MyStudies/MyStudies.xcworkspace`](MyStudies/MyStudies.xcworkspace)を開きます。
1. プロジェクトの[ビルド構成](https://help.apple.com/xcode/mac/current/#/dev745c5c974)を[`iOS/MyStudies/MyStudies/Default.xcconfig`](MyStudies/MyStudies/Default.xcconfig)にマップします（[手順](https://help.apple.com/xcode/mac/current/#/deve97bde215?sub=devf0d495219)）
1. [`Default.xcconfig`](MyStudies/MyStudies/Default.xcconfig)ファイルで以下を更新します。
    -    `STUDY_DATASTORE_URL`を[`Study datastore`](../study-datastore)のURLで更新します。
    -    `RESPONSE_DATASTORE_URL`を[`Response datastore`](../response-datastore/)のURLで更新します。
    -    `USER_DATASTORE_URL`を[`User datastore`](../participant-datastore/user-mgmt-module/)のURLで更新します。
    -    `ENROLLMENT_DATASTORE_URL`を[`Enrollment datastore`](../participant-datastore/enroll-mgmt-module/)のURLで更新します。
    -    `CONSENT_DATASTORE_URL`を[`Consent datastore`](../participant-datastore/consent-mgmt-module/)のURLで更新します。
    -    `AUTH_URL`を[`Auth server`](../auth-server/)のURLで更新します。
    -    `HYDRA_BASE_URL`を[`Hydra server`](../hydra/)のURLで更新します。
    -    `HYDRA_CLIENT_ID`を、[`Hydra`](/hydra/)デプロイ中に構成した`client_id`で更新します（モバイルアプリケーションは、`client_id`を相互に、`Auth server`および`Participant manager`と共有します）
    -    [`Study datastore`](/study-datastore/)のデプロイ中に[`study-datastore/src/main/resources/authorizationResource.properties`](../study-datastore/src/main/resources/authorizationResource.properties)を設定した`bundle_id`と`app_token`で`API_KEY`を`<value of ios.bundleid>:<value of ios.apptoken>`の形式で更新します。
    -    [`Study builder`](../study-builder/)ユーザインターフェイスで治験管理者が構成する`AppId`で`APP_ID`変数を更新します。
    -    `APP_TYPE`を"gateway"または"standalone"に設定します。
    -    [`Study builder`](../study-builder/)ユーザインターフェイスで治験管理者が構成した`StudyId`で`STUDY_ID`キーを更新します（*Gateway*アプリケーションでは必要ありません）
1. 暗号化された`.p12`形式で[プッシュ通知証明書](https://help.apple.com/developer-account/#/dev82a71386a)を作成してプッシュ通知を有効にします（詳細については、[APNへの証明書ベースの接続の確立](https://developer.apple.com/documentation/usernotifications/setting_up_a_remote_notification_server/establishing_a_certificate-based_connection_to_apns)を参照してください）
1. モバイルアプリケーションとインターフェイスするように[`Participant datastore`](/participant-datastore/)インスタンスを構成します
    -    [`participant-datastore/sqlscript/mystudies_app_info_update_db_script.sql`](../participant-datastore/sqlscript/mystudies_app_info_update_db_script.sql)のコピーを作成し、iOSの構成に一致するように値を更新します。
    -    オプションで、Androidの構成に一致するようにAndroidフィールドを構成します（Androidアプリケーションを構成していない場合、またはAndroidの構成中にこの手順を既に完了している場合は必要ありません）
    -    [`Participant datastore`](/participant-datastore/)のデプロイ中に作成した`mystudies_participant_datastore`データベースで更新された[`mystudies_app_info_update_db_script.sql`](../participant-datastore/sqlscript/mystudies_app_info_update_db_script.sql)スクリプトを実行します（[手順](https://cloud.google.com/sql/docs/mysql/import-export/importing#importing_a_sql_dump_file)）
1. *オプション* 画像とテキストをカスタマイズします。
    -    [`iOS/MyStudies/MyStudies/Assets/Assets.xcassets`](MyStudies/MyStudies/Assets/Assets.xcassets/)のアイコンと画像を置き換えます。
    -    [`iOS/MyStudies/MyStudies/Branding/Generic/Branding.plist`](MyStudies/MyStudies/Branding/Generic/Branding.plist)ファイルのユーザ向けテキストを更新します。考慮すべきフィールドは次のとおりです。
         -    `ProductTitleName` - ユーザに表示されるアプリケーション名
         -    `WebsiteButtonTitle` - 概要画面に表示されるリンクのテキスト
         -    `WebsiteLink` - 概要画面に表示されるリンクの宛先
         -    `TermsAndConditionURL` - 利用規約リンクの宛先
         -    `PrivacyPolicyURL` - プライバシーポリシーリンクの宛先
         -    `NavigationTitleName` - ユーザに表示されるナビゲーションバーのタイトル
    -    [`iOS/MyStudies/MyStudies/Utils/Resources/Plists/UI/GatewayOverview.plist`](MyStudies/MyStudies/Utils/Resources/Plists/UI/GatewayOverview.plist)ファイルでユーザに提示される紹介情報を更新します。
    -    [`iOS/MyStudies/MyStudies/Assets/OtherAssets/`](MyStudies/MyStudies/Assets/OtherAssets/)にPDFファイルを追加し、[`iOS/MyStudies/MyStudies/Models/Resource/Resources.plist`](MyStudies/MyStudies/Models/Resource/Resources.plist)に対応するエントリを作成することで、ユーザが追加のリソースドキュメントを利用できるようにすることができます。

# ビルドとデプロイ

iOSアプリケーションをビルドおよびデプロイする手順については、[こちら](https://help.apple.com/xcode/mac/current/#/devdc0193470)をご覧ください。

***
<p align="center">Copyright 2020 Google LLC</p>
