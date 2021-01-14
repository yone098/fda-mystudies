# Kubernetes セットアップ

このディレクトリには、すべてのアプリに共通するいくつかのKubernetesリソースが含まれています。

## Kubernetesファイルの場所

以下のすべてファイルはリポジトリのルートからの相対的なものです。

* kubernetes/
  * cert.yaml
    * [Google-managed SSL certificates](https://cloud.google.com/kubernetes-engine/docs/how-to/managed-certs)を利用するためのKubernetes ManagedCertificate
  * ingress.yaml
    * クラスタ内のサービスにHTTPコールをルーティングするためのKubernetes Ingress
  * pod_security_policy.yaml
    * クラスタアプリに適用される制限的なPodセキュリティポリシー
  * pod_security_policy-istio.yaml
    * クラスタ内のIstioコンテナにのみ適用される、より緩やかなPodセキュリティポリシー
  * kubeapply.sh
    * すべてのリソースをクラスタに適用するヘルパースクリプト。必須ではありませんが、手動の手順は後述します。
* auth-server/
  * tf-deployment.yaml
    * Kubernetesのデプロイ、そのシークレットとともにアプリをデプロイします。
    * これはdepployment.yamlからフォークされたもので、Terraformの設定を変更します。
  * tf-service.yaml
    * 他のアプリとIngressと通信するためにアプリを公開するKubernetesサービスです。
    * これは service.yaml からフォークされたもので、Terraform の設定を変更します。
* response-datastore/
  * auth-serverと同じです。
* study-builder/
  * auth-serverと同じです。
* study-datastore/
  * auth-serverと同じです。
* participant-datastore/consent-mgmt-module
  * auth-serverと同じです。
* participant-datastore/enroll-mgmt-module
  * auth-serverと同じです。
* participant-datastore/user-mgmt-module
  * auth-serverと同じです。
* participant-manager/
  * auth-serverと同じです。

## セットアップ

### 事前準備

以下の依存関係をインストールし、PATHに追加します。

* [gcloud](https://cloud.google.com/sdk/gcloud)
* [gsutil](https://cloud.google.com/storage/docs/gsutil_install)
* [kubectl](https://kubernetes.io/docs/tasks/tools/install-kubectl)

コピーした `deployment.hcl` で定義されている以下の値を見つけてください。

* `<prefix>`
* `<env>`

また、以降の手順で使用される以下のプロジェクトIDにも注意してください。

* Apps project ID: `<prefix>-<env>-apps`
* Data project ID: `<prefix>-<env>-data`
* Firebase project ID: `<prefix>-<env>-firebase`

### Terraform

[deployment.md](../deployment.md)に従ってインフラストラクチャを作成します。
これは、GKEクラスタとCloud SQL MySQLデータベースインスタンスを作成します。

### SQL

このリポジトリには、アプリをデプロイする前にインポートする必要があるSQLダンプファイルがあります。

gcloud import コマンドは、GCSバケットからのみインポートします。Terraform のセットアップでは、バケットを作成し、SQLインスタンスにそこからファイルを読み込む権限を与えます。バケットの名前は`<prefix>-<env>-mystudies-sql-import`です。例えば`example-dev-mystudies-sql-import`です。

SQLファイルをバケットにアップロードします。

```bash
gsutil cp \
  ./study-builder/sqlscript/* \
  ./response-datastore/sqlscript/mystudies_response_server_db_script.sql \
  ./participant-datastore/sqlscript/mystudies_participant_datastore_db_script.sql \
  ./hydra/sqlscript/create_hydra_db_script.sql \
  gs://<prefix>-<env>-mystudies-sql-import
```

Cloud SQL DBインスタンスの名前を検索します。GCPコンソールを見ると、これは単にインスタンス名であり、「インスタンス接続名」では**ありません**。例：接続名が "myproject-data:us-east1:mystudies "の場合、"mystudies "だけを使用する必要があります。

スクリプトをこの順にインポートします。

#### Hydra

```bash
gcloud sql import sql --project=<prefix>-<env>-data <instance-name> gs://<prefix>-<env>-mystudies-sql-import/create_hydra_db_script.sql
```

#### Study builder

```bash
gcloud sql import sql --project=<prefix>-<env>-data <instance-name> gs://<prefix>-<env>-mystudies-sql-import/HPHC_My_Studies_DB_Create_Script.sql
gcloud sql import sql --project=<prefix>-<env>-data <instance-name> gs://<prefix>-<env>-mystudies-sql-import/procedures.sql
gcloud sql import sql --project=<prefix>-<env>-data <instance-name> gs://<prefix>-<env>-mystudies-sql-import/version_info_script.sql
```

#### Response datastore

```bash
gcloud sql import sql --project=<prefix>-<env>-data <instance-name> gs://<prefix>-<env>-mystudies-sql-import/mystudies_response_server_db_script.sql
```

#### Participant datastore

```bash
gcloud sql import sql --project=<prefix>-<env>-data <instance-name> gs://<prefix>-<env>-mystudies-sql-import/mystudies_participant_datastore_db_script.sql
```

### Kubernetes の設定値

組織やデプロイに合わせてKubernetesの設定を変更する必要があるかもしれません。

以下の各tf-deployment.yamlファイルの中にあります (パスはリポジトリのルートからの相対パスです)。

1. auth-server/tf-deployment.yaml
1. hydra/tf-deployment.yaml
1. response-datastore/tf-deployment.yaml
1. study-builder/tf-deployment.yaml
1. study-datastore/tf-deployment.yaml
1. participant-datastore/consent-mgmt-module/tf-deployment.yaml
1. participant-datastore/enroll-mgmt-module/tf-deployment.yaml
1. participant-datastore/user-mgmt-module/tf-deployment.yaml
1. participant-manager-datastore/tf-deployment.yaml
1. participant-manager/tf-deployment.yaml

以下のことをしてください。

* `gcr.io/cloudsql-docker/gce-proxy`以外のすべてのイメージについては、
     `gcr.io/<project>` の部分を `gcr.io/<prefix>-<env>-apps` に置き換えてください。
* cloudsql-proxyコンテナについては、 `-instances` フラグを 
    `-instances=<cloudsq-instance-connection-name>=tcp:3306` と設定します。

./kubernetes/cert.yamlファイルにて

* 組織に合わせて名前とドメインを変更します。

./kubernetes/ingress.yamlファイルにて

* `networking.gke.io/managed-certificates` アノテーションを ./kubernetes/cert.yaml での名前と一致するように変更します。
* 名前と `kubernetes.io/ingress.global-static-ip-name` アノテーションを組織に合わせて変更します。
* デプロイした環境と ./kubernetes/cert.yaml で更新した内容に合わせてホストを変更します。

./participant-manager/src/environments/environment.prod.tsにて

* ドメイン名を組織に合わせて変更します。
* `clientId`を`auto-auth-server-client-id`の値に変更します。
    この値はシークレットプロジェクトのシークレットマネージャーで見つけることができます。

### GKE クラスタ - Terraform

一部のKubernetesリソースは、設定の容易さからTerraformを介して管理されています。これらはクラスタが既に存在した後に適用する必要があり、[Master Authorized Networks](https://cloud.google.com/kubernetes-engine/docs/how-to/authorized-networks)の設定のため、CI/CD自動化では適用できません。

まず、gcloud経由で認証を行います。

```bash
gcloud auth login
gcloud auth application-default login
```

Kubernetes Terraformディレクトリに移動します。

```bash
cd deployment/terraform/kubernetes/
```

**`terraform.tfvars`ファイルを編集します。プロジェクトとクラスタの情報が正しいことを確認してください。**

Terraform の設定を起動、計画、適用します。

```bash
terraform init
terraform plan
terraform apply
```

(オプション) 最後に、gcloud認証を無効化します。

```bash
gcloud auth revoke
gcloud auth application-default revoke
```

### GKE クラスタ - kubectl

以下のすべてのコマンドをリポジトリのルートから実行してください。

まず、クラスタと対話できるように kubectl の資格情報を取得します。

```bash
gcloud container clusters get-credentials "<cluster-name>" --region="<region>" --project="<prefix>-<env>-apps"
```

ポッドのセキュリティポリシーを適用します。

```bash
$ kubectl apply \
  -f ./kubernetes/pod_security_policy.yaml \
  -f ./kubernetes/pod_security_policy-istio.yaml
```

すべてのデプロイを適用します。

```bash
$ kubectl apply \
  -f ./study-datastore/tf-deployment.yaml \
  -f ./response-datastore/tf-deployment.yaml \
  -f ./participant-datastore/consent-mgmt-module/tf-deployment.yaml \
  -f ./participant-datastore/enroll-mgmt-module/tf-deployment.yaml \
  -f ./participant-datastore/user-mgmt-module/tf-deployment.yaml \
  -f ./study-builder/tf-deployment.yaml \
  -f ./auth-server/tf-deployment.yaml \
  -f ./participant-manager-datastore/tf-deployment.yaml \
  -f ./hydra/tf-deployment.yaml \
  -f ./participant-manager/tf-deployment.yaml
```

すべてのサービスを適用します。

```bash
$ kubectl apply \
  -f ./study-datastore/tf-service.yaml \
  -f ./response-datastore/tf-service.yaml \
  -f ./participant-datastore/consent-mgmt-module/tf-service.yaml \
  -f ./participant-datastore/enroll-mgmt-module/tf-service.yaml \
  -f ./participant-datastore/user-mgmt-module/tf-service.yaml \
  -f ./study-builder/tf-service.yaml \
  -f ./auth-server/tf-service.yaml \
  -f ./participant-manager-datastore/tf-service.yaml \
  -f ./hydra/tf-service.yaml \
  -f ./participant-manager/tf-service.yaml
```

証明書とingressを適用します。

```bash
$ kubectl apply \
  -f ./kubernetes/cert.yaml \
  -f ./kubernetes/ingress.yaml
```

## トラブルシューティング

クラスタに問題がある場合、いくつか確認できることがあります。

* 待ってください、すべてのデプロイに時間がかかることがあります。
* `kubectl describe pods` と `kubectl logs <pod> <container>` を実行します。
    アプリケーションのログはデフォルトでは`warning`レベルに設定されていますが、
    より多くの情報が必要な場合は、ログレベルを`info`レベルに変更することを検討してください。
* Secret Managerにあるすべてのシークレットに値があり、空になっていないことを確認してください。
    シークレットの値を更新した後、./terraform/kubernets で `terraform init` と 
    `terraform apply` を実行して Kubernetes のシークレットを更新してください。
* Pod Security Policesが適用されていることを確認してください。
    クラスタはエンフォースメントを有効にしており、Pod Security Policesが適用されていない場合は
    コンテナを起動しません。
* クラスターのingressが健全であることを確認してください。
* トラブルシューティングガイドに従ってください。例えば、
    [このページ](https://learnk8s.io/troubleshooting-deployments) や
    [このページ](https://kubernetes.io/docs/tasks/debug-application-cluster/debug-cluster/)です。
* 現在のところ、ingress-gce の Firewalls には既知の問題があります。
    [kubernetes/ingress-gce#485](https://github.com/kubernetes/ingress-gce/issues/485)
    および/または
    [kubernetes/ingress-gce#584](https://github.com/kubernetes/ingress-gce/issues/584)
    を参照してください。
    1. `kubectl describe ingress <ingress-name>`を実行します。
    1. "Events"の下の"Firewall
        change required by network admin"という形で提案されている`<gcloud command>`コマンドを見てみましょう。
    1. 提案されたコマンドをそれぞれ実行します。
