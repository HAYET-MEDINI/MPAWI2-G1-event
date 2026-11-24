pipeline {
    agent any

    options {
        timestamps()
        skipDefaultCheckout(true)
    }

    parameters {
        booleanParam(name: 'DEPLOY_RELEASE', defaultValue: false, description: 'Publier aussi une version Release dans Nexus')
        string(name: 'RELEASE_VERSION', defaultValue: '1.0.1', description: 'Version Release au format x.y.z')
    }

    environment {
        JAVA_HOME = 'C:/jdk-21.0.12_windows-x64_bin/jdk-21.0.12'
        MAVEN_HOME = 'C:/Users/Omega Informatique/Documents/apache-maven-3.9.16-bin/apache-maven-3.9.16'
        DOCKER_HOME = 'C:/Users/Omega Informatique/AppData/Local/Programs/DockerDesktop/resources/bin'
        KUBECTL = 'C:/Users/Omega Informatique/AppData/Local/Programs/DockerDesktop/resources/bin/kubectl.exe'
        KUBECONFIG = 'C:/Users/Omega Informatique/.kube/config'

        GIT_REPO_URL = 'https://github.com/HAYET-MEDINI/MPAWI2-G1-event.git'
        APP_GIT_BRANCH = 'Emna-cherni'
        SONAR_URL = 'http://localhost:9000'
        SONAR_PROJECT_KEY = 'eventsProject'
        NEXT_SNAPSHOT_VERSION = '1.0.1-SNAPSHOT'
        DOCKER_IMAGE_NAME = 'eventsproject'

        K8S_NAMESPACE = 'eventsproject'
        K8S_APP_PORT = '8080'
        K8S_NODEPORT = '30080'
    }


    stages {
        stage('1. CHECKOUT GIT') {
            steps {
                git branch: "${APP_GIT_BRANCH}", url: "${GIT_REPO_URL}"
            }
        }

        stage('2. MVN CLEAN') {
            steps {
                bat '''
                    @echo off
                    set "PATH=%JAVA_HOME%\\bin;%MAVEN_HOME%\\bin;%PATH%"
                    java --version
                    call mvn --version
                    call mvn -B clean compile
                    if errorlevel 1 exit /b 1
                '''
            }
        }

        stage('3. ARTIFACT CONSTRUCTION') {
            steps {
                bat '''
                    @echo off
                    set "PATH=%JAVA_HOME%\\bin;%MAVEN_HOME%\\bin;%PATH%"
                    call mvn -B package -DskipTests
                    if errorlevel 1 exit /b 1
                '''
            }
        }

        stage('4. UNIT TESTS') {
            steps {
                bat '''
                    @echo off
                    set "PATH=%JAVA_HOME%\\bin;%MAVEN_HOME%\\bin;%PATH%"
                    call mvn -B test jacoco:report
                    if errorlevel 1 exit /b 1
                '''
            }
            post {
                always {
                    junit(testResults: '**/target/surefire-reports/*.xml', allowEmptyResults: true)
                    archiveArtifacts(artifacts: 'target/site/jacoco/**', allowEmptyArchive: true)
                }
                success {
                    archiveArtifacts(artifacts: 'target/eventsProject-*.jar', excludes: 'target/*.jar.original', fingerprint: true)
                }
            }
        }

        stage('5. MVN SONARQUBE') {
            steps {
                withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
                    bat '''
                        @echo off
                        set "PATH=%JAVA_HOME%\\bin;%MAVEN_HOME%\\bin;%PATH%"
                        call mvn -B org.sonarsource.scanner.maven:sonar-maven-plugin:4.0.0.4121:sonar ^
                            -Dsonar.projectKey=%SONAR_PROJECT_KEY% ^
                            -Dsonar.host.url=%SONAR_URL% ^
                            -Dsonar.token=%SONAR_TOKEN% ^
                            -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
                        if errorlevel 1 exit /b 1
                    '''
                }
            }
        }

        stage('6. PUBLISH TO NEXUS') {
            when {
                expression { env.APP_GIT_BRANCH == 'Emna-cherni' }
            }
            steps {
                withCredentials([file(credentialsId: 'nexus-maven-settings', variable: 'MAVEN_SETTINGS')]) {
                    bat '''
                        @echo off
                        set "PATH=%JAVA_HOME%\\bin;%MAVEN_HOME%\\bin;%PATH%"
                        call mvn -B -s "%MAVEN_SETTINGS%" deploy -DskipTests
                        if errorlevel 1 exit /b 1
                    '''
                }

                script {
                    if (params.DEPLOY_RELEASE) {
                        if (!(params.RELEASE_VERSION ==~ /^\\d+\\.\\d+\\.\\d+$/)) {
                            error('RELEASE_VERSION doit être au format x.y.z, exemple 1.0.1')
                        }
                        withCredentials([file(credentialsId: 'nexus-maven-settings', variable: 'MAVEN_SETTINGS')]) {
                            withEnv(["RELEASE_VERSION=${params.RELEASE_VERSION}"]) {
                                try {
                                    bat '''
                                        @echo off
                                        set "PATH=%JAVA_HOME%\\bin;%MAVEN_HOME%\\bin;%PATH%"
                                        call mvn -B versions:set -DnewVersion=%RELEASE_VERSION% -DgenerateBackupPoms=false
                                        if errorlevel 1 exit /b 1
                                        call mvn -B -s "%MAVEN_SETTINGS%" deploy -DskipTests
                                        if errorlevel 1 exit /b 1
                                    '''
                                } finally {
                                    bat '''
                                        @echo off
                                        set "PATH=%JAVA_HOME%\\bin;%MAVEN_HOME%\\bin;%PATH%"
                                        call mvn -B versions:set -DnewVersion=%NEXT_SNAPSHOT_VERSION% -DgenerateBackupPoms=false
                                        if errorlevel 1 exit /b 1
                                    '''
                                }
                            }
                        }
                    }
                }
            }
        }

        stage('7. BUILDING OUR IMAGE') {
            when {
                expression { fileExists('Dockerfile') }
            }
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', usernameVariable: 'DOCKERHUB_USER', passwordVariable: 'DOCKERHUB_TOKEN')]) {
                    bat '''
                        @echo off
                        set "PATH=%DOCKER_HOME%;%PATH%"
                        docker --version
                        docker info >nul 2>&1
                        if errorlevel 1 (
                            echo Docker daemon is unavailable to Jenkins.
                            exit /b 1
                        )
                        docker build ^
                            -t "%DOCKERHUB_USER%/eventsproject:%BUILD_NUMBER%" ^
                            -t "%DOCKERHUB_USER%/eventsproject:latest" .
                        if errorlevel 1 exit /b 1
                    '''
                }
            }
        }

        stage('8. DEPLOY OUR IMAGE') {
            options {
                timeout(time: 30, unit: 'MINUTES')
            }
            when {
                expression { fileExists('Dockerfile') }
            }
            steps {
                retry(3) {
                    withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', usernameVariable: 'DOCKERHUB_USER', passwordVariable: 'DOCKERHUB_TOKEN')]) {
                        script {
                            try {
                                bat '''
                                    @echo off
                                    set "PATH=%DOCKER_HOME%;%PATH%"
                                    powershell.exe -NoProfile -NonInteractive -Command "$env:DOCKERHUB_TOKEN | docker login --username $env:DOCKERHUB_USER --password-stdin"
                                    if errorlevel 1 exit /b 1
                                    docker push "%DOCKERHUB_USER%/eventsproject:%BUILD_NUMBER%"
                                    if errorlevel 1 exit /b 1
                                    docker push "%DOCKERHUB_USER%/eventsproject:latest"
                                    if errorlevel 1 exit /b 1
                                '''
                            } finally {
                                bat '''
                                    @echo off
                                    set "PATH=%DOCKER_HOME%;%PATH%"
                                    docker logout
                                '''
                            }
                        }
                    }
                }
            }
        }

        stage('9. Test AWS Credentials') {
            steps {
                withCredentials([file(credentialsId: 'aws-credentials', variable: 'AWS_CREDENTIALS_FILE')]) {
                    bat '''
                        @echo off
                        docker run --rm ^
                            -v "%AWS_CREDENTIALS_FILE%:/root/.aws/credentials:ro" ^
                            -e AWS_SHARED_CREDENTIALS_FILE=/root/.aws/credentials ^
                            amazon/aws-cli sts get-caller-identity
                        if errorlevel 1 exit /b 1
                    '''
                }
            }
        }

        stage('10. SETUP TERRAFORM') {
            steps {
                withCredentials([file(credentialsId: 'aws-credentials', variable: 'AWS_CREDENTIALS_FILE')]) {
                    dir('terraform') {
                        bat '''
                            @echo off
                            echo === Terraform Init ===
                            docker run --rm ^
                                -v "%WORKSPACE%:/workspace" ^
                                -v "%AWS_CREDENTIALS_FILE%:/root/.aws/credentials:ro" ^
                                -e AWS_SHARED_CREDENTIALS_FILE=/root/.aws/credentials ^
                                -w /workspace/terraform ^
                                hashicorp/terraform:latest init
                            if errorlevel 1 exit /b 1

                            echo === Terraform Validate ===
                            docker run --rm ^
                                -v "%WORKSPACE%:/workspace" ^
                                -v "%AWS_CREDENTIALS_FILE%:/root/.aws/credentials:ro" ^
                                -e AWS_SHARED_CREDENTIALS_FILE=/root/.aws/credentials ^
                                -w /workspace/terraform ^
                                hashicorp/terraform:latest validate
                            if errorlevel 1 exit /b 1

                            echo Terraform Apply skipped: AWS Academy SCP restriction.
                        '''
                    }
                }
            }
        }

