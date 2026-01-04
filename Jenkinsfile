pipeline {
  agent { label 'java_builder' }

  environment {
    // ---- App / Image ----
    APP_NAME      = "students-service"
    IMAGE_REPO    = "docker.io/omaralamad/students-service"
    IMAGE_TAG     = "${env.BUILD_NUMBER}-${env.GIT_COMMIT.take(7)}"
    FULL_IMAGE    = "${IMAGE_REPO}:${IMAGE_TAG}"
    HELM_NS       = "students-service-by-jenkins"
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
          dockerd --host=unix:///var/run/docker.sock >/tmp/dockerd.log 2>&1 &
          sleep 2
          docker version
          docker build -t "$FULL_IMAGE" .
        '''
      }
    }

    stage('Docker Push') {
      steps {
        withCredentials([usernamePassword(
          credentialsId: 'docker-registry-creds',
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
