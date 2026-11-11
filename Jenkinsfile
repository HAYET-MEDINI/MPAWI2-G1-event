
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
            description: 'Nouvelle version unique (format x.y.z)'
        )
    }

    environment {
        GIT_REPO_URL = 'https://github.com/HAYET-MEDINI/MPAWI2-G1-event.git'
        GIT_BRANCH = 'mayssa'

        // Adresse Docker de SonarQube
        SONAR_URL = 'http://sonarqube:9000'
        SONAR_PROJECT_KEY = 'eventsProject'

        // Version SNAPSHOT actuelle du projet
        SNAPSHOT_VERSION = '1.0.0-SNAPSHOT'
    }

    stages {

        stage('1. Git Checkout') {
            steps {
                git branch: "${GIT_BRANCH}",
                    url: "${GIT_REPO_URL}"
            }
        }

        stage('2. Maven Clean & Compile') {
            steps {
                sh 'chmod +x mvnw'
                sh './mvnw clean compile'
            }
        }

        stage('3. Unit Tests & JaCoCo') {
            steps {
                sh './mvnw verify'
            }

            post {
                always {
                    junit(
                        allowEmptyResults: true,
                        testResults: '**/target/surefire-reports/*.xml'
                    )

                    archiveArtifacts(
                        allowEmptyArchive: true,
                        artifacts: '**/target/site/jacoco/**'
                    )
                }
            }
        }

        stage('4. SonarQube Analysis') {
            steps {
                withCredentials([
                    string(
                        credentialsId: 'sonar-token',
                        variable: 'SONAR_TOKEN'
                    )
                ]) {
                    sh '''
                        ./mvnw org.sonarsource.scanner.maven:sonar-maven-plugin:4.0.0.4121:sonar \
                        -Dsonar.projectKey="$SONAR_PROJECT_KEY" \
                        -Dsonar.host.url="$SONAR_URL" \
                        -Dsonar.token="$SONAR_TOKEN"
                    '''
                }
            }
        }

        stage('5. Package JAR') {
            steps {
                sh './mvnw package -DskipTests'
            }

            post {
                success {
                    archiveArtifacts(
                        artifacts: 'target/*.jar',
                        excludes: 'target/*.jar.original',
                        fingerprint: true
                    )
                }
            }
        }

        stage('6. Deploy Snapshot to Nexus') {
            steps {
                withCredentials([
                    file(
                        credentialsId: 'maven-settings',
                        variable: 'MAVEN_SETTINGS'
                    )
                ]) {
                    sh '''
                        ./mvnw deploy \
                        -DskipTests \
                        -s "$MAVEN_SETTINGS"
                    '''
                }
            }
        }

        stage('7. Deploy Release to Nexus') {
            when {
                expression {
                    return params.DEPLOY_RELEASE
                }
            }

            steps {
                script {
                    if (!(params.RELEASE_VERSION ==~ /\\d+\\.\\d+\\.\\d+/)) {
                        error(
                            'RELEASE_VERSION doit être au format x.y.z, par exemple 1.0.1'
                        )
                    }

                    if (params.RELEASE_VERSION == env.SNAPSHOT_VERSION) {
                        error('La version Release doit être différente de la version SNAPSHOT.')
                    }

                    try {
                        sh """
                            ./mvnw versions:set \
                            -DnewVersion=${params.RELEASE_VERSION} \
                            -DgenerateBackupPoms=false
                        """

                        withCredentials([
                            file(
                                credentialsId: 'maven-settings',
                                variable: 'MAVEN_SETTINGS'
                            )
                        ]) {
                            sh '''
                                ./mvnw deploy \
                                -DskipTests \
                                -s "$MAVEN_SETTINGS"
                            '''
                        }

                    } finally {
                        sh """
                            ./mvnw versions:set \
                            -DnewVersion=${SNAPSHOT_VERSION} \
                            -DgenerateBackupPoms=false
                        """
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

        always {
            echo 'Fin de l’exécution du pipeline Jenkins.'
        }
    }
}