stage('11. UPDATE KUBECONFIG') {
    steps {
        bat '''
            @echo off

            echo === Check Kubeconfig ===
            if not exist "%KUBECONFIG%" (
                echo Kubeconfig file not found: %KUBECONFIG%
                exit /b 1
            )

            echo === Set Minikube Context ===
            "%KUBECTL%" config use-context minikube
            if errorlevel 1 exit /b 1

            echo === Current Context ===
            "%KUBECTL%" config current-context
            if errorlevel 1 exit /b 1

            echo === Check Kubernetes Cluster ===
            "%KUBECTL%" cluster-info
            if errorlevel 1 exit /b 1

            echo === Check Nodes ===
            "%KUBECTL%" get nodes
            if errorlevel 1 exit /b 1

            echo === Create Namespace if necessary ===
            "%KUBECTL%" create namespace eventsproject --dry-run=client -o yaml | "%KUBECTL%" apply -f -
            if errorlevel 1 exit /b 1

            echo === Namespace ===
            "%KUBECTL%" get namespace eventsproject
        '''
    }
}


stage('12. DEPLOY TO MINIKUBE KUBERNETES') {
    options {
        timeout(time: 30, unit: 'MINUTES')
    }

    steps {
        withCredentials([
            usernamePassword(
                credentialsId: 'dockerhub-credentials',
                usernameVariable: 'DOCKERHUB_USER',
                passwordVariable: 'DOCKERHUB_TOKEN'
            )
        ]) {
            script {

                def deploymentYaml = """
apiVersion: apps/v1
kind: Deployment
metadata:
  name: eventsproject
  namespace: ${env.K8S_NAMESPACE}
  labels:
    app: eventsproject
spec:
  replicas: 1
  selector:
    matchLabels:
      app: eventsproject
  template:
    metadata:
      labels:
        app: eventsproject
    spec:
      containers:
      - name: eventsproject
        image: ${env.DOCKERHUB_USER}/eventsproject:${env.BUILD_NUMBER}
        imagePullPolicy: Always
        ports:
        - containerPort: ${env.K8S_APP_PORT}
        readinessProbe:
          tcpSocket:
            port: ${env.K8S_APP_PORT}
          initialDelaySeconds: 20
          periodSeconds: 10
        livenessProbe:
          tcpSocket:
            port: ${env.K8S_APP_PORT}
          initialDelaySeconds: 40
          periodSeconds: 20
"""

                def serviceYaml = """
apiVersion: v1
kind: Service
metadata:
  name: eventsproject-service
  namespace: ${env.K8S_NAMESPACE}
spec:
  type: NodePort
  selector:
    app: eventsproject
  ports:
  - name: http
    port: ${env.K8S_APP_PORT}
    targetPort: ${env.K8S_APP_PORT}
    nodePort: ${env.K8S_NODEPORT}
"""

                writeFile(
                    file: 'k8s-deployment.yaml',
                    text: deploymentYaml
                )

                writeFile(
                    file: 'k8s-service.yaml',
                    text: serviceYaml
                )

                bat '''
                    @echo off

                    echo =====================================
                    echo       DEPLOY MYSQL
                    echo =====================================

                    if not exist "k8s\\mysql.yaml" (
                        echo ERROR: k8s\\mysql.yaml not found
                        exit /b 1
                    )

                    "%KUBECTL%" apply -f k8s\\mysql.yaml
                    if errorlevel 1 exit /b 1

                    echo === Wait for MySQL ===

                    "%KUBECTL%" rollout status deployment/mysql -n eventsproject --timeout=180s

                    if errorlevel 1 (
                        echo MySQL deployment failed
                        "%KUBECTL%" get pods -n eventsproject
                        "%KUBECTL%" describe deployment mysql -n eventsproject
                        exit /b 1
                    )

                    echo =====================================
                    echo       DEPLOY APPLICATION
                    echo =====================================

                    "%KUBECTL%" apply -f k8s-deployment.yaml
                    if errorlevel 1 exit /b 1

                    "%KUBECTL%" apply -f k8s-service.yaml
                    if errorlevel 1 exit /b 1

                    echo === Wait for Application ===

                    "%KUBECTL%" rollout status deployment/eventsproject -n eventsproject --timeout=180s

                    if errorlevel 1 (
                        echo Application deployment failed

                        echo === Deployment Details ===
                        "%KUBECTL%" describe deployment eventsproject -n eventsproject

                        echo === Pods ===
                        "%KUBECTL%" get pods -n eventsproject -o wide

                        echo === Application Logs ===
                        "%KUBECTL%" logs deployment/eventsproject -n eventsproject --all-containers=true

                        exit /b 1
                    )

                    echo =====================================
                    echo       DEPLOYMENT SUCCESS
                    echo =====================================

                    echo === Deployments ===
                    "%KUBECTL%" get deployments -n eventsproject

                    echo === Pods ===
                    "%KUBECTL%" get pods -n eventsproject -o wide

                    echo === Services ===
                    "%KUBECTL%" get services -n eventsproject

                    echo === Application Service ===
                    "%KUBECTL%" get service eventsproject-service -n eventsproject
                '''
            }
        }
    }
}


    } // close stages

    post {
        success {
            echo 'Pipeline terminé avec succès.'
        }
        failure {
            echo 'Pipeline en échec. Consultez la sortie de la console.'
        }
        always {
            echo 'Fin du pipeline.'
        }
    }
}
