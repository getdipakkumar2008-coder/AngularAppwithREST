# AWS deployment

This repository includes GitHub Actions workflows for continuous integration and AWS deployment of both applications.

## Workflows

### `.github/workflows/backend-ci.yml`
- Triggers on backend changes, pull requests, and manual dispatch.
- Runs in `backend/`.
- Sets up Temurin Java 25 with Maven cache.
- Executes `mvn -B clean verify` so compilation and JUnit/Testcontainers tests must pass.
- Uploads the built JAR and Surefire reports as workflow artifacts.

### `.github/workflows/frontend-ci.yml`
- Triggers on frontend changes, pull requests, and manual dispatch.
- Runs in `frontend/`.
- Sets up Node.js 20 with npm cache from `frontend/package-lock.json`.
- Executes `npm ci`, `npm test -- --watch=false --browsers=ChromeHeadless`, and `npm run build`.
- Uploads the built `frontend/dist/` output as a workflow artifact.

### `.github/workflows/deploy-aws.yml`
- Triggers on pushes to `main` and manual dispatch.
- Uses GitHub OIDC to assume an AWS IAM role without long-lived AWS keys.
- Has two deployment jobs:
  - `deploy-backend`: runs backend tests, builds the backend Docker image, pushes `latest` and `${GITHUB_SHA}` tags to Amazon ECR, then deploys the rendered ECS task definition.
  - `deploy-frontend`: runs frontend tests, builds the frontend Docker image, pushes `latest` and `${GITHUB_SHA}` tags to Amazon ECR, then deploys the rendered ECS task definition.
- In both jobs, testing happens before Docker images are pushed or ECS is updated.

## Required GitHub configuration

### Secrets

| Name | Purpose |
| --- | --- |
| `AWS_ROLE_ARN` | ARN of the IAM role that GitHub Actions assumes through OIDC. |

### Variables

| Name | Purpose |
| --- | --- |
| `AWS_REGION` | AWS region for ECR and ECS (for example `ap-south-1`). Used as a fallback if the `AWS_REGION` variable is not defined. |
| `ECR_BACKEND_REPOSITORY` | Name of the backend Amazon ECR repository. |
| `ECR_FRONTEND_REPOSITORY` | Name of the frontend Amazon ECR repository. |
| `ECS_CLUSTER` | Amazon ECS cluster name hosting both services. |
| `ECS_BACKEND_SERVICE` | Amazon ECS service name for the backend deployment. |
| `ECS_FRONTEND_SERVICE` | Amazon ECS service name for the frontend deployment. |
| `ECS_BACKEND_TASK_DEFINITION` | Path to the backend task definition template, typically `.aws/backend-task-definition.json`. |
| `ECS_FRONTEND_TASK_DEFINITION` | Path to the frontend task definition template, typically `.aws/frontend-task-definition.json`. |
| `ECS_BACKEND_CONTAINER_NAME` | Backend container name in the task definition template. Set this to `backend-app` unless you rename the template container. |
| `ECS_FRONTEND_CONTAINER_NAME` | Frontend container name in the task definition template. Set this to `frontend-app` unless you rename the template container. |

The deployment workflow resolves the AWS region from `vars.AWS_REGION` first and falls back to `secrets.AWS_REGION`, so either location is accepted.

## AWS prerequisites

Before enabling the deployment workflow, create the following AWS resources:

1. **Two Amazon ECR repositories**
   - One for the Spring Boot backend image.
   - One for the Angular/Nginx frontend image.

2. **Amazon ECS (Fargate) infrastructure**
   - An ECS cluster.
   - A backend ECS service.
   - A frontend ECS service.
   - Networking for Fargate tasks (subnets, security groups, load balancer/target groups as needed).

3. **Task definition templates**
   - This repository includes starter templates at:
     - `.aws/backend-task-definition.json`
     - `.aws/frontend-task-definition.json`
   - Update the placeholder `executionRoleArn` and `taskRoleArn` values before deploying.
   - Keep the container names aligned with the GitHub variables used by the workflow.

4. **IAM roles**
   - **GitHub OIDC deploy role**: assumed by GitHub Actions via `AWS_ROLE_ARN`.
   - **ECS task execution role**: referenced by the task definitions and typically attached to the AWS-managed policy `AmazonECSTaskExecutionRolePolicy`.
   - **Optional ECS task role**: referenced by the task definitions if the applications need AWS API access at runtime.

## OIDC trust policy scope

Scope the IAM trust relationship to this repository so only workflows from this repo can assume the role.

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Federated": "arn:aws:iam::<aws-account-id>:oidc-provider/token.actions.githubusercontent.com"
      },
      "Action": "sts:AssumeRoleWithWebIdentity",
      "Condition": {
        "StringEquals": {
          "token.actions.githubusercontent.com:aud": "sts.amazonaws.com"
        },
        "StringLike": {
          "token.actions.githubusercontent.com:sub": "repo:getdipakkumar2008-coder/AngularAppwithREST:ref:refs/heads/main"
        }
      }
    }
  ]
}
```

If you want to allow other branches or environments later, widen the `sub` condition deliberately instead of removing it.

## IAM permissions for the GitHub OIDC deploy role

For a quick start, attach AWS-managed policies that cover the workflow actions, then tighten them later:

- `AmazonEC2ContainerRegistryPowerUser`
- `AmazonECS_FullAccess`

Also add an inline policy granting `iam:PassRole` only for the ECS task execution role and ECS task role ARNs referenced by your task definitions.

For production, replace broad managed policies with a least-privilege policy limited to:
- ECR login and image push actions for the two target repositories.
- ECS task definition registration and service update actions for the target cluster/services.
- `iam:PassRole` for only the task roles used by these services.

## Deployment notes

- The backend Docker image exposes port `8080`.
- The frontend Docker image serves the Angular build from Nginx on port `80`.
- The provided Nginx configuration supports SPA routing with fallback to `index.html`.
- If you want the frontend container to reverse-proxy `/api`, update `frontend/nginx.conf` with the correct internal backend hostname or load balancer endpoint for your AWS environment.
