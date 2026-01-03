pipeline {
  agent { label 'java-builder' }

  environment {
    // ---- App / Image ----
    APP_NAME      = "my-java-service"
    IMAGE_REPO    = "registry.example.com/my-team/my-java-service"   // change
    IMAGE_TAG     = "${env.BUILD_NUMBER}-${env.GIT_COMMIT.take(7)}"
    FULL_IMAGE    = "${IMAGE_REPO}:${IMAGE_TAG}"

    // ---- Optional: Maven local repo cache inside workspace ----
    MAVEN_OPTS    = "-Dmaven.repo.local=.m2/repository"
  }

  options {
    timestamps()
    disableConcurrentBuilds()
  }

  stages {

    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Build & Test') {
      steps {
        sh '''
          set -euxo pipefail
          java -version
          mvn -version
          mvn -B clean test package
        '''
      }
      post {
        always {
          junit 'target/surefire-reports/*.xml'
          archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
        }
      }
    }

    stage('Docker Build') {
      steps {
        sh '''
          set -euxo pipefail
          docker version
          docker build -t "$FULL_IMAGE" .
        '''
      }
    }

    stage('Docker Push') {
      steps {
        withCredentials([usernamePassword(
          credentialsId: 'docker-registry-creds',  // create this in Jenkins
          usernameVariable: 'REG_USER',
          passwordVariable: 'REG_PASS'
        )]) {
          sh '''
            set -euxo pipefail
            echo "$REG_PASS" | docker login -u "$REG_USER" --password-stdin "$(echo "$IMAGE_REPO" | cut -d/ -f1)"
            docker push "$FULL_IMAGE"
          '''
        }
      }
    }

    stage('Deploy Helm') {
      steps {
        sh '''
          set -euxo pipefail
          helm version
          kubectl version --client

          kubectl get ns "$HELM_NS" >/dev/null 2>&1 || kubectl create ns "$HELM_NS"

          helm upgrade --install "$HELM_RELEASE" "$HELM_CHART" \
            -n "$HELM_NS" \
            --set image.repository="$IMAGE_REPO" \
            --set image.tag="$IMAGE_TAG" \
            --wait --timeout 10m
        '''
      }
    }
  }

  post {
    success {
      echo "✅ Deployed ${FULL_IMAGE} with Helm release ${HELM_RELEASE} in namespace ${HELM_NS}"
    }
    failure {
      echo "❌ Pipeline failed"
    }
  }
}
