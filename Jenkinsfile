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
        GIT_REPO_URL = 'https://github.com/HAYET-MEDINI/MPAWI2-G1-event.git'
        GIT_BRANCH = 'mayssa'

        SONAR_URL = 'http://localhost:9000'
        SONAR_PROJECT_KEY = 'eventsProject'
    }

    stages {
        stage('1. Git Checkout') {
            steps {
                git branch: "${GIT_BRANCH}", url: "${GIT_REPO_URL}"
            }
        }

        stage('2. Maven Clean & Compile') {
            steps {
                sh 'sh ./mvnw clean compile'
            }
        }

        stage('3. Unit Tests & JaCoCo') {
            steps {
                sh 'sh ./mvnw verify'
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
                    sh 'sh ./mvnw org.sonarsource.scanner.maven:sonar-maven-plugin:4.0.0.4121:sonar -Dsonar.projectKey="$SONAR_PROJECT_KEY" -Dsonar.host.url="$SONAR_URL" -Dsonar.token="$SONAR_TOKEN"'
                }
            }
        }

        stage('5. Package JAR') {
            steps {
                sh 'sh ./mvnw package -DskipTests'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/eventsProject-*.jar', excludes: 'target/*.jar.original', fingerprint: true
                }
            }
        }

        stage('6. Deploy Snapshot to Nexus') {
            steps {
                withCredentials([file(credentialsId: 'maven-settings', variable: 'MAVEN_SETTINGS')]) {
                    sh 'sh ./mvnw deploy -DskipTests -s "$MAVEN_SETTINGS"'
                }
            }
        }

        stage('7. Deploy Release to Nexus') {
            when {
                expression { params.DEPLOY_RELEASE }
            }
            steps {
                script {
                    if (!(params.RELEASE_VERSION ==~ /\d+\.\d+\.\d+/)) {
                        error('RELEASE_VERSION doit être au format x.y.z, par exemple 1.0.1')
                    }
                    try {
                        sh "sh ./mvnw versions:set -DnewVersion=${params.RELEASE_VERSION} -DgenerateBackupPoms=false"
                        withCredentials([file(credentialsId: 'maven-settings', variable: 'MAVEN_SETTINGS')]) {
                            sh 'sh ./mvnw deploy -DskipTests -s "$MAVEN_SETTINGS"'
                        }
                    } finally {
                        sh 'sh ./mvnw versions:set -DnewVersion=1.0.0-SNAPSHOT -DgenerateBackupPoms=false'
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