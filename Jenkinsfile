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
        SONAR_URL = 'http://localhost:9000'
        SONAR_PROJECT_KEY = 'eventsProject'
        NEXT_SNAPSHOT_VERSION = '1.0.1-SNAPSHOT'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build and test with JaCoCo') {
            steps {
                bat '''
                    @echo off
                    set "PATH=%JAVA_HOME%/bin;%MAVEN_HOME%/bin;%PATH%"
                    java --version
                    mvn --version
                    call mvn -B clean verify
                '''
            }
        }

        stage('SonarQube analysis') {
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

        stage('Archive JAR') {
            steps {
                archiveArtifacts artifacts: 'target/eventsProject-*.jar', excludes: 'target/*.jar.original', fingerprint: true
            }
        }

        stage('Build and push Docker image') {
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
                        def image = docker.build("${env.DOCKERHUB_USER}/eventsproject:${env.BUILD_NUMBER}")
                        docker.withRegistry('https://index.docker.io/v1/', 'dockerhub-credentials') {
                            image.push()
                        }
                    }
                }
            }
        }

        stage('Deploy snapshot to Nexus') {
            when {
                expression {
                    def branchName = env.BRANCH_NAME ?: env.GIT_BRANCH ?: ''
                    branchName == 'Emna-cherni' || branchName == 'origin/Emna-cherni'
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

        stage('Deploy release to Nexus') {
            when {
                allOf {
                    expression {
                        def branchName = env.BRANCH_NAME ?: env.GIT_BRANCH ?: ''
                        branchName == 'Emna-cherni' || branchName == 'origin/Emna-cherni'
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
        always {
            junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true
            archiveArtifacts artifacts: 'target/site/jacoco/**', allowEmptyArchive: true
        }
    }
}