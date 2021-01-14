<!--
 Copyright 2020 Google LLC
 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
-->

# 概要
このディレクトリには、治験参加者向けの**FDA MyStudies** Androidアプリケーションを構築するために必要なすべてのコードが含まれています。 [`build.gradle`](app/build.gradle)、[`api.properties`](api.properties)、[`strings.xml`](app/src/fda/res/values/strings.xml)ファイルをカスタマイズすると、Androidアプリケーションが**FDA MyStudies**デプロイメントの他のコンポーネントと対話できるようになります。 デフォルトのアプリケーション画像を独自の画像に置き換えることで、アプリのブランドをさらにカスタマイズできます。 治験の作成と操作に関連するすべての構成は、コードの変更やモバイルアプリケーションの再デプロイを必要とせずに、[`Study builder`](../study-builder/)を使用して行われます。

<!--TODO A demonstration of the Android mobile application can be found [here](todo). --->

![Example screens](../documentation/images/mobile-screens.png "Example screens")

# 要件
**FDA MyStudies** Androidアプリケーションには[Android Studio](https://developer.android.com/studio/index.html)が必要であり、Kitkat以降のAndroidバージョンで実行できます。

# 統合プラットフォーム
**FDA MyStudies**モバイルアプリケーションは、すべての調査、スケジュール、アクティビティ、適格性、同意、および通知情報を[`Study datastore`](../study-datastore/)からフェッチし、偽名化された参加者の応答データを[`Response datastore`](../response-datastore/)に投稿します。 同意書およびその他の識別可能なデータは、[`Participant datastore`](../participant-datastore/)に投稿されます。 電子メールとパスワードの認証は、OAuth2.0と[`Hydra`](/hydra/)を使用するMy Studies [`Auth server`](../auth-server/)によって処理されます。

# 設定手順

1. [`Android/app/build.gradle`](app/build.gradle)の`applicationId`を[Application ID](https://developer.android.com/studio/build/application-id)に設定します。
1. バックエンドサービスの構成に一致するように[`Android/api.properties`](api.properties)を変更します。
1. [`Android/app/src/fda/res/values/strings.xml`](app/src/fda/res/values/strings.xml)ファイルで以下を更新します。
    -   [`Hydra`](/hydra/)認証サーバーからアプリにリダイレクトするように`deeplink_host`を設定します（たとえば、`app://mystudies.<your-domain>/mystudies` - Androidアプリケーション内のディープリンクの詳細については、[こちら](https://developer.android.com/training/app-links/deep-linking)をご覧ください）
    -   `google_maps_key`を、[ここ](https://developers.google.com/maps/documentation/android-sdk/get-api-key)にある手順に従って取得したAPIキーに設定します。
    -   `package_name`と`app_name`を、[`Android/app/build.gradle`](app/build.gradle)で`applicationId`に定義する値に対応するように設定します（[詳細](https://developer.android.com/studio/build/application-id)）
    -    必要に応じてユーザー向けのテキスト文字列をカスタマイズします。
1. プッシュ通知を構成する
    -   [Firebase console](https://console.firebase.google.com/)に移動し、[`Response datastore`](/response-datastore/)のデプロイ中にCloud Firestore用に構成したプロジェクトを選択します。
    -    Firebaseコンソールのクラウドメッセージングセクションに[Androidアプリを登録](https://firebase.google.com/docs/android/setup)します（`Androidパッケージ名`は[`Android/app/build.gradle`](app/build.gradle)ファイルの`applicationID`値です）
    -    [Firebaseプロジェクト設定](https://console.firebase.google.com/project/_/settings/general/)ページから`google-services.json`ファイルをダウンロードし、[`Android/app/src/fda/google-services.json`](app/src/fda/google-services.json)を置き換えます。
1. モバイルアプリケーションとインターフェイスするように[`Participant datastore`](/participant-datastore/)インスタンスを構成します。
    -    [`participant-datastore/sqlscript/mystudies_app_info_update_db_script.sql`](../participant-datastore/sqlscript/mystudies_app_info_update_db_script.sql)のコピーを作成し、Androidの構成に一致するように値を更新します。
    -    オプションで、iOS構成に一致するようにiOSフィールドを構成します（iOSアプリケーションを構成していない場合は必要ありません）
    -    [`Participant datastore`](/participant-datastore/)のデプロイ中に作成した`mystudies_participant_datastore`データベースで更新された[`mystudies_app_info_update_db_script.sql`](../participant-datastore/sqlscript/mystudies_app_info_update_db_script.sql)スクリプトを実行します（[手順](https://cloud.google.com/sql/docs/mysql/import-export/importing#importing_a_sql_dump_file)）
1. *オプション* 画像とテキストをカスタマイズする
     -  [`Android/app/src/fda/res/`](app/src/fda/res/)ディレクトリ内の適切な解像度で画像を置き換えます：`mipmap-hdpi`, `mipmap-mdpi`, `mipmap-xhdpi`, `mipmap-xxhdpi`, `mipmap-xxxhdpi`, `drawable-560dpi`, `drawable-xhdpi`, `drawable-xxhdpi`, `drawable-xxxhdpi`
     -  [`Android/app/src/main/res/values/strings.xml`](app/src/main/res/values/strings.xml)ファイルのユーザー向けテキストをカスタマイズします。 
1. 変更を含む[`Android/`](../Android/)ディレクトリを[Android Studio](https://developer.android.com/studio/index.html)の既存のプロジェクトとして開きます。
1. 必要に応じて、ツール &rarr;[SDK Manager](https://developer.android.com/studio/intro/update#sdk-manager)、ファイル &rarr;プロジェクトをGradleファイルと同期を使用してAndroid 10 SDKをインストールします（Gradleプラグインは更新しないでください）

# 構築とデプロイ
**FDA MyStudies**アプリケーションをビルドして実行するには、[こちら](https://developer.android.com/studio/run)の手順に従ってください。

アプリケーションをユーザーに配布するには、[ここ](https://developer.android.com/studio/publish)でオプションを確認してください。

***
<p align="center">Copyright 2020 Google LLC</p>
