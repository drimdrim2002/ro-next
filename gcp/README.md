# GCP 배포

이 구성은 **Cloud Run API + Cloud Run worker + Workflows + Cloud Storage**를 사용한다. 두 Cloud Run 서비스는 기본적으로 scale-to-zero이며, 결과와 ALNS 후보만 Cloud Storage에 남긴다.

## 사전 준비

```bash
export PROJECT_ID="your-gcp-project-id"
export REGION="asia-northeast3"
export REPOSITORY="ro-next"
export BUCKET="${PROJECT_ID}-ro-next-results"
export API_SERVICE="ro-next-api"
export WORKER_SERVICE="ro-next-worker"
export WORKFLOW="ro-next-optimization"
export API_SA="ro-next-api"
export WORKFLOW_SA="ro-next-workflow"
export WORKER_SA="ro-next-worker"

gcloud config set project "$PROJECT_ID"
gcloud services enable run.googleapis.com workflows.googleapis.com \
  workflowexecutions.googleapis.com cloudbuild.googleapis.com artifactregistry.googleapis.com storage.googleapis.com
gcloud artifacts repositories create "$REPOSITORY" --repository-format=docker --location="$REGION"
gcloud storage buckets create "gs://${BUCKET}" --location="$REGION"
gcloud iam service-accounts create "$API_SA"
gcloud iam service-accounts create "$WORKFLOW_SA"
gcloud iam service-accounts create "$WORKER_SA"

export API_SA_EMAIL="${API_SA}@${PROJECT_ID}.iam.gserviceaccount.com"
export WORKFLOW_SA_EMAIL="${WORKFLOW_SA}@${PROJECT_ID}.iam.gserviceaccount.com"
export WORKER_SA_EMAIL="${WORKER_SA}@${PROJECT_ID}.iam.gserviceaccount.com"
gcloud projects add-iam-policy-binding "$PROJECT_ID" --member="serviceAccount:${API_SA_EMAIL}" --role="roles/workflows.invoker"
gcloud storage buckets add-iam-policy-binding "gs://${BUCKET}" --member="serviceAccount:${API_SA_EMAIL}" --role="roles/storage.objectViewer"
gcloud storage buckets add-iam-policy-binding "gs://${BUCKET}" --member="serviceAccount:${WORKER_SA_EMAIL}" --role="roles/storage.objectAdmin"
```

## 빌드와 Cloud Run 배포

```bash
gcloud builds submit --config=gcp/cloudbuild.yaml \
  --substitutions=_REGION="$REGION",_REPOSITORY="$REPOSITORY",_IMAGE=optimizer,_TAG=dev

export IMAGE="${REGION}-docker.pkg.dev/${PROJECT_ID}/${REPOSITORY}/optimizer:dev"

gcloud run deploy "$WORKER_SERVICE" --image="$IMAGE" --region="$REGION" \
  --no-allow-unauthenticated --min-instances=0 --timeout=900 \
  --service-account="$WORKER_SA_EMAIL" \
  --set-env-vars=SERVICE_MODE=worker,RESULTS_BUCKET="$BUCKET"

export WORKER_URL="$(gcloud run services describe "$WORKER_SERVICE" --region="$REGION" --format='value(status.url)')"
```

Workflows 실행 서비스 계정에 worker 호출 권한을 부여한 뒤 workflow를 배포한다.

```bash
gcloud run services add-iam-policy-binding "$WORKER_SERVICE" --region="$REGION" \
  --member="serviceAccount:${WORKFLOW_SA_EMAIL}" --role="roles/run.invoker"

gcloud workflows deploy "$WORKFLOW" --location="$REGION" \
  --source=gcp/workflows/optimization.yaml \
  --service-account="$WORKFLOW_SA_EMAIL" \
  --set-env-vars=WORKER_URL="$WORKER_URL"

export WORKFLOW_NAME="projects/${PROJECT_ID}/locations/${REGION}/workflows/${WORKFLOW}"
```

마지막으로 API Cloud Run 서비스 계정에는 `roles/workflows.invoker`, worker 서비스 계정에는 결과 bucket의 `roles/storage.objectAdmin`을 부여한다.

```bash
gcloud run deploy "$API_SERVICE" --image="$IMAGE" --region="$REGION" \
  --allow-unauthenticated --min-instances=0 --timeout=30 \
  --service-account="$API_SA_EMAIL" \
  --set-env-vars=SERVICE_MODE=api,RESULTS_BUCKET="$BUCKET",WORKFLOW_NAME="$WORKFLOW_NAME"
```

공개 API가 필요 없으면 마지막 명령의 `--allow-unauthenticated`를 제거하고 IAM/IAP 또는 API Gateway를 앞에 둔다. 각각의 서비스 계정은 API 실행, worker 호출, 객체 읽기/쓰기 권한만 가진다.

## 비용 및 확장

- API와 worker 모두 `min-instances=0`이므로 유휴 Cloud Run 비용은 없다.
- Cloud Run worker는 요청 단위로 ALNS batch를 실행한다. 15분 이상 또는 높은 CPU 병렬성이 필요해지면 Workflows의 `runAlnsBatches` 단계만 Cloud Run Job 또는 GKE Job 호출로 교체한다.
- `inputUri`와 결과는 반드시 같은 리전의 Cloud Storage bucket을 사용해 네트워크 비용과 지연을 줄인다.
