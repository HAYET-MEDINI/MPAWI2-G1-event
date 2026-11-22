pipeline {
    agent any

    parameters {
        booleanParam(
            name: 'DEPLOY_RELEASE',
            defaultValue: false,
            description: 'Publier aussi une version Release dans Nexus'
        )
        string(
            name: 'RELEASE_VERSION',
            defaultValue: '1.0.1',
            description: 'Nouvelle version unique (format x.y.z), par exemple 1.0.1'
        )
        booleanParam(
            name: 'APPLY_TERRAFORM',
            defaultValue: false,
            description: 'Créer ou modifier les ressources AWS (peut entraîner des frais)'
        )
    }

    environment {
        JDK_HOME = 'C:\\Program Files\\Java\\jdk-21.0.12'
        MAVEN_HOME = 'C:\\Users\\Hayet\\apache-maven-3.9.9'
        TERRAFORM_HOME = 'D:\\terraform'
        AWS_CLI = 'C:\\Program Files\\Amazon\\AWSCLIV2\\aws.exe'

        GIT_REPO_URL = 'https://github.com/HAYET-MEDINI/MPAWI2-G1-event.git'
        GIT_BRANCH = 'Hayet-Medini'

        SONAR_URL = 'http://localhost:9000'
        SONAR_PROJECT_KEY = 'eventsProject'
        MAVEN_SETTINGS = 'C:\\Users\\Hayet\\.m2\\settings.xml'
    }

    stages {
        stage('CHECKOUT GIT') {
            steps {
                git branch: "${GIT_BRANCH}", url: "${GIT_REPO_URL}"
            }
        }

        stage('MVN CLEAN') {
            steps {
                bat '''
                    @echo off
                    set "JAVA_HOME=%JDK_HOME%"
                    set "PATH=%JDK_HOME%\\bin;%MAVEN_HOME%\\bin;%PATH%"
                    call mvn clean
                '''
            }
        }

        stage('ARTIFACT CONSTRUCTION') {
            steps {
                bat '''
                    @echo off
                    set "JAVA_HOME=%JDK_HOME%"
                    set "PATH=%JDK_HOME%\\bin;%MAVEN_HOME%\\bin;%PATH%"
                    call mvn package -DskipTests
                '''
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/eventsProject-*.jar', excludes: 'target/*.jar.original', fingerprint: true
                }
            }
        }

        stage('UNIT TESTS') {
            steps {
                bat '''
                    @echo off
                    set "JAVA_HOME=%JDK_HOME%"
                    set "PATH=%JDK_HOME%\\bin;%MAVEN_HOME%\\bin;%PATH%"
                    call mvn test
                '''
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                    archiveArtifacts allowEmptyArchive: true, artifacts: 'target/site/jacoco/**'
                }
            }
        }

        stage('MVN SONARQUBE') {
            steps {
                withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
                    bat '''
                        @echo off
                        set "JAVA_HOME=%JDK_HOME%"
                        set "PATH=%JDK_HOME%\\bin;%MAVEN_HOME%\\bin;%PATH%"
                        call mvn org.sonarsource.scanner.maven:sonar-maven-plugin:4.0.0.4121:sonar -Dsonar.projectKey=%SONAR_PROJECT_KEY% -Dsonar.host.url=%SONAR_URL% -Dsonar.token=%SONAR_TOKEN%
                    '''
                }
            }
        }

        stage('PUBLISH TO NEXUS') {
            steps {
                bat '''
                    @echo off
                    set "JAVA_HOME=%JDK_HOME%"
                    set "PATH=%JDK_HOME%\\bin;%MAVEN_HOME%\\bin;%PATH%"
                    call mvn deploy -DskipTests -s "%MAVEN_SETTINGS%"
                '''
                script {
                    if (params.DEPLOY_RELEASE) {
                        if (!(params.RELEASE_VERSION ==~ /\d+\.\d+\.\d+/)) {
                            error('RELEASE_VERSION doit être au format x.y.z, par exemple 1.0.1')
                        }
                        try {
                            bat '''
                                @echo off
                                set "JAVA_HOME=%JDK_HOME%"
                                set "PATH=%JDK_HOME%\\bin;%MAVEN_HOME%\\bin;%PATH%"
                                call mvn versions:set -DnewVersion=%RELEASE_VERSION% -DgenerateBackupPoms=false
                                if errorlevel 1 exit /b %ERRORLEVEL%
                                call mvn deploy -DskipTests -s "%MAVEN_SETTINGS%"
                            '''
                        } finally {
                            bat '''
                                @echo off
                                set "JAVA_HOME=%JDK_HOME%"
                                set "PATH=%JDK_HOME%\\bin;%MAVEN_HOME%\\bin;%PATH%"
                                call mvn versions:set -DnewVersion=1.0.0-SNAPSHOT -DgenerateBackupPoms=false
                            '''
                        }
                    }
                }
            }
        }

        stage('BUILDING OUR IMAGE') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-credentials',
                    usernameVariable: 'DOCKERHUB_USER',
                    passwordVariable: 'DOCKERHUB_TOKEN'
                )]) {
                    script {
                        docker.build("${env.DOCKERHUB_USER}/eventsproject:${env.BUILD_NUMBER}")
                    }
                }
            }
        }

        stage('DEPLOY OUR IMAGE') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-credentials',
                    usernameVariable: 'DOCKERHUB_USER',
                    passwordVariable: 'DOCKERHUB_TOKEN'
                )]) {
                    script {
                        docker.withRegistry('', 'dockerhub-credentials') {
                            docker.image("${env.DOCKERHUB_USER}/eventsproject:${env.BUILD_NUMBER}").push()
                        }
                    }
                }
            }
        }

        stage('Test AWS Credentials') {
            steps {
                withCredentials([file(credentialsId: 'aws-credentials', variable: 'AWS_SHARED_CREDENTIALS_FILE')]) {
                    withEnv(['AWS_DEFAULT_REGION=us-east-1']) {
                        bat '''
                            @echo off
                            if not exist "%AWS_CLI%" (
                                echo AWS CLI not found at "%AWS_CLI%".
                                exit /b 9009
                            )
                            "%AWS_CLI%" sts get-caller-identity --query Account --output text >NUL
                            if errorlevel 1 exit /b %ERRORLEVEL%
                            echo AWS credentials are valid.
                        '''
                    }
                }
            }
        }

        stage('SETUP TERRAFORM') {
            steps {
                dir('terraform-source') {
                    git branch: 'master', url: 'https://gitlab.com/ThourayaLouati/docker-spring-boot.git'
                    dir('terraform') {
                        withCredentials([file(credentialsId: 'aws-credentials', variable: 'AWS_SHARED_CREDENTIALS_FILE')]) {
                            withEnv(["PATH+TERRAFORM=${env.TERRAFORM_HOME}"]) {
                                bat 'terraform init'
                                bat 'terraform validate'
                                script {
                                    if (params.APPLY_TERRAFORM) {
                                        bat 'terraform apply -auto-approve'
                                    } else {
                                        echo 'Terraform apply ignoré. Coche APPLY_TERRAFORM pour déployer les ressources AWS.'
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    post {
        success {
            echo 'Pipeline terminé avec succès.'
        }
        failure {
            echo 'Pipeline en échec. Consulte la Console Output.'
        }
    }
}
