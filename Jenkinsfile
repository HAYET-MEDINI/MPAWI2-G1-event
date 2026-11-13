
pipeline {
    agent any

    options {
        timestamps()
        skipDefaultCheckout(true)
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
            description: 'Version Release au format x.y.z'
        )
    }

    environment {
        JAVA_HOME = 'C:/jdk-21.0.12_windows-x64_bin/jdk-21.0.12'

        MAVEN_HOME = 'C:/Users/Omega Informatique/Documents/apache-maven-3.9.16-bin/apache-maven-3.9.16'
        DOCKER_HOME = 'C:/Users/Omega Informatique/AppData/Local/Programs/DockerDesktop/resources/bin'

        GIT_REPO_URL = 'https://github.com/HAYET-MEDINI/MPAWI2-G1-event.git'
        APP_GIT_BRANCH = 'Emna-cherni'

        SONAR_URL = 'http://localhost:9000'
        SONAR_PROJECT_KEY = 'eventsProject'

        NEXT_SNAPSHOT_VERSION = '1.0.1-SNAPSHOT'

        DOCKER_IMAGE_NAME = 'eventsproject'
    }

    stages {

        // ==========================================
        // 1. GIT CHECKOUT
        // ==========================================

        stage('1. Git checkout') {
            steps {
                git branch: "${APP_GIT_BRANCH}",
                    url: "${GIT_REPO_URL}"
            }
        }

        // ==========================================
        // 2. MAVEN CLEAN AND COMPILE
        // ==========================================

        stage('2. Maven clean and compile') {
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

        // ==========================================
        // 3. UNIT TESTS AND JACOCO
        // ==========================================

        stage('3. Unit tests and JaCoCo') {
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
                    junit(
                        testResults: '**/target/surefire-reports/*.xml',
                        allowEmptyResults: true
                    )

                    archiveArtifacts(
                        artifacts: 'target/site/jacoco/**',
                        allowEmptyArchive: true
                    )
                }
            }
        }

        // ==========================================
        // 4. SONARQUBE ANALYSIS
        // ==========================================

        stage('4. SonarQube analysis') {
            steps {
                withCredentials([
                    string(
                        credentialsId: 'sonar-token',
                        variable: 'SONAR_TOKEN'
                    )
                ]) {
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

        // ==========================================
        // 5. PACKAGE JAR
        // ==========================================

        stage('5. Package JAR') {
            steps {
                bat '''
                    @echo off
                    set "PATH=%JAVA_HOME%\\bin;%MAVEN_HOME%\\bin;%PATH%"

                    call mvn -B package -DskipTests

                    if errorlevel 1 exit /b 1
                '''
            }

            post {
                success {
                    archiveArtifacts(
                        artifacts: 'target/eventsProject-*.jar',
                        excludes: 'target/*.jar.original',
                        fingerprint: true
                    )
                }
            }
        }

        // ==========================================
        // 6. BUILD DOCKER IMAGE
        // ==========================================

        stage('6. Build Docker image') {
            when {
                expression {
                    fileExists('Dockerfile')
                }
            }

            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'dockerhub-credentials',
                        usernameVariable: 'DOCKERHUB_USER',
                        passwordVariable: 'DOCKERHUB_TOKEN'
                    )
                ]) {
                    bat '''
                        @echo off
                        set "PATH=%DOCKER_HOME%;%PATH%"

                        docker --version
                        if errorlevel 1 exit /b 1

                        docker info >nul 2>&1
                        if errorlevel 1 (
                            echo Docker daemon is unavailable to the Jenkins agent.
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

        // ==========================================
        // 7. PUSH DOCKER IMAGE
        // ==========================================

        stage('7. Push Docker image') {
            when {
                expression {
                    fileExists('Dockerfile')
                }
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
                                docker logout || exit /b 0
                            '''
                        }
                    }
                }
            }
        }

        // ==========================================
        // 8. DEPLOY SNAPSHOT TO NEXUS
        // ==========================================

        stage('8. Deploy snapshot to Nexus') {
            when {
                expression {
                    env.APP_GIT_BRANCH == 'Emna-cherni'
                }
            }

            steps {
                withCredentials([
                    file(
                        credentialsId: 'nexus-maven-settings',
                        variable: 'MAVEN_SETTINGS'
                    )
                ]) {
                    bat '''
                        @echo off
                        set "PATH=%JAVA_HOME%\\bin;%MAVEN_HOME%\\bin;%PATH%"

                        call mvn -B -s "%MAVEN_SETTINGS%" deploy -DskipTests

                        if errorlevel 1 exit /b 1
                    '''
                }
            }
        }

        // ==========================================
        // 9. DEPLOY RELEASE TO NEXUS
        // ==========================================

        stage('9. Deploy release to Nexus') {
            when {
                allOf {
                    expression {
                        env.APP_GIT_BRANCH == 'Emna-cherni'
                    }

                    expression {
                        params.DEPLOY_RELEASE
                    }
                }
            }

            steps {
                script {
                    if (!(params.RELEASE_VERSION ==~ /^\d+\.\d+\.\d+$/)) {
                        error(
                            'RELEASE_VERSION doit être au format x.y.z, exemple 1.0.1'
                        )
                    }
                }

                withCredentials([
                    file(
                        credentialsId: 'nexus-maven-settings',
                        variable: 'MAVEN_SETTINGS'
                    )
                ]) {
                    withEnv([
                        "RELEASE_VERSION=${params.RELEASE_VERSION}"
                    ]) {
                        script {
                            try {
                                bat '''
                                    @echo off
                                    set "PATH=%JAVA_HOME%\\bin;%MAVEN_HOME%\\bin;%PATH%"

                                    echo Setting Release version...

                                    call mvn -B versions:set ^
                                        -DnewVersion=%RELEASE_VERSION% ^
                                        -DgenerateBackupPoms=false

                                    if errorlevel 1 exit /b 1

                                    echo Deploying Release to Nexus...

                                    call mvn -B -s "%MAVEN_SETTINGS%" deploy -DskipTests

                                    if errorlevel 1 exit /b 1
                                '''
                            } finally {
                                bat '''
                                    @echo off
                                    set "PATH=%JAVA_HOME%\\bin;%MAVEN_HOME%\\bin;%PATH%"

                                    echo Restoring Snapshot version...

                                    call mvn -B versions:set ^
                                        -DnewVersion=%NEXT_SNAPSHOT_VERSION% ^
                                        -DgenerateBackupPoms=false

                                    if errorlevel 1 exit /b 1
                                '''
                            }
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // POST ACTIONS
    // ==========================================

    post {
        success {
            echo 'Pipeline termine avec succes.'
        }

        failure {
            echo 'Pipeline en echec. Consultez la sortie de la console.'
        }

        always {
            echo 'Fin du pipeline.'
        }
    }
}
