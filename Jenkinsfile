
pipeline {
    agent any

    parameters {
        booleanParam(
            name: 'DEPLOY_RELEASE',
            defaultValue: false,
            description: 'Déployer une version Release'
        )

        string(
            name: 'RELEASE_VERSION',
            defaultValue: '1.0.1',
            description: 'Version Release au format x.y.z'
        )
    }

    environment {
        GIT_REPO_URL = 'https://github.com/HAYET-MEDINI/MPAWI2-G1-event.git'
        GIT_BRANCH = 'mayssa'

        SONAR_URL = 'http://sonarqube:9000'
        SONAR_PROJECT_KEY = 'eventsProject'

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
                sh './mvnw -B -ntp clean compile'
            }
        }

        stage('3. Unit Tests & JaCoCo') {
            steps {
                sh './mvnw -B -ntp verify'
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
                        ./mvnw -B -ntp org.sonarsource.scanner.maven:sonar-maven-plugin:4.0.0.4121:sonar \
                        -Dsonar.projectKey="$SONAR_PROJECT_KEY" \
                        -Dsonar.host.url="$SONAR_URL" \
                        -Dsonar.token="$SONAR_TOKEN"
                    '''
                }
            }
        }

        stage('5. Package JAR') {
            steps {
                sh './mvnw -B -ntp package -DskipTests'
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
                        ./mvnw -B -ntp deploy \
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
                    def releaseVersion = params.RELEASE_VERSION?.trim()

                    // Vérifier que la version respecte le format x.y.z
                    if (!(releaseVersion ==~ /^\d+\.\d+\.\d+$/)) {
                        error('RELEASE_VERSION doit être au format x.y.z, exemple : 1.0.1')
                    }

                    // Vérifier que la version Release est différente de SNAPSHOT
                    if (releaseVersion == env.SNAPSHOT_VERSION) {
                        error('La version Release doit être différente de SNAPSHOT.')
                    }

                    try {
                        echo "Déploiement de la version Release : ${releaseVersion}"

                        // Modifier temporairement la version du projet
                        sh """
                            ./mvnw -B -ntp versions:set -DnewVersion=${releaseVersion} -DgenerateBackupPoms=false
                        """

                        // Déployer la version Release dans Nexus
                        withCredentials([
                            file(
                                credentialsId: 'maven-settings',
                                variable: 'MAVEN_SETTINGS'
                            )
                        ]) {
                            sh '''
                                ./mvnw -B -ntp deploy \
                                -DskipTests \
                                -s "$MAVEN_SETTINGS"
                            '''
                        }

                        echo 'Déploiement Release terminé avec succès.'

                    } finally {
                        // Restaurer la version SNAPSHOT même en cas d'échec
                        echo "Restauration de la version : ${env.SNAPSHOT_VERSION}"

                        sh """
                            ./mvnw -B -ntp versions:set -DnewVersion=${env.SNAPSHOT_VERSION} -DgenerateBackupPoms=false
                        """
                    }
                }
            }
        }
    }

    post {
        success {
            echo 'Pipeline Jenkins terminé avec succès.'
        }

        failure {
            echo 'Le pipeline Jenkins a échoué. Consultez la Console Output.'
        }

        always {
            echo 'Fin de l execution du pipeline.'
        }
    }
}
