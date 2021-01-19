<!--
 Copyright 2020 Google LLC
 Use of this source code is governed by an MIT-style
 license that can be found in the LICENSE file or at
 https://opensource.org/licenses/MIT.
-->

## FDA MyStudies のデプロイ
### 導入

このガイドでは、FDA MyStudiesを半自動でGoogle Cloudにデプロイする手順を説明しています。数時間で完了するように設計されています。

本ドキュメントでは、 [Terraform’s](https://www.terraform.io/) のオープンソースのInfrastructure-as-codeと継続的インテグレーションおよび継続的デプロイ（CICD）を統合して、再現性と保守性の高い環境を実現する方法を説明します。 [Kubernetes](https://kubernetes.io/) は、堅牢なスケーリングとクラスタ管理が可能なオープンソースのコンテナオーケストレーションツールとして選ばれています。リポジトリには、必要なTerraformテンプレート、セットアップスクリプト、Kubernetes設定ファイルがすべて含まれています。本ガイドでは、どのようなクラウドサービスを有効化するか、どの設定パラメータを更新するかを説明し、各手動部分の手順をステップバイステップで説明します。

この導入アプローチは、Google Cloud の [HIPAAに準拠したアーキテクチャ](https://cloud.google.com/solutions/architecture-hipaa-aligned-project) に基づいています。コンプライアンスに対するこのアプローチの詳細については、[*HIPAA に準拠したプロジェクトセットアップ*](https://cloud.google.com/solutions/setting-up-a-hipaa-aligned-project) ソリューションガイドを参照してください。

### デプロイの概要

このガイドに従うと、FDA MyStudies プラットフォームの独自のインスタンスが作成されます。結果として得られるデプロイは、 [**Figure 1**](#figure-1-overall-architecture-of-the-semi-automated-deployment) に示すような構造になります。プラットフォームの各機能的に異なる側面は、区分化と堅牢なアクセス管理を容易にするために、独自のクラウドプロジェクトにデプロイされます。
各プロジェクトとリソースにはそれぞれ目的に応じた名前が付けられ、`{PREFIX}-{ENV}`というラベルが付けられており、`{PREFIX}`は選択した一貫した名前で、`{ENV}`は環境を記述します(例えば `dev` や `prod` など)。デプロイ用に作成するプロジェクトのリストは以下の通りです。

Project | Name | Purpose
---------|------------|---------------
Devops | `{PREFIX}-{ENV}-devops` | このプロジェクトは、GitHub リポジトリの [`deployment/terraform/`](/deployment/terraform/) ディレクトリで定義された状態にインフラストラクチャを合わせる Terraform CICD パイプラインを実行します。
Apps | `{PREFIX}-{ENV}-apps` | このプロジェクトでは、FDAのMyStudiesアプリケーションのコンテナイメージを保存し、GitHubリポジトリのアプリケーションディレクトリへの変更を監視するCICDパイプラインでイメージを更新し、イメージを操作するKubernetesクラスタを管理しています（ ([**Figure 2**](#figure-2-application-architecture) は、各アプリケーションとそのデータソースとの関係を図示しています）。
Data | `{PREFIX}-{ENV}-data` | このプロジェクトには、FDA MyStudiesの各アプリケーションをサポートするMySQLデータベースと、研究リソースと同意文書を保持する Blob ストレージバケットが含まれています。
Firebase | `{PREFIX}-{ENV}-firebase` | このプロジェクトには、治験応答データを格納するNoSQLデータベースが含まれています。
Networks | `{PREFIX}-{ENV}-networks` | このプロジェクトでは、ネットワークポリシーとファイアウォールを管理します。
Secrets | `{PREFIX}-{ENV}-secrets` | このプロジェクトは、クライアント ID やクライアントの secrets などのデプロイメントの secrets を管理します。
Audit | `{PREFIX}-{ENV}-audit` | このプロジェクトは、FDAのMyStudiesプラットフォームとアプリケーションの監査ログを保存します。

このデプロイでは、アプリケーションの URL を次のように設定します。

Application | URL | Notes
--------------|-----------|-----------
[Study builder](/study-builder/) | `studies.{PREFIX}-{ENV}.{DOMAIN}/study-builder` | このURLは、管理者ユーザを `Study builder` のユーザインタフェースに誘導します。
[Study datastore](/study-datastore/) | `studies.{PREFIX}-{ENV}.{DOMAIN}/study-datastore` | このURLは `Study datastore` バックエンドサービスのためのものである。
[Participant manager](/participant-manager/) | `participants.{PREFIX}-{ENV}.{DOMAIN}/participant-manager` | このURLは管理者ユーザーを `Participant manager` のユーザーインターフェイスに誘導します。
[Participant manager datastore](/participant-manager-datastore/) | `participants.{PREFIX}-{ENV}.{DOMAIN}/participant-manager-datastore` | このURLは `Participant manager datastore` バックエンドサービス用のものである。
[Participant datastore](/participant-datastore/) | `participants.{PREFIX}-{ENV}.{DOMAIN}/participant-user-datastore`<br/>`participants.{PREFIX}-{ENV}.{DOMAIN}/participant-enroll-datastore`<br/>`participants.{PREFIX}-{ENV}.{DOMAIN}/participant-consent-datastore` | これらのURLは `Participant datastore` バックエンドサービスのものである。
[Response datastore](/response-datastore/) | `participants.{PREFIX}-{ENV}.{DOMAIN}/response-datastore` | このURLは `Response datastore` バックエンドサービスのためのものである。
[Auth server](/auth-server/) | `participants.{PREFIX}-{ENV}.{DOMAIN}/auth-server` | このURLは、管理ユーザーと治験参加者がそれぞれのアプリケーションにログインするためのものです。
[Hydra](/hydra/) | `participants.{PREFIX}-{ENV}.{DOMAIN}/oauth2` | このURLは `Auth server` が OAuth 2.0 の同意フローを完了させるために使われます。

各アプリケーションの目的についての詳細は、 [*Platform Overview*](/documentation/architecture.md) ガイドに記載されています。各アプリケーションの設定や操作に関する詳細な情報は、[それぞれの READMEs](/documentation/README.md)に記載されています。

#### 図1：半自動デプロイの全体的なアーキテクチャ
![Architecture](/documentation/images/deployment-reference-architecture.svg "Architecture")

#### 図2：アプリケーションアーキテクチャ
![Applications](/documentation/images/apps-reference-architecture.svg "Applications")

デプロイプロセスは、次のようなアプローチで行われます。
1. デプロイに使用する FDA MyStudies リポジトリのコピーを作成します。
1. デプロイのオーケストレーションに使用する `devops` クラウドプロジェクトを作成します。
1. クローン化した FDA MyStudies リポジトリを `devops` プロジェクトに接続し、残りのデプロイを自動化する CICD パイプラインを設定します。
1. CICDパイプラインを使用して必要なクラウドリソースをプロビジョニングします。
1. アプリケーションコンテナの作成を自動化する第2のCICDパイプラインを設定します。
1. アプリケーションコンテナを実行するためのKubernetesクラスタを作成します。
1. 初期ユーザーアカウントを作成し、必要な証明書、secrets、URL、ポリシー、ネットワークメッシュを設定します。
1. ブランディングやテキストの内容を自由にカスタマイズ。
1. 最初の治験を作成します。
1. モバイルアプリケーションの設定とデプロイ。

### はじめる前に

1. よく知っておいてください。
    -    [Terraform](https://www.terraform.io/) と [Terraform Engine](https://github.com/GoogleCloudPlatform/healthcare-data-protection-suite/tree/master/docs/tfengine)
    -    [Kubernetes](https://kubernetes.io/) と [Google Kubernetes Engine](https://cloud.google.com/kubernetes-engine/docs/how-to/cluster-access-for-kubectl)
    -    [CICD](https://en.wikipedia.org/wiki/CI/CD) と [Google Cloud Build](https://cloud.google.com/kubernetes-engine/docs/tutorials/gitops-cloud-build)
    -    [IAM](https://en.wikipedia.org/wiki/Identity_management) と Google Cloud’s [resource hierarchy](https://cloud.google.com/resource-manager/docs/cloud-platform-resource-hierarchy)
1. デプロイにおけるTerraformの設定ファイルとクラウドリソースの名前の付け方と整理方法を理解します。
    -  `{PREFIX}` はデプロイメのために選択した名前で、さまざまなディレクトリ、クラウドリソース、URLの前に付加されます (例: ‘mystudies’ など)。
    -  `{ENV}` は、ディレクトリやクラウドリソースの `{PREFIX}` に付加されるラベルです (例えば、‘dev’、‘test’、‘prod’ など)。
    - `{DOMAIN}` はURLに使用するドメインです (例: ‘your_company_name.com‘ や ‘your_medical_center.edu‘ など)。
    - [`/deployment/deployment.hcl`](/deployment/deployment.hcl) は、デプロイのトップレベルのパラメータを指定するファイルです (例えば `{PREFIX}` 、 `{ENV}` 、 `{DOMAIN}` の値など)。
    - [`/deployment/mystudies.hcl`](/deployment/mystudies.hcl) はデプロイの全体的なレシピを表すファイルです (ディプロイが進むにつれて、このレシピの様々な側面のコメントが解除されます)。
    -  `tfengine` コマンドによって [`/deployment/terraform/`](/deployment/terraform/) に作成されたディレクトリは、CICD パイプラインが監視している別個のクラウドプロジェクトを表しており、これらのディレクトリに加えた変更に基づいてリソースを作成、更新、または破棄します。
    -  FDA MyStudies リポジトリ内の他のディレクトリは、プラットフォームの様々なコンポーネントにマッピングされ、各コンポーネントのデプロイをサポートする `tf-deployment.yaml` や `tf-service.yaml` などの Terraform と Kubernetes の設定ファイルが含まれています。

### クラウドプラットフォームの準備

1.  [organization resource](https://cloud.google.com/resource-manager/docs/creating-managing-organization#acquiring) を含む Google Cloud 環境にアクセスできることを確認してください（組織リソースがない場合は、Google ワークスペースを [作成](https://support.google.com/a/answer/9983832) してドメインを選択することで取得できます）。
1. 使用する課金アカウントが 10 以上のプロジェクトの[クォータ](https://support.google.com/cloud/answer/6330231?hl=en) を持っていることを確認します (新しく作成された課金アカウントは、デフォルトで 3～5 プロジェクトのクォータになる場合があります)。
    - 手動で [プロジェクトを作成](https://cloud.google.com/resource-manager/docs/creating-managing-projects) して [課金アカウントにリンク](https://cloud.google.com/billing/docs/how-to/modify-project#enable_billing_for_a_project) することで、課金アカウントがサポートできるプロジェクト数をテストすることができます。10個のプロジェクトを課金アカウントにリンクすることができれば、次に進むことができます。そうでなければ [追加のクォータを要求](https://support.google.com/code/contact/billing_quota_increase) してください (課金アカウントからテストプロジェクトのリンクを外すことを忘れないでください。そうでなければクォータは使い果たしてしまうかもしれません)
1.  [resource manager](https://console.cloud.google.com/cloud-resource-manager) を使用して、FDA MyStudiesインフラストラクチャをデプロイするための [フォルダを作成し](https://cloud.google.com/resource-manager/docs/creating-managing-folders) 、例えば、このフォルダに `{PREFIX}-{ENV}` という名前を付けることができます（組織のe [`resourcemanager.folderAdmin`](https://cloud.google.com/resource-manager/docs/access-control-folders) ロールを持っていない場合は、Google CloudのIT管理者に依頼する必要があります）。
1. 次の Cloud IAM ロールを持つユーザーアカウントにアクセスできることを確認します。
    - `roles/resourcemanager.folderAdmin` フォルダ作成のため
    - `roles/resourcemanager.projectCreator` フォルダ作成のため
    - `roles/compute.xpnAdmin` フォルダ作成のため
    - `roles/billing.admin` 請求アカウントを使用するため
1.  [groups manager](https://console.cloud.google.com/identity/groups) を使用して、デプロイ時に使用する次の管理用 [IAM](https://cloud.google.com/iam/docs/overview#concepts_related_identity) グループを [create](https://support.google.com/a/answer/33343?hl=en) します。

    Group name | 説明
    ------------------|----------------
    `{PREFIX}-{ENV}-folder-admins@{DOMAIN}` | このグループのメンバーは、ディプロイフォルダの [resourcemanager.folderAdmin](https://cloud.google.com/iam/docs/understanding-roles#resource-manager-roles) ロールを持っています(例えば、接頭辞が `mystudies`、`environment` `prod`、domain `example.com` のデプロイの場合、 `mystudies-prod-folder-admins@example.com` という名前のグループが必要です)。
    `{PREFIX}-{ENV}-devops-owners@{DOMAIN}` | このグループのメンバーは、CICD パイプラインと Terraform の状態を変更するために必要な devops プロジェクトのオーナーアクセス権を持っています。
    `{PREFIX}-{ENV}-auditors@{DOMAIN}` | このグループのメンバーは、デプロイフォルダに [`iam.securityReviewer`](https://cloud.google.com/iam/docs/understanding-roles#iam-roles) ロール、監査ログプロジェクトには `bigquery.user` と `storage.objectViewer` ロールがあります。
    `{PREFIX}-{ENV}-cicd-viewers@{DOMAIN}` | このグループのメンバーはCloud BuildでCICDの結果を見ることができます。例えば、`terraform plan`のプリサブミットと`terraform apply`のポストサブミットの結果などです。
    `{PREFIX}-{ENV}-bastion-accessors@{DOMAIN}` | このグループのメンバーは、プライベートなクラウド SQL インスタンスへのアクセスを提供する [bastion host](https://cloud.google.com/solutions/connecting-securely#bastion) プロジェクトにアクセスする権限を持っています。
    `{PREFIX}-{ENV}-project-owners@{DOMAIN}` | このグループのメンバーは、デプロイの各プロジェクトへのアクセス権を持っています。
1. デプロイに使用するユーザーアカウントをこれらのグループに追加します (まだメンバーでない場合)。

### 環境設定

1. 既存の環境で作業することもできますし、任意の Google Cloud プロジェクトに VM インスタンスを [作成](https://cloud.google.com/compute/docs/instances/create-start-instance) して新しい環境を設定することもできます (例えば、Debian GNU/Linux 10 とデフォルト設定の `e2-medium` GCE VM など)。
1. 以下の依存関係がインストールされ、`$PATH`に追加されていることを確認してください。
    - Google Cloud のコマンドラインツール `gcloud` (Google Compute Engine VM を使用している場合は既にインストール済み) を [インストール](https://cloud.google.com/sdk/docs/install) します。
         ```bash
         apt-get install google-cloud-sdk
         ```
    - クラウドストレージのコマンドラインツール `gsutil` をインストールします（Google Compute Engine VM を使用している場合は既に [Install](https://cloud.google.com/storage/docs/gsutil_install) 済み）。
    - Kubernetes のコマンドラインツール `kubectl` を [Install](https://kubernetes.io/docs/tasks/tools/install-kubectl) します。例：
         ```bash
         sudo apt-get update && sudo apt-get install -y apt-transport-https gnupg2 curl && \
           curl -s https://packages.cloud.google.com/apt/doc/apt-key.gpg | sudo apt-key add - && \
           echo "deb https://apt.kubernetes.io/ kubernetes-xenial main" | sudo tee -a /etc/apt/sources.list.d/kubernetes.list && \
           sudo apt-get update && \
           sudo apt-get install -y kubectl
         ```
    - [Terraform 0.12.29](https://learn.hashicorp.com/tutorials/terraform/install-cli) をインストールします。例：
         ```shell
         sudo apt-get install software-properties-common -y && \
           curl -fsSL https://apt.releases.hashicorp.com/gpg | sudo apt-key add - && \
           sudo apt-add-repository "deb [arch=amd64] https://apt.releases.hashicorp.com $(lsb_release -cs) main" && \
           sudo apt-get update && sudo apt-get install terraform=0.12.29
         ```
    - [Terraform Engine](https://github.com/GoogleCloudPlatform/healthcare-data-protection-suite/tree/master/docs/tfengine#installation) をインストールします。例：
         ```shell
         VERSION=v0.4.0 && \
           sudo apt install wget -y && \
           sudo wget -O /usr/local/bin/tfengine https://github.com/GoogleCloudPlatform/healthcare-data-protection-suite/releases/download/${VERSION}/tfengine_${VERSION}_linux-amd64 && \
           sudo chmod +x /usr/local/bin/tfengine
         ```
    - [Git](https://github.com/git-guides/install-git) をインストールします。例：
         ```shell
         sudo apt-get install git
         ```
1. [FDA MyStudies repository](https://github.com/GoogleCloudPlatform/fda-mystudies) を [複製し](https://docs.github.com/en/free-pro-team@latest/github/creating-cloning-and-archiving-repositories/duplicating-a-repository) ローカルにクローンします。
1. デプロイ用の値で [`/deployment/deployment.hcl`](/deployment/deployment.hcl) を更新します。
1. デプロイ用に [`/deployment/scripts/set_env_var.sh`](/deployment/scripts/set_env_var.sh) を更新し、スクリプトを使用して環境変数を設定します。例：
    ```
    source set_env_var.sh    # executed from your /deployment/scripts directory
    ```
1. 上記の権限を持つユーザーとして認証します（このデプロイでは、gcloudやTerraformコマンドはサービスアカウントではなくユーザーとして作成されることを前提としています）。
    - 例えば、`gcloud auth login --update-adc` を実行して [アプリケーションのデフォルトの認証情報](https://cloud.google.com/docs/authentication/production) を更新します (Google Compute Engine VM を使用している場合は、アプリケーションのデフォルトの認証情報を更新しなければなりません。そうしないと、リクエストはデフォルトのサービスアカウントで行われ続けます)。
    - デプロイが完了したら、ユーザーアカウントをログアウトすることを忘れないでください。

### devops プロジェクトを作成し CICD パイプラインを設定

1. Terraform 設定ファイルの生成
    - [`mystudies.hcl`](/deployment/mystudies.hcl) 内の `enable_gcs_backend` フラグを `false` に設定します。例：
         ```bash
         sed -e 's/enable_gcs_backend = true/enable_gcs_backend = false/g' \
          -i.backup $GIT_ROOT/deployment/mystudies.hcl
         ```
    - `tfengine` コマンドを実行して設定を生成します (デフォルトでは、CICD は GitHub リポジトリの `deployment/terraform/` ディレクトリにある Terraform の設定を探しますので、`--output_path` を GitHub リポジトリのローカルルート内の `deployment/terraform/` ディレクトリを指すように設定します)。例：
         ```bash
         tfengine --config_path=$ENGINE_CONFIG --output_path=$GIT_ROOT/deployment/terraform
         ```
1. Terraformを使用して `{PREFIX}-{ENV}-devops` プロジェクトとTerraformステートバケットを作成します（このステップが失敗した場合は、アプリケーションのデフォルト認証情報を更新し、必要なバージョンのTerraformがインストールされていることを確認してください）。例：
    ```bash
    cd $GIT_ROOT/deployment/terraform/devops
    terraform init && terraform apply
    ```
1. [`mystudies.hcl`](/deployment/mystudies.hcl) の `enable_gcs_backend` フラグを `true` に設定し、Terraform の設定を再生成することで、`{PREFIX}-{ENV}-devops` プロジェクトの状態を新たに作成したステートバケットにバックアップします。例：
    ```bash
    sed -e 's/enable_gcs_backend = false/enable_gcs_backend = true/g' \
      -i.backup $GIT_ROOT/deployment/mystudies.hcl    
    tfengine --config_path=$ENGINE_CONFIG --output_path=$GIT_ROOT/deployment/terraform
    cd $GIT_ROOT/deployment/terraform/devops
    terraform init -force-copy
    ```
1. 新しい `{PREFIX}-{ENV}-devops` プロジェクトで [Cloud Build](https://console.cloud.google.com/cloud-build/triggers) を開き、クローンしたGitHubリポジトリを [接続](https://cloud.google.com/cloud-build/docs/automating-builds/create-github-app-triggers#installing_the_cloud_build_app) します（次のステップでTerraformがトリガーを作成するので、トリガーの追加は省略します）。
1. デプロイメント用の CICD パイプラインを作成します (これにより、$GIT_ROOT/deployment/terraform/ にあるファイルへの変更を含むプルリクエストが [`deployment.hcl`](/deployment/deployment.hcl) で指定した GitHub ブランチに対して提起されたときに実行される Cloud Builder のトリガーが作成されます)。
    ```bash
    cd $GIT_ROOT/deployment/terraform/cicd
    terraform init && terraform apply
    ```
### プラットフォームインフラストラクチャのデプロイ

1. ローカルの git 作業ディレクトリ (これでインフラストラクチャの状態を表すようになりました) を、クローンした FDA MyStudies リポジトリの新しいブランチにコミットします。
    ```bash
    cd $GIT_ROOT
    git checkout -b initial-deployment
    git add $GIT_ROOT/deployment/terraform
    git commit -m "Perform initial deployment"
    git push origin initial-deployment
    ```
1. この新しいブランチを使用して、 [`deployment.hcl`](/deployment/deployment.hcl) で指定したブランチに対するプルリクエストを [作成](https://docs.github.com/en/free-pro-team@latest/github/collaborating-with-issues-and-pull-requests/creating-a-pull-request) することで、Terraform のプリサブミットチェックを実行するように Cloud Build をトリガーします。(`devops` プロジェクトの [Cloud Build history](https://console.cloud.google.com/cloud-build/builds) で、必要に応じて事前提出チェックや再実行ジョブのステータスを確認することができます)。
    > 注:  pre-submit チェックまたは post-submitの `terraform apply` が課金アカウントに関連するエラーで失敗した場合、指定された課金アカウントにすべてのプロジェクトを添付するのに必要な [クォータ](https://support.google.com/cloud/answer/6330231?hl=en) がない可能性があります。 [追加のクォータを要求](https://support.google.com/code/contact/billing_quota_increase) する必要があるかもしれません。
1. 新規作成した各プロジェクトの `{PREFIX}-{ENV}-project-owners@{DOMAIN}` グループに [`roles/owner`](https://cloud.google.com/resource-manager/docs/access-control-proj#using_basic_roles) 権限を [付与](https://cloud.google.com/iam/docs/granting-changing-revoking-access) します。

### デプロイ用にドメインを設定

1. Cloud DNSが `{PREFIX}-{ENV}` サブドメインに割り当てた [ネームサーバーを決定します](https://cloud.google.com/dns/docs/update-name-servers#look-up-cloud-dns-name-servers) 。
    - `{PREFIX}-{ENV}-apps` プロジェクトの [DNS zones](https://console.cloud.google.com/net-services/dns/zones) に移動します。
    - ゾーン名 `{PREFIX}-{ENV}` をクリックし、右上の `Registrar Setup` をクリックすると、サブドメインに割り当てられたネームサーバが表示されます。
1. ドメインレジストラでドメインのDNS設定を更新して、Cloud DNSによって割り当てられたネームサーバーを使用して [委任サブゾーン](https://cloud.google.com/dns/docs/dns-overview#delegated_subzone) を作成します（このプロセスはドメインレジストラによって異なります - これらの変更を行う方法については、ドメインレジストラのマニュアルを参照するか、ドメインのIT管理者に問い合わせてください。
    - ドメインレジストラがGoogle Domainsの場合は、以下のように委任サブゾーンを作成します。
         1. ドメインを管理しているアカウントを使用して [Google Domains](https://domains.google.com/) にログインします。
         1. ドメインのDNSページに移動し、 ‘custom resource records’ セクションまでスクロールします（他のセクションで変更を加える必要はありません）。
         1. Cloud DNSで指定したネームサーバーと一致する `{PREFIX}-{ENV}` サブドメインのNS [resource record](https://support.google.com/domains/answer/3290350) を作成します。例：
![Domain configuration](/documentation/images/delegated-subzone.png "Domain configuration")
1. 設定を[確認](https://cloud.google.com/dns/docs/tutorials/create-domain-tutorial#step-6:-verify-your-setup) してください（DNSの変更がインターネット上に伝播するまでに最大48時間かかる場合があります）。

### デプロイのデータベース設定

1.  [*Native mode*](https://cloud.google.com/datastore/docs/firestore-or-datastore) のCloud Firestoreデータベースを{ `PREFIX}-{ENV}-firebase` プロジェクトに [作成](https://console.cloud.google.com/datastore/) します(ここで選択した場所は、 `deployment.hcl` ファイルで設定したリージョンと一致する必要はありません)。
1. TerraformとCICDを使用して、Firestoreインデックス、クラウドSQLインスタンス、ユーザーアカウント、IAMロールバインディングを作成します。
    -  [`mystudies.hcl`](/deployment/mystudies.hcl) のステップ5.1～5.6のブロックのコメントを解除します。
         ```bash
         sed -e 's/#5# //g' -i.backup $GIT_ROOT/deployment/mystudies.hcl
         ```
    - Terraform の設定を再生成し、変更をリポジトリにコミットします。例：
         ```bash
         cd $GIT_ROOT
         tfengine --config_path=$ENGINE_CONFIG --output_path=$GIT_ROOT/deployment/terraform
         git checkout -b database-configuration
         git add $GIT_ROOT/deployment/terraform
         git commit -m "Configure databases"
         git push origin database-configuration
         ```
    - プルリクエストのプリサブミットチェックが正常に完了し、コードレビューの承認が得られたら、プルリクエストをマージして `terraform apply` を実行します(この操作には最大20分かかる場合があります - 操作のステータスは `devops` プロジェクトの [Cloud Build history](https://console.cloud.google.com/cloud-build/builds) で確認できます)。
1. SQLスクリプトバケットのパーミッションを設定して、Cloud SQLインスタンスが必要な初期化スクリプトをインポートできるようにします。
    - [`mystudies.hcl`](/deployment/mystudies.hcl) 内 Step 6 のブロックのコメントを解除します。例：
         ```bash
         sed -e 's/#6# //g' -i.backup $GIT_ROOT/deployment/mystudies.hcl
         ```
    - Terraform の設定を再生成し、変更をリポジトリにコミットします。例：
         ```bash
         cd $GIT_ROOT
         tfengine --config_path=$ENGINE_CONFIG --output_path=$GIT_ROOT/deployment/terraform
         git checkout -b sql-bucket-permissions
         git add $GIT_ROOT/deployment/terraform
         git commit -m "Set SQL bucket permissions"
         git push origin sql-bucket-permissions
         ```
    - プルリクエストのプリサブミットチェックが正常に完了し、コードレビューの承認が得られたら、プルリクエストをマージして `terraform apply` を実行します(この操作には最大10分かかる場合があります - この操作のステータスは `devops` プロジェクトの [Cloud Build history](https://console.cloud.google.com/cloud-build/builds) で確認できます)。
1. SQL スクリプトをインポートして MySQL データベースを初期化します。
    - 必要なSQLスクリプトファイルを、Terraformデプロイ時に作成した`{PREFIX}-{ENV}-mystudies-sql-import` ストレージバケットにアップロードします。例：
         ```bash
         gsutil cp \
           ${GIT_ROOT}/study-builder/sqlscript/* \
           ${GIT_ROOT}/response-datastore/sqlscript/mystudies_response_server_db_script.sql \
           ${GIT_ROOT}/participant-datastore/sqlscript/mystudies_app_info_update_db_script.sql \
           ${GIT_ROOT}/participant-datastore/sqlscript/mystudies_participant_datastore_db_script.sql \
           ${GIT_ROOT}/auth-server/sqlscript/mystudies_oauth_server_hydra_db_script.sql \
           ${GIT_ROOT}/hydra/sqlscript/create_hydra_db_script.sql \
           gs://${PREFIX}-${ENV}-mystudies-sql-import
         ```
    - SQL スクリプトをクラウドストレージからクラウド SQL インスタンスにインポートします。例：
         ```bash
         gcloud sql import sql --project=${PREFIX}-${ENV}-data mystudies gs://${PREFIX}-${ENV}-mystudies-sql-import/create_hydra_db_script.sql -q
         gcloud sql import sql --project=${PREFIX}-${ENV}-data mystudies gs://${PREFIX}-${ENV}-mystudies-sql-import/mystudies_oauth_server_hydra_db_script.sql -q
         gcloud sql import sql --project=${PREFIX}-${ENV}-data mystudies gs://${PREFIX}-${ENV}-mystudies-sql-import/HPHC_My_Studies_DB_Create_Script.sql -q
         gcloud sql import sql --project=${PREFIX}-${ENV}-data mystudies gs://${PREFIX}-${ENV}-mystudies-sql-import/procedures.sql -q
         gcloud sql import sql --project=${PREFIX}-${ENV}-data mystudies gs://${PREFIX}-${ENV}-mystudies-sql-import/version_info_script.sql -q
         gcloud sql import sql --project=${PREFIX}-${ENV}-data mystudies gs://${PREFIX}-${ENV}-mystudies-sql-import/mystudies_response_server_db_script.sql -q
         gcloud sql import sql --project=${PREFIX}-${ENV}-data mystudies gs://${PREFIX}-${ENV}-mystudies-sql-import/mystudies_participant_datastore_db_script.sql -q
         ```
1. [Cloud SQL Admin API](https://cloud.google.com/sql/docs/mysql/admin-api) を `{PREFIX}-{ENV}-apps` プロジェクトで [有効](https://console.cloud.google.com/marketplace/product/google/sqladmin.googleapis.com) にします。
    ```bash
    gcloud config set project $PREFIX-$ENV-apps && \
      gcloud services enable sqladmin.googleapis.com
    ```

### アプリケーションの設定とデプロイ

1. あなたの `{PREFIX}-{ENV}-apps` プロジェクトの [Global Compute Engine API Backend Services quota]((https://console.cloud.google.com/iam-admin/quotas/details;servicem=compute.googleapis.com;metricm=compute.googleapis.com%2Fbackend_services;limitIdm=1%2F%7Bproject%7D)) を 20 に増やすよう [リクエスト](https://cloud.google.com/compute/quotas#requesting_additional_quota) してください (まだこの値以上に設定されていない場合)。
1. クローンした GitHub リポジトリのアプリケーションディレクトリの CICD を有効にして、アプリケーションコードを変更したときにデプロイ用のアプリケーションコンテナを自動的に構築します。
    - `{PREFIX}-{ENV}-apps` プロジェクトで [Cloud Build](https://console.cloud.google.com/cloud-build/triggers) を有効にし、クローンした GitHub リポジトリに [接続](https://cloud.google.com/cloud-build/docs/automating-builds/create-github-app-triggers#installing_the_cloud_build_app) します（Terraform が次のステップで作成するので、トリガーの追加は省略します）。
    - [`mystudies.hcl`](/deployment/mystudies.hcl) のアプリプロジェクト(step 7)の Cloud Build のトリガー部分のコメントを外します。例：
         ```bash
         sed -e 's/#7# //g' -i.backup $GIT_ROOT/deployment/mystudies.hcl
         ```
    - Terraform の設定を再生成し、変更をリポジトリにコミットします。例：
         ```bash
         cd $GIT_ROOT
         tfengine --config_path=$ENGINE_CONFIG --output_path=$GIT_ROOT/deployment/terraform
         git checkout -b enable-apps-CICD
         git add $GIT_ROOT/deployment/terraform
         git commit -m "Enable CICD for applications"
         git push origin enable-apps-CICD
         ```
    - プルリクエストのプリサブミットチェックが正常に完了し、コードレビューの承認が得られたら、プルリクエストをマージして `terraform apply` を実行します(この操作には最大10分かかる場合があります - この操作のステータスは `devops` プロジェクトの [Cloud Build history](https://console.cloud.google.com/cloud-build/builds) で確認できます)。
1. Kubernetes とアプリケーションの設定ファイルを、デプロイ固有の値で更新します。
    -  レポ内の各 `tf-deployment.yaml` の `<PREFIX>` 、 `<ENV>` 、 `<LOCATION>` の値を置き換えてください。例：
         ```bash
         find $GIT_ROOT -name 'tf-deployment.yaml' \
           -exec sed -e 's/<PREFIX>-<ENV>/'$PREFIX'-'$ENV'/g' \
           -e 's/<LOCATION>/'$LOCATION'/g' -i.backup '{}' \;
         ```
    -   [`/deployment/kubernetes/cert.yaml`](/deployment/kubernetes/cert.yaml) と [`/deployment/kubernetes/ingress.yaml`](/deployment/kubernetes/ingress.yaml) の `<PREFIX>`, `<ENV>`, `<DOMAIN>` の値を置換します。例：
         ```bash
         sed -e 's/<PREFIX>/'$PREFIX'/g' \
           -e 's/<ENV>/'$ENV'/g' \
           -e 's/<DOMAIN>/'$DOMAIN'/g' -i.backup \
           $GIT_ROOT/deployment/kubernetes/cert.yaml
         sed -e 's/<PREFIX>/'$PREFIX'/g' \
           -e 's/<ENV>/'$ENV'/g' \
           -e 's/<DOMAIN>/'$DOMAIN'/g' -i.backup \
           $GIT_ROOT/deployment/kubernetes/ingress.yaml
         ```
    - [`/participant-manager/src/environments/environment.prod.ts`](/participant-manager/src/environments/environment.prod.ts) 内の `<BASE_URL>` を `participants.{PREFIX}-{ENV}.{DOMAIN}` に、`<auth-server-client-id>` を `auto-auth-server-client-id` のシークレットの値に置き換えます (この値は `{PREFIX}-{ENV}-secrets` プロジェクトの [Secret Manager](https://console.cloud.google.com/security/secret-manager/) で見つけることができます)。例：
         ```bash
         gcloud config set project $PREFIX-$ENV-secrets
         export auth_server_client_id=$( \
           gcloud secrets versions access latest --secret="auto-auth-server-client-id")
         sed -e 's/<BASE_URL>/participants.'$PREFIX'-'$ENV'.'$DOMAIN'/g' \
           -e 's/<AUTH_SERVER_CLIENT_ID>/'$auth_server_client_id'/g' -i.backup \
           $GIT_ROOT/participant-manager/src/environments/environment.prod.ts
         ```
    - 変更をリポジトリにコミットします。例：
         ```bash
         cd $GIT_ROOT
         git checkout -b configure-application-properties
         git add $GIT_ROOT
         git commit -m "Initial configuration of application properties"
         git push origin configure-application-properties
         ```
    - プルリクエストのプリサブミットチェックが正常に完了し、コードレビューの承認を受けプルリクエストをマージしてコンテナイメージを構築します。その後、コンテナレジストリにある `http://gcr.io/{PREFIX}-{ENV}-apps` で利用できるようになります。(この操作には10分ほどかかるかもしれません - 操作のステータスは `{PREFIX}-{ENV}-apps` プロジェクトのCloud Build履歴で確認できます)。
1.  [Secret Manager](https://console.cloud.google.com/security/secret-manager) を開いて `{PREFIX}-{ENV}-secrets` プロジェクトのシークレットの値を接頭辞 "manual-" で入力します (または、`gcloud config set project $PREFIX-$ENV-secrets` で gcloud プロジェクトを設定し、以下のコマンドを使用します - その後、`history -c` でシェルの履歴を消去します)。

    Manually set secret | Description | When to set | Example command
    --------------------------|-------------------|----------------------|-------------------
    `manual-mystudies-email-address` | The login of the email account you want MyStudies to use to send system-generated emails | Set this value now or enter a placeholder | `echo -n "<SECRET_VALUE>" \| gcloud secrets versions add "manual-mystudies-email-address" --data-file=-`
    `manual-mystudies-email-password` | The password for that email account | Set this value now or enter a placeholder | `echo -n "<SECRET_VALUE>" \| gcloud secrets versions add "manual-mystudies-email-password" --data-file=-`
    `manual-mystudies-contact-email-address` | The email address that the in-app contact and feedback forms will send messages to | Set this value now or enter a placeholder | `echo -n "<SECRET_VALUE>" \| gcloud secrets versions add "manual-mystudies-contact-email-address" --data-file=-`
    `manual-mystudies-from-email-address` | The return email address that is shown is system-generated messages (for example, `no-reply@example.com`) | Set this value now or enter a placeholder | `echo -n "<SECRET_VALUE>" \| gcloud secrets versions add "manual-mystudies-from-email-address" --data-file=-`
    `manual-mystudies-from-email-domain` | The domain of the above email address (just the value after “@”) | Set this value now or enter a placeholder | `echo -n "<SECRET_VALUE>" \| gcloud secrets versions add "manual-mystudies-from-email-domain" --data-file=-`
    `manual-mystudies-smtp-hostname` | The hostname for your email account’s SMTP server (for example, `smtp.gmail.com`) | Set this value now or enter a placeholder | `echo -n "<SECRET_VALUE>" \| gcloud secrets versions add "manual-mystudies-smtp-hostname" --data-file=-`
    `manual-mystudies-smtp-use-ip-allowlist` | Typically ‘false’; if ‘true’, the platform will not authenticate to the email server and will rely on the allowlist configured in the SMTP service | Set this value to `true` or `false` now (you can update it later) | `echo -n "false" \| gcloud secrets versions add "manual-mystudies-smtp-use-ip-allowlist" --data-file=-`
    `manual-log-path` | The path to a directory within each application’s container where your logs will be written (for example `/logs`) | Set this value now | `echo -n "/logs" \| gcloud secrets versions add "manual-log-path" --data-file=-`
    `manual-org-name` | The name of your organization that is displayed to users, for example ‘Sincerely, the `manual-org-name` support team’ | Set this value now | `echo -n "<SECRET_VALUE>" \| gcloud secrets versions add "manual-org-name" --data-file=-`
    `manual-terms-url` | URL for a terms and conditions page that the applications will link to (for example, `https://example.com/terms`) | Set this value now or enter a placeholder | `echo -n "<SECRET_VALUE>" \| gcloud secrets versions add "manual-terms-url" --data-file=-`
    `manual-privacy-url` | URL for a privacy policy page that the applications will link to (for example, `https://example.com/privacy`) | Set this value now or enter a placeholder | `echo -n "<SECRET_VALUE>" \| gcloud secrets versions add "manual-privacy-url" --data-file=-`
    `manual-mobile-app-appid` | The value of the `App ID` (15 characters max) that you will configure on the Settings page of the [Study builder](/study-builder/) user interface when you create your first study (you will also use this same value when configuring your mobile applications for deployment) | Set now if you know what value you will use when you create your first study - otherwise enter a placeholder and update once you have created a study in the [Study builder](/study-builder) | `echo -n "<SECRET_VALUE>" \| gcloud secrets versions add "manual-mobile-app-appid" --data-file=-`
    `manual-android-bundle-id` | The value of [`applicationId`](https://developer.android.com/studio/build/application-id) that you will configure in [`Android/app/build.gradle`](/Android/app/build.gradle) during [Android configuration](/Android/), for example `{PREFIX}_{ENV}.{DOMAIN}` (note that some characters are not permitted) | If you know what value you will use during [Android](/Android/) deployment you can set this now, otherwise enter a placeholder and update later (leave as placeholder if you will be deploying to iOS only) | `echo -n "<SECRET_VALUE>" \| gcloud secrets versions add "manual-android-bundle-id" --data-file=-`
    `manual-fcm-api-url` | [URL](https://firebase.google.com/docs/reference/fcm/rest) of your Firebase Cloud Messaging API ([documentation](https://firebase.google.com/docs/cloud-messaging/http-server-ref)) | Set now if you know what this value will be - otherwise create a placeholder and update after completing your [Android](/Android/) deployment (leave as placeholder if you will be deploying to iOS only) | `echo -n "<SECRET_VALUE>" \| gcloud secrets versions add "manual-fcm-api-url" --data-file=-`
    `manual-android-server-key` | The Firebase Cloud Messaging [server key](https://firebase.google.com/docs/cloud-messaging/auth-server#authorize-legacy-protocol-send-requests) that you will obtain during [Android configuration](/Android/) | Set now if you know what this value will be - otherwise create a placeholder and update after completing your [Android](/Android/) deployment (leave as placeholder if you will be deploying to iOS only) | `echo -n "<SECRET_VALUE>" \| gcloud secrets versions add "manual-android-server-key" --data-file=-`
    `manual-android-deeplink-url` | The URL to redirect to after Android login (for example, `app://{PREFIX}-{ENV}.{DOMAIN}/mystudies`) | Set now if you know what this value will be - otherwise create a placeholder and update after completing your [Android](/Android/) deployment (leave as placeholder if you will be deploying to iOS only) | `echo -n "<SECRET_VALUE>" \| gcloud secrets versions add "manual-android-deeplink-url" --data-file=-`
    `manual-ios-bundle-id` | The value you will obtain from Xcode (Project target > General tab > Identity section > Bundle identifier) during [iOS configuration](/iOS/) - for a production application, the bundle ID needs to be verified with Apple and is usually a reverse domain name that you own; it is a unique app identifier and application capabilities are mapped to this value ([details](https://developer.apple.com/documentation/appstoreconnectapi/bundle_ids)) | Set now if you know what this value will be - otherwise create a placeholder and update after completing your [iOS](/iOS/) deployment (leave as placeholder if you will be deploying to Android only) | `echo -n "<SECRET_VALUE>" \| gcloud secrets versions add "manual-ios-bundle-id" --data-file=-`
    `manual-ios-certificate` | The value of the Base64 converted `.p12` file that you will obtain during [iOS configuration](/iOS/) | Set now if you know what this value will be - otherwise create a placeholder and update after completing your [iOS](/iOS/) deployment (leave as placeholder if you will be deploying to Android only) | `echo -n "<SECRET_VALUE>" \| gcloud secrets versions add "manual-ios-certificate" --data-file=-`
    `manual-ios-certificate-password` | The value of the password for the `.p12` certificate (necessary if your certificate is encrypted - otherwise leave empty) | Set now if you know what this value will be - otherwise create a placeholder and update after completing your [iOS](/iOS/) deployment (leave as placeholder if you will be deploying to Android only) | `echo -n "<SECRET_VALUE>" \| gcloud secrets versions add "manual-ios-certificate-password" --data-file=-`
    `manual-ios-deeplink-url` | The URL to redirect to after iOS login (for example, `app://{PREFIX}-{ENV}.{DOMAIN}/mystudies`) | Set now if you know what this value will be - otherwise create a placeholder and update after completing your [iOS](/iOS/) deployment (leave as placeholder if you will be deploying to Android only) | `echo -n "<SECRET_VALUE>" \| gcloud secrets versions add "manual-ios-deeplink-url" --data-file=-`
     > Note: When updating secrets after this initial deployment, you must refresh your Kubernetes cluster and restart the relevant pods to ensure the updated secrets are propagated to your applications (you do not need to do this now - only when making updates later), for example you can update your Kubernetes state with:
     ```bash
     cd $GIT_ROOT/deployment/terraform/kubernetes
     terraform init && terraform apply
     ```
    > then, restart the pods by deleting them in the Kubernetes dashboard or running:
     ```bash
     APP_PATH=<path_to_component_to_restart> # for example, $GIT_ROOT/auth-server
     kubectl scale --replicas=0 -f $APP_PATH/tf-deployment.yaml && \
     kubectl scale --replicas=1 -f $APP_PATH/tf-deployment.yaml
     ```
     > If you rotate an application’s ‘client_id’ or ‘client_secret’, such as `auto-response-datastore-client-id` or `auto-response-datastore-secret-key`, you must register the new values in Hydra by re-running the `register_clients_in_hydra.sh` script or executing the appropriate REST requests directly (see [`/hydra/README.md`](/hydra/README.md) for more information about working with Hydra manually)
1. Finish Kubernetes cluster configuration and deployment
    - Configure the remaining resources with Terraform, for example:
         ```bash
         cd $GIT_ROOT/deployment/terraform/kubernetes/
         terraform init && terraform apply
         ```
    - Set your `kubectl` credentials, for example:
         ```bash
         gcloud container clusters get-credentials "$PREFIX-$ENV-gke-cluster" \
           --region=$LOCATION --project="$PREFIX-$ENV-apps"
         ```
    - Apply the pod security policies, for example:
         ```bash
         kubectl apply \
           -f $GIT_ROOT/deployment/kubernetes/pod_security_policy.yaml \
           -f $GIT_ROOT/deployment/kubernetes/pod_security_policy-istio.yaml
         ```
    - Apply all deployments, for example:
         ```bash
         kubectl apply \
           -f $GIT_ROOT/study-datastore/tf-deployment.yaml \
           -f $GIT_ROOT/response-datastore/tf-deployment.yaml \
           -f $GIT_ROOT/participant-datastore/consent-mgmt-module/tf-deployment.yaml \
           -f $GIT_ROOT/participant-datastore/enroll-mgmt-module/tf-deployment.yaml \
           -f $GIT_ROOT/participant-datastore/user-mgmt-module/tf-deployment.yaml \
           -f $GIT_ROOT/study-builder/tf-deployment.yaml \
           -f $GIT_ROOT/auth-server/tf-deployment.yaml \
           -f $GIT_ROOT/participant-manager-datastore/tf-deployment.yaml \
           -f $GIT_ROOT/hydra/tf-deployment.yaml \
           -f $GIT_ROOT/participant-manager/tf-deployment.yaml
         ```
    - Apply all services, for example:
         ```bash
         kubectl apply \
           -f $GIT_ROOT/study-datastore/tf-service.yaml \
           -f $GIT_ROOT/response-datastore/tf-service.yaml \
           -f $GIT_ROOT/participant-datastore/consent-mgmt-module/tf-service.yaml \
           -f $GIT_ROOT/participant-datastore/enroll-mgmt-module/tf-service.yaml \
           -f $GIT_ROOT/participant-datastore/user-mgmt-module/tf-service.yaml \
           -f $GIT_ROOT/study-builder/tf-service.yaml \
           -f $GIT_ROOT/auth-server/tf-service.yaml \
           -f $GIT_ROOT/participant-manager-datastore/tf-service.yaml \
           -f $GIT_ROOT/hydra/tf-service.yaml \
           -f $GIT_ROOT/participant-manager/tf-service.yaml
         ```
    - Apply the certificate and the ingress, for example:
         ```bash
         kubectl apply \
           -f $GIT_ROOT/deployment/kubernetes/cert.yaml \
           -f $GIT_ROOT/deployment/kubernetes/ingress.yaml
         ```
    - Update firewalls:
        - Run `kubectl describe ingress $PREFIX-$ENV`
        - Look at the suggested commands under "Events", in the form of "Firewall
        change required by network admin"
        - Run each of the suggested commands
1. Verify the status of your Kubernetes cluster
     - Check the [Kubernetes ingress dashboard](https://console.cloud.google.com/kubernetes/ingresses) in your `{PREFIX}-{ENV}-apps` project to view the status of your cluster ingress (if status is not green, repeat the firewall step above)
    - Check the [Kubernetes workloads dashboard](https://console.cloud.google.com/kubernetes/workload) in your `{PREFIX}-{ENV}-apps` project to view the status of your applications (confirm all applications are green before proceeding - it can take up to 15 minutes for all containers to become operational)
    - [Check the status](https://cloud.google.com/load-balancing/docs/ssl-certificates/google-managed-certs#certificate-resource-status) of your Kubernetes cluster SSL certificates on the certificates page of your `{PREFIX}-{ENV}-apps` project’s [load balancing](https://console.cloud.google.com/net-services/loadbalancing/) ‘advanced menu’ (both the `participants.{PREFIX}-{ENV}.{DOMAIN}` and `studies.{PREFIX}-{ENV}.{DOMAIN}` certificates must be green for your deployment to use `https`)
1. Configure your initial application credentials
    - Create the [`Hydra`](/hydra/) credentials for server-to-server requests by running [`register_clients_in_hydra.sh`](/deployment/scripts/register_clients_in_hydra.sh), for example:
         ```bash
         $GIT_ROOT/deployment/scripts/register_clients_in_hydra.sh \
           $PREFIX $ENV $DOMAIN
         ```
    - Create your first admin user account for the [`Participant manager`](/participant-manager/) application by running the [`create_participant_manager_superadmin.sh`](/deployment/scripts/create_participant_manager_superadmin.sh) script to generate and import a SQL dump file for the [`Participant datastore`](/participant-datastore/) database (the password you specify must be at least 8 characters long and contain lower case, upper case, numeric and special characters), for example:
         ```bash
         $GIT_ROOT/deployment/scripts/create_participant_manager_superadmin.sh \
           $PREFIX $ENV <YOUR_DESIRED_LOGIN_EMAIL> <YOUR_DESIRED_PASSWORD>
         ```
    - Create your first admin user account for the [`Study builder`](/study-builder/) application by running the [`create_study_builder_superadmin.sh`](/deployment/scripts/create_study_builder_superadmin.sh) script to generate and import a SQL dump file for the [`Study datastore`](/study-datastore/) database, for example:
         ```bash
         sudo apt-get install apache2-utils -y
         $GIT_ROOT/deployment/scripts/create_study_builder_superadmin.sh \
           $PREFIX $ENV <YOUR_DESIRED_LOGIN_EMAIL> <YOUR_DESIRED_PASSWORD>
         ```

### Configure your first study

1. Navigate your browser to `studies.{PREFIX}-{ENV}.{DOMAIN}/studybuilder/` (the trailing slash is necessary) and use the account credentials that you created with the `create_study_builder_superadmin.sh` script to log into the [`Study builder`](/study-builder/) user interface
1. Change your password, then create any additional administrative accounts that you might need
1. Create a new study with the `App ID` that you set in the `manual-mobile-app-appid` secret, or choose a new `App ID` that you will update `manual-mobile-app-appid` with
1. Publish your study to propagate your study values to the other platform components
1. Navigate your browser to `participants.{PREFIX}-{ENV}.{DOMAIN}/participant-manager/` (the trailing slash is necessary), then use the account credentials that you created with the `create_participant_manager_superadmin.sh` script to log into the [`Participant manager`](/participant-manager/) user interface (if the `Participant Manager` application fails to load, confirm you are using `https` - this deployment requires `https` to be fully operational)
1. You will be asked to change your password; afterwards you can create any additional administrative accounts that you might need
1. Confirm your new study is visible in the `Participant manager` interface

### Prepare your mobile applications

1. Follow the instructions in either or both [`Android`](/Android/) and [`iOS`](/iOS/) deployment guides (if you haven’t created a study yet, you can configure the mobile applications with the `APP_ID` you plan on using when you create your first study in the [`Study builder`](/study-builder/))
1. Open Secret Manager in your `{PREFIX}-{ENV}-secrets` project and update the secrets you previously configured with placeholder values (you can skip this step if you already configured your secrets with the appropriate values - if you do update secret values, make sure to refresh your Kubernetes cluster and applications as described above)
    - `manual-mobile-app-appid` is the value of the `App ID` (15 characters max) that you configured, or will configure, on the Settings page of the [`Study builder`](/study-builder/)
    - `manual-android-bundle-id` is the value of [`applicationId`](https://developer.android.com/studio/build/application-id) that you configured in [`Android/app/build.gradle`](/Android/app/build.gradle), for example `{PREFIX}_{ENV}.{DOMAIN}` (note that some characters are not permitted)
    - `manual-fcm-api-url` is the URL of your [Firebase Cloud Messaging API](https://firebase.google.com/docs/reference/fcm/rest)
    - `manual-android-server-key` is your Firebase Cloud Messaging [server key](https://firebase.google.com/docs/cloud-messaging/auth-server#authorize-legacy-protocol-send-requests)
    - `manual-android-deeplink-url` is the URL to redirect to after Android login (for example, `app://{PREFIX}-{ENV}.{DOMAIN}/mystudies`)
    - `manual-ios-bundle-id` is the value you obtained from Xcode (in production use-cases, this  bundle ID needs to be [verified with Apple](https://developer.apple.com/documentation/appstoreconnectapi/bundle_ids))
    - `manual-ios-certificate` is the value of the Base64 converted `.p12` file
    - `manual-ios-certificate-password` is the value of the password for the `.p12` certificate
    - `manual-ios-deeplink-url` is the URL to redirect to after iOS login
1. Initialize your `Participant datastore` database to work with your mobile applications
    - Once a study with the `App ID` corresponding to the `manual-mobile-app-appid` secret is created and published using the [`Study builder`](/study-builder) user interface, a corresponding
app record will appear in the [`Participant manager`](/participant-manager/) user interface (if you created a study before all of your platform components were operational, you can reinitialize this process by using the `Study builder` user interface to pause and resume the study)
    - Once the `App ID` appears in `Participant manager`, run the [`deployment/scripts/copy_app_info_to_sql.sh`](/deployment/scripts/copy_app_info_to_sql.sh) script to update the remaining databases, for example:
         ```bash
         $GIT_ROOT/deployment/scripts/copy_app_info_to_sql.sh $PREFIX $ENV
         ```

### Clean up

1. Remove your user account from the groups you no longer need access to
1. Revoke user access in your environment, for example:
    ```bash
    gcloud auth revoke <user>@<domain> -q && \
      gcloud auth application-default revoke -q
    ```
1. Optionally, confirm no other users are logged in, for example:
    ```bash
    gcloud auth list
    ```

***
<p align="center">Copyright 2020 Google LLC</p>
