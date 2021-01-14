# Terraform EngineとTerraformを使用したFDA MyStudiesのデプロイ

このドキュメントでは、infrastrucutre-as-code を使用して FDA MyStudies を Google Cloud 上にデプロイする手順を約1時間で説明します。これらの手順を説明するビデオチュートリアルは、ご要望に応じてご利用いただけます。

提供されている [template](./deployment.hcl) をインスタンス化して使用することができます。
[Terraform Engine](https://github.com/GoogleCloudPlatform/healthcare-data-protection-suite/tree/master/docs/tfengine) を使用して、インフラストラクチャ全体を定義してデプロイする Terraform 設定を生成します。

テンプレートから生成された Terraform 設定は、FDA MyStudies インフラストラクチャを専用フォルダにデプロイし、リモート Terraform 状態管理と CICD パイプラインをデフォルトで有効にします。生成されたTerraform設定は、GitHubリポジトリにチェックインする必要があります。

この Terraform のデプロイは、Google Cloud の [HIPAA-aligned architecture](https://cloud.google.com/solutions/architecture-hipaa-aligned-project)に適応したものです。プロジェクトの構成とデプロイに関するこのアプローチについては、["Setting up a HIPAA-aligned project"](https://cloud.google.com/solutions/setting-up-a-hipaa-aligned-project) ソリューションガイドで説明しています。

## 前提条件

[前提条件](https://github.com/GoogleCloudPlatform/healthcare-data-protection-suite/tree/templates-v0.4.0/docs/tfengine#prerequisites) に従って、フォルダ内のFDA MyStudiesプラットフォームインフラストラクチャをデプロイする準備をします。

さらに、以下の追加管理 [IAM](https://cloud.google.com/iam/docs/overview#concepts_related_identity) グループを [作成](https://support.google.com/a/answer/33343?hl=en) します。

- {PREFIX}-bastion-accessors@{DOMAIN}: このグループは、プライベートのクラウド SQL インスタンスにアクセスできる踏み台ホストにアクセスする権限を持っています。

注意: {PREFIX}に{ENV}を含めることを検討してください。

## インストール

tfengine のバイナリ v0.4.0 をインストールするには、[インストールの指示](https://github.com/GoogleCloudPlatform/healthcare-data-protection-suite/tree/master/docs/tfengine/#installation) に従ってください。

## 生成された Terraform 設定のレイアウト

```bash
|- devops/: TerraformステートとCICDパイプラインをホストするプロジェクトを作成するための1回限りの手動デプロイ。
|- cicd/: CICD パイプラインを作成し、パーミッションを設定するための1回限りの手動デプロイ。
|- audit/: 監査プロジェクトとリソース（ログバケット、データセット、シンク）
|- {PREFIX}-{ENV}-apps/: アプリのプロジェクトとリソース（GKE）
|- {PREFIX}-{ENV}-data/: データプロジェクトとリソース（GCSバケット、CloudSQLインスタンス）
|- {PREFIX}-{ENV}-firebase/: firebase プロジェクトとリソース（firestores）
|- {PREFIX}-{ENV}-networks/: ネットワークプロジェクトとリソース (VPC, 踏み台ホスト)
|- {PREFIX}-{ENV}-secrets/: シークレットプロジェクトとリソース（シークレット）
|- kubernetes/: GKEクラスタが作成された後に手動でkubernetesのデプロイを行います。
```

各ディレクトリ (devops/, cicd/, {PREFIX}-{ENV}-apps/など) は、1つのTerraformデプロイを表します。各デプロイはインフラストラクチャ内の特定のリソースを管理します。

通常、デプロイには以下のファイルが含まれます。

- **main.tf**: このファイルは、管理するTerraformのリソースとモジュールを定義します。

- **variables.tf**: このファイルは、ディプロイで使用できる入力変数を定義します。

- **outputs.tf**: このファイルは、このデプロイからの出力を定義します。これらの値は他のデプロイで使用することができます。

- **terraform.tfvars**: このファイルは、入力変数の値を定義します。

各デプロイにどのようなリソースが用意されているかについては、 [mystudies.hcl](./mystudies.hcl) ファイルと個別の **main.tf** ファイルの両方のコメントを確認してください。

## CICD

`cicd` recipe の `maged_modules` の下にリストされているデプロイは、CICDパイプラインを介してデプロイされるように設定されています。

CICDサービスアカウントは、自身のプロジェクト（`devops` プロジェクト）内でリソースのサブセット（APIなど）を管理することができます。これにより、ユーザーは、手動で適用する必要なく、標準のCloud Buildパイプラインを介してデプロイされた `devops` プロジェクト内で行われた変更を、リスクの低いものにすることができます。承認されたセット（API）以外の `devops` プロジェクト内の他の変更は、手動で行う必要があります。

よくある使用例は、新しいAPIを有効にする必要があるプロジェクトに新しいリソースを追加する場合です。リソースのプロジェクトと `devops` プロジェクトの両方にAPIを追加する必要があります。上記の機能を使えば、CICD は両方の変更をデプロイすることができます。

## デプロイ手順

デプロイ手順では、Terraform Engine configの編集と Terraform config の再生成を数回行うことに注意してください。

### 準備

1. `gcloud auth ログイン [アカウント]` を使用してスーパー管理者として認証します。

    警告: スーパー管理者としてログアウトするには、`gcloud auth revoke` を実行することを忘れないでください。初期設定以上にスーパー管理者としてログインするのは危険です。

1. [deployment.hcl](./deployment.hcl) のコピーを作成し、インスタンス固有の値を記入します。

    - prefix
    - env
    - domain
    - billing_account
    - folder_id
    - github.owner
    - github.name
    - ...

    リソースの場所など、他のフィールドもユースケースに合わせて変更することができます。

1. リモートの GitHub リポジトリをローカルにクローンし、Terraform の設定を確認するために使用し、ローカルのパスを環境変数 `GIT_ROOT` に保存します。

    ```bash
    export GIT_ROOT=/path/to/your/local/repo/fda-mystudies
    ```

1. デプロイテンプレートのコピーへのパスと `mystudies.hcl` ソリューション テンプレートへのパスを環境変数に保存し、後で簡単に参照できるようにします。

    ```bash
    export ENGINE_CONFIG=/path/to/your/local/deployment.hcl
    export MYSTUDIES_TEMPLATE=/path/to/your/local/mystudies.hcl
    ```

### Step 1: Devops プロジェクトと CICD を手動でデプロイする

1. `tfengine` コマンドを実行して設定を生成します。デフォルトでは、CICDはTerraformの設定をGitHubリポジトリの `terraform/` ディレクトリの下で探すので、 `--output_path` にGitHubリポジトリのローカルルート内の `terraform/` ディレクトリを指定します。

    新しいフォルダでの最初のデプロイメントで、`$MYSTUDIES_TEMPLATE` の `enable_gcs_backend` が `false` に設定されているか、コメントアウトされていることを確認してください。

    ```bash
    tfengine --config_path=$ENGINE_CONFIG --output_path=$GIT_ROOT/deployment/terraform
    ```

#### Devops プロジェクト

1. 最初に `devops/` フォルダをデプロイして、 `devops` プロジェクトとTerraformステートバケットを作成します。

    ```bash
    cd $GIT_ROOT/deployment/terraform/devops
    terraform init
    terraform apply
    ```

    これであなたの `devops` プロジェクトの準備が整いました。

1. `MYSTUDIES_TEMPLATE` で、 `enable_gcs_backend` を `true` に設定し、Teraformの設定を再生成します。

    ```bash
    tfengine --config_path=$ENGINE_CONFIG --output_path=$GIT_ROOT/deployment/terraform
    ```

1. 以下のコマンドを実行して、新しく作成したステートバケットに `devops` プロジェクトの状態をバックアップします。

    ```bash
    terraform init -force-copy
    ```

#### CICD パイプライン

1. [Installing the Cloud Build app](https://cloud.google.com/cloud-build/docs/automating-builds/create-github-app-triggers#installing_the_cloud_build_app) の手順に従って、[connect your GitHub repository](https://console.cloud.google.com/cloud-build/triggers/connect) を{PREFIX}-{ENV}-devopsプロジェクトに接続します。-app-triggers#installing_the_cloud_build_app).

    この操作を行うには、その GitHub リポジトリの Admin 権限が必要です。

1. `cicd/` フォルダをデプロイして CICD パイプラインを設定します。

    ```bash
    cd $GIT_ROOT/deployment/terraform/cicd
    terraform init
    terraform apply
    ```

### Step 2: CICD を介してプロジェクトと最初のリソースセットをデプロイする

1. `.gitignore` ファイルに以下の項目を追加することで、以前の手動デプロイで生成された `.terraform/` ディレクトリや `*.tfstate` 、`*.tfstate.backup` ファイルを誤ってコミットしないようにします。

    ```bash
    **/.terraform
    *.tfstate
    *.tfstate.*
    ```

1. 現在のローカルの git 作業ディレクトリをコミットし、Pull Request を送信してこれらの設定をマージします。プリサブミットテストが通過し、コードレビューの承認を得ていることを確認します。そして、CD ジョブはあなたのために以下のリソースをデプロイします。

    - Audit
        - Project
        - All resources (log sink bucket and dataset)
    - Secrets
        - Project
        - All resources (いくつかのシークレットの値を手動で入力する必要があることに注意してください)
    - Networks
        - Project
        - All resources (VPC, subnets, bastion host, etc)
    - Apps
        - Project
        - All resources (Service Accounts, GKE, DNS, Binary Authorization,
            etc)
    - Firebase
        - Project
        - Partial resources (Firestore data export buckets, PubSub, etc)
    - Data
        - Project
        - Partial resources (Storage buckets, IAM bindings, etc)

### Step 3: secrets の設定

1. 特定のシークレットの値を手動で入力します。これはGoogle Cloud ConsoleのWeb UIで行う必要があります。手順は以下の通りです。

    1. <https://console.cloud.google.com/> の {PREFIX}-{ENV}-secrets に移動してください。
    1. Select "Security" --> "Secret Manager" from the top-left dropdown.
    1. Fill in the values for the secrets with prefix `manual-`.

### Step 4: Firestore database の設定

1. Setup Firestore database. This needs to be done on Google Cloud Console web
    UI. Steps:

    1. Navigate to {PREFIX}-{ENV}-firebase on
        <https://console.cloud.google.com/>.
    1. 左上のドロップダウンから  "Firestore" > "Data" を選択します。
    1. "SELECT NATIVE MODE" ボタンをクリックします。
    1. ドロップダウンからlocationを選択します。理想的には、アプリを実行する場所の近くであるべきです。
    1. "CREATE DATABASE" ボタンをクリックします。

### Step 5: CICD を介したFirebaseリソースとデータリソースの追加デプロイ

1. `$MYSTUDIES_TEMPLATE` で、*Step5.1*, *Step 5.2*, *Step 5.3*, *Step 5.4*, *Step 5.5* と *Step 5.6* とマークされたブロックのコメントを外します。その後、Terraformの設定を再生成します。In

    ```bash
    tfengine --config_path=$ENGINE_CONFIG --output_path=$GIT_ROOT/deployment/terraform
    ```

1. 現在のローカルの git 作業ディレクトリをコミットし、Pull Request を送信してこれらの設定をマージします。プリサブミットテストが通過し、コードレビューの承認を得たことを確認します。これで、CD ジョブがリソースをデプロイしてくれます。

### Step 6: CICD を介して SQL インポートバケット IAM メンバーをデプロイする

1. `MYSTUDIES_TEMPLATE` で、*Step6* とマークされたブロックのコメントを解除し、Terraformの設定を再生成します。

    ```bash
    tfengine --config_path=$ENGINE_CONFIG --output_path=$GIT_ROOT/deployment/terraform
    ```

1. 現在のローカルの git 作業ディレクトリをコミットし、Pull Request を送信してこれらの設定をマージします。プリサブミットテストが通過し、コードレビューの承認を得たことを確認します。これで、CD ジョブがリソースをデプロイしてくれます。

### Step 7: サーバーコンテナ用のクラウドビルドトリガーのデプロイ

1. [Installing the Cloud Build app](https://cloud.google.com/cloud-build/docs/automating-builds/create-github-app-triggers#installing_the_cloud_build_app) の手順に従って、GitHubリポジトリを {PREFIX}-{ENV}-apps プロジェクトに接続します。

    この操作を行うには、その GitHub リポジトリの Admin 権限が必要です。

1. `MYSTUDIES_TEMPLATE` で、AppsプロジェクトのCloud Build Triggersの部分のコメントを外し、Terraformの設定を再生成します。

1. 現在のローカルの git 作業ディレクトリをコミットし、Pull Request を送信してこれらの設定をマージします。プリサブミットテストが通過し、コードレビューの承認を得たことを確認します。これで、CD ジョブがリソースをデプロイしてくれます。

### Step 8: Kubernetes デプロイ

1. [Kubernetes README.md](./kubernetes/README.md) に従い、KubernetesリソースをGKEクラスタにデプロイします。

### Step 9: Secrets セットアップ

1. [register_clients_in_hydra.sh $PREFIX $ENV](./scripts/register_clients_in_hydra.sh) を実行し、デプロイメントの PREFIX、ENV、LOCATION をパラメータとして渡します。このスクリプトは、生成されたクライアントIDと秘密鍵を使用して、各アプリケーションをhydraに登録します。


### Step 10: スーパー管理者アカウント

Study BuilderまたはParticipant ManagerのWeb UIに初めてアクセスするには、アプリケーションごとに初期のスーパー管理者アカウントを作成する必要があります。

1. **Participant Manager**

`create_participant_manager_superadmin.sh` は、メールとパスワードを受け付け、参加者管理者の初期スーパー管理者アカウントを生成します。

```bash
./scripts/create_participant_manager_superadmin.sh <prefix> <env> <email> <password>
```

1. **Study Builder**

`create_study_builder_superadmin.sh` は、電子メールとパスワードを受け取り、Study Builderの初期のスーパー管理者アカウントを生成します。

```bash
./scripts/create_study_builder_superadmin.sh <prefix> <env> <email> <password>
```

### Step 11: モバイルアプリのセットアップ

1. [iOS](../iOS/README.md) および [Android](../Android/README.md) アプリを個別の指示に従ってビルドしてデプロイします。iOS と Android の設定手順を参照してください。


### Step 12: 参加者マネージャーでのモバイルアプリのセットアップ

アプリレコードは、FDAのMyStudiesデプロイに関連付けられたモバイルアプリの表現です。アプリは APP_ID によって識別されます。これは、シークレットマネージャーで `manual-mobile-app-appid` に設定した値です。

このApp IDを使用する治験が作成されると（治験ビルダで）、参加者マネージャーに対応するアプリレコードが作成されます。


**注** 現在のデプロイでは、単一のアプリ（`manual-mobile-app-appid` を使用）のみをサポートしており、Secret ManagerからCloudSQLにモバイル情報を渡すには、次のような手動の手順が必要です。

1. アプリがParticipant Managerで利用できるようになったら、デプロのPREFIXとENVを渡して、 [copy_app_info_to_sql.sh](scripts/copy_app_info_to_sql.sh) を実行します。

    このスクリプトでアクセスしたシークレットは
    ```bash
    # bundleID used for the Android App.
    manual-android-bundle-id
    # found under settings > cloud messaging in the android app defined in your firebase project.
    manual-android-server-key
    # bundleID used to build and distribute the iOS App.
    manual-ios-bundle-id
    # push notifications certificate in encrypted .p12 format.
    manual-ios-certificate
    # push notifications certificate password.
    manual-ios-certificate-password
    # redirect links to mobile apps, e.g. app://mydeploymentdomain.com/mystudies
    manual-ios-deeplink-url
    manual-android-deeplink-url
    ```

### Step 13: クリーンアップ

1. gcloud auth revokeを実行してスーパー管理者のアクセス権を取り消し、通常のアクティビティのために通常のユーザーとして認証します。
