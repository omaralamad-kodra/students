pipeline {
  agent { label 'java_builder' }

  environment {
    // ---- App / Image ----
    APP_NAME      = "students-service"
    IMAGE_REPO    = "docker.io/omaralamad/students-service"
    IMAGE_TAG     = "${env.BUILD_NUMBER}-${env.GIT_COMMIT.take(7)}"
    FULL_IMAGE    = "${IMAGE_REPO}:${IMAGE_TAG}"
    HELM_NS       = "students-service-by-jenkins"
    HELM_RELEASE  = "students-service-by-jenkins"
  }

  stages {

    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Test') {
      steps {
        sh '''
          set -euxo pipefail
          java -version
          mvn -version
          mvn -B clean test
        '''
      }
      post {
        always {
          junit 'target/surefire-reports/*.xml'
        }
      }
    }
    
    stage('Build') {
      steps {
        sh '''
          set -euxo pipefail
          mvn -B -DskipTests package
        '''
      }
      post {
        always {
          archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
        }
      }
    }

    stage('Docker Build & Push') {
      steps {
        withCredentials([usernamePassword(
          credentialsId: 'docker-registry-creds',
          usernameVariable: 'REG_USER',
          passwordVariable: 'REG_PASS'
        )]) {
          sh '''
            set -euxo pipefail
    
            # Start Docker daemon (DinD) if it's not already running
            if ! docker info >/dev/null 2>&1; then
              dockerd --host=unix:///var/run/docker.sock >/tmp/dockerd.log 2>&1 &
              # Wait for dockerd
              for i in $(seq 1 30); do
                docker info >/dev/null 2>&1 && break
                sleep 1
              done
            fi
    
            docker version
    
            # Login + build + push
            echo "$REG_PASS" | docker login -u "$REG_USER" --password-stdin "$(echo "$IMAGE_REPO" | cut -d/ -f1)"
            docker build -t "$FULL_IMAGE" .
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
          helm upgrade --install "$HELM_RELEASE" ./chart \
            -n "$HELM_NS" \
            --set image.repository="$IMAGE_REPO" \
            --set image.tag="$IMAGE_TAG" \
            --wait --timeout 10m  \
            --create-namespace
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
