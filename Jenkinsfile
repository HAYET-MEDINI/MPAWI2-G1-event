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
    }

    environment {
        JDK_HOME = 'C:\\Program Files\\Java\\jdk-21.0.12'
        MAVEN_HOME = 'C:\\Users\\Hayet\\apache-maven-3.9.9'

        GIT_REPO_URL = 'https://github.com/HAYET-MEDINI/MPAWI2-G1-event.git'
        GIT_BRANCH = 'Hayet-Medini'

        SONAR_URL = 'http://localhost:9000'
        SONAR_PROJECT_KEY = 'eventsProject'
        MAVEN_SETTINGS = 'C:\\Users\\Hayet\\.m2\\settings.xml'
    }

    stages {
        stage('1. Git Checkout') {
            steps {
                git branch: "${GIT_BRANCH}", url: "${GIT_REPO_URL}"
            }
        }

        stage('2. Maven Clean & Compile') {
            steps {
                bat '''
                    @echo off
                    set "JAVA_HOME=%JDK_HOME%"
                    set "PATH=%JDK_HOME%\\bin;%MAVEN_HOME%\\bin;%PATH%"
                    call mvn clean compile
                '''
            }
        }

        stage('3. Unit Tests & JaCoCo') {
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

        stage('4. SonarQube Analysis') {
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

        stage('5. Package JAR') {
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

        stage('6. Build Docker Image') {
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

        stage('7. Push Docker Image') {
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

        stage('8. Deploy Snapshot to Nexus') {
            steps {
                bat '''
                    @echo off
                    set "JAVA_HOME=%JDK_HOME%"
                    set "PATH=%JDK_HOME%\\bin;%MAVEN_HOME%\\bin;%PATH%"
                    call mvn deploy -DskipTests -s "%MAVEN_SETTINGS%"
                '''
            }
        }

        stage('9. Deploy Release to Nexus') {
            when {
                expression { params.DEPLOY_RELEASE }
            }
            steps {
                script {
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

    post {
        success {
            echo 'Pipeline terminé avec succès.'
        }
        failure {
            echo 'Pipeline en échec. Consulte la Console Output.'
        }
    }
}
