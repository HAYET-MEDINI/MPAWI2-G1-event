pipeline {
    agent any

    options {
        timestamps()
    }

    parameters {
        booleanParam(
            name: 'DEPLOY_RELEASE',
            defaultValue: false,
            description: 'Publier aussi une version Release dans Nexus'
        )
        string(
            name: 'RELEASE_VERSION',
            defaultValue: '1.0.1',
            description: 'Version de release au format x.y.z'
        )
    }

    environment {
        JAVA_HOME = 'C:/jdk-21.0.12_windows-x64_bin/jdk-21.0.12'
        MAVEN_HOME = 'C:/Users/Omega Informatique/Documents/apache-maven-3.9.16-bin/apache-maven-3.9.16'
        GIT_REPO_URL = 'https://github.com/HAYET-MEDINI/MPAWI2-G1-event.git'
        GIT_BRANCH = 'Emna-cherni'
        SONAR_URL = 'http://localhost:9000'
        SONAR_PROJECT_KEY = 'eventsProject'
        NEXT_SNAPSHOT_VERSION = '1.0.1-SNAPSHOT'
    }

    stages {
        stage('1. Git checkout') {
            steps {
                git branch: "${GIT_BRANCH}", url: "${GIT_REPO_URL}"
            }
        }

        stage('2. Maven clean and compile') {
            steps {
                bat '''
                    @echo off
                    set "PATH=%JAVA_HOME%/bin;%MAVEN_HOME%/bin;%PATH%"
                    java --version
                    mvn --version
                    call mvn -B clean compile
                '''
            }
        }

        stage('3. Unit tests and JaCoCo') {
            steps {
                bat '''
                    @echo off
                    set "PATH=%JAVA_HOME%/bin;%MAVEN_HOME%/bin;%PATH%"
                    call mvn -B test jacoco:report
                '''
            }
            post {
                always {
                    junit testResults: '**/target/surefire-reports/*.xml', allowEmptyResults: true
                    archiveArtifacts artifacts: 'target/site/jacoco/**', allowEmptyArchive: true
                }
            }
        }

        stage('4. SonarQube analysis') {
            steps {
                withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
                    bat '''
                        @echo off
                        set "PATH=%JAVA_HOME%/bin;%MAVEN_HOME%/bin;%PATH%"
                        call mvn -B org.sonarsource.scanner.maven:sonar-maven-plugin:4.0.0.4121:sonar -Dsonar.projectKey=%SONAR_PROJECT_KEY% -Dsonar.host.url=%SONAR_URL% -Dsonar.token=%SONAR_TOKEN%
                    '''
                }
            }
        }

        stage('5. Package JAR') {
            steps {
                bat '''
                    @echo off
                    set "PATH=%JAVA_HOME%/bin;%MAVEN_HOME%/bin;%PATH%"
                    call mvn -B package -DskipTests
                '''
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/eventsProject-*.jar', excludes: 'target/*.jar.original', fingerprint: true
                }
            }
        }

        stage('6. Build Docker image') {
            when {
                expression {
                    fileExists('Dockerfile')
                }
            }
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

        stage('7. Push Docker image') {
            when {
                expression {
                    fileExists('Dockerfile')
                }
            }
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-credentials',
                    usernameVariable: 'DOCKERHUB_USER',
                    passwordVariable: 'DOCKERHUB_TOKEN'
                )]) {
                    script {
                        docker.withRegistry('https://index.docker.io/v1/', 'dockerhub-credentials') {
                            docker.image("${env.DOCKERHUB_USER}/eventsproject:${env.BUILD_NUMBER}").push()
                        }
                    }
                }
            }
        }

        stage('8. Deploy snapshot to Nexus') {
            when {
                expression {
                    env.GIT_BRANCH == 'Emna-cherni' || env.GIT_BRANCH == 'origin/Emna-cherni'
                }
            }
            steps {
                withCredentials([file(credentialsId: 'nexus-maven-settings', variable: 'MAVEN_SETTINGS')]) {
                    bat '''
                        @echo off
                        set "PATH=%JAVA_HOME%/bin;%MAVEN_HOME%/bin;%PATH%"
                        call mvn -B -s "%MAVEN_SETTINGS%" deploy -DskipTests
                    '''
                }
            }
        }

        stage('9. Deploy release to Nexus') {
            when {
                allOf {
                    expression {
                        env.GIT_BRANCH == 'Emna-cherni' || env.GIT_BRANCH == 'origin/Emna-cherni'
                    }
                    expression {
                        params.DEPLOY_RELEASE
                    }
                }
            }
            steps {
                script {
                    if (!(params.RELEASE_VERSION ==~ /\d+\.\d+\.\d+/)) {
                        error('RELEASE_VERSION doit être au format x.y.z, par exemple 1.0.1')
                    }
                }
                withCredentials([file(credentialsId: 'nexus-maven-settings', variable: 'MAVEN_SETTINGS')]) {
                    script {
                        try {
                            bat """
                                @echo off
                                set "PATH=%JAVA_HOME%/bin;%MAVEN_HOME%/bin;%PATH%"
                                call mvn -B versions:set -DnewVersion=${params.RELEASE_VERSION} -DgenerateBackupPoms=false
                                if errorlevel 1 exit /b %ERRORLEVEL%
                                call mvn -B -s "%MAVEN_SETTINGS%" deploy -DskipTests
                            """
                        } finally {
                            bat '''
                                @echo off
                                set "PATH=%JAVA_HOME%/bin;%MAVEN_HOME%/bin;%PATH%"
                                call mvn -B versions:set -DnewVersion=%NEXT_SNAPSHOT_VERSION% -DgenerateBackupPoms=false
                            '''
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
            echo 'Pipeline en échec. Consultez la sortie de la console.'
        }
        always {
            echo 'Fin du pipeline.'
        }
    }
}