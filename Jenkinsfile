pipeline {
    agent any

    tools {
        jdk 'JDK21'
        maven 'Maven3'
    }

    environment {
        GIT_REPO_URL = 'https://github.com/HAYET-MEDINI/MPAWI2-G1-event.git'
        GIT_BRANCH   = 'Aloui-Abir'

        // Services accessibles depuis le conteneur Jenkins
        SONAR_URL = 'http://host.docker.internal:9000'
        NEXUS_URL = 'http://host.docker.internal:8081'
    }

    stages {

        stage('1. Git Checkout') {
            steps {
                echo '=== Checkout GitHub ==='

                git branch: "${GIT_BRANCH}",
                    url: "${GIT_REPO_URL}"

                sh '''
                    echo "Branch:"
                    git branch --show-current

                    echo "Commit:"
                    git log -1 --oneline
                '''
            }
        }

        stage('2. Maven Clean & Compile') {
            steps {
                echo '=== Maven Clean & Compile ==='

                sh '''
                    mvn clean compile \
                        -Dmaven.compiler.source=21 \
                        -Dmaven.compiler.target=21
                '''
            }
        }

        stage('3. Unit Tests & JaCoCo') {
            steps {
                echo '=== Unit Tests & JaCoCo ==='

                sh '''
                    mvn test \
                        -Dmaven.compiler.source=21 \
                        -Dmaven.compiler.target=21
                '''
            }

            post {
                always {
                    junit(
                        allowEmptyResults: true,
                        testResults: 'target/surefire-reports/*.xml'
                    )

                    echo 'JaCoCo report generated in target/site/jacoco/'
                }
            }
        }

        stage('4. SonarQube Analysis') {
            steps {
                echo '=== SonarQube Analysis ==='

                withCredentials([
                    string(
                        credentialsId: 'sonarqube-token',
                        variable: 'SONAR_TOKEN'
                    )
                ]) {

                    sh '''
                        mvn org.sonarsource.scanner.maven:sonar-maven-plugin:4.0.0.4121:sonar \
                            -Dsonar.host.url="$SONAR_URL" \
                            -Dsonar.token="$SONAR_TOKEN" \
                            -Dsonar.projectKey=eventsProject \
                            -Dsonar.projectName=eventsProject
                    '''
                }
            }
        }

        stage('5. Package JAR') {
            steps {
                echo '=== Package JAR ==='

                sh '''
                    mvn package \
                        -DskipTests \
                        -Dmaven.compiler.source=21 \
                        -Dmaven.compiler.target=21
                '''
            }

            post {
                success {
                    archiveArtifacts(
                        artifacts: 'target/*.jar',
                        fingerprint: true
                    )
                }
            }
        }

        stage('6. Deploy Snapshot to Nexus') {
            steps {
                echo '=== Deploy Snapshot to Nexus ==='

                withCredentials([
                    file(
                        credentialsId: 'maven-settings',
                        variable: 'MAVEN_SETTINGS'
                    )
                ]) {

                    sh '''
                        mvn versions:set \
                            -DnewVersion=1.0.0-SNAPSHOT \
                            -DgenerateBackupPoms=false

                        mvn deploy \
                            -DskipTests \
                            -s "$MAVEN_SETTINGS" \
                            -DaltDeploymentRepository=deploymentRepo::default::${NEXUS_URL}/repository/maven-snapshots/
                    '''
                }
            }
        }
    
               stage('7. Deploy Release to Nexus') {
            steps {
                echo '=== Deploy RELEASE to Nexus ==='

                withCredentials([
                    file(
                        credentialsId: 'maven-settings',
                        variable: 'MAVEN_SETTINGS'
                    )
                ]) {
                    sh '''
                        # 1. On change la version pour enlever le -SNAPSHOT
                        mvn versions:set \
                            -DnewVersion=1.0.0 \
                            -DgenerateBackupPoms=false

                        # 2. On déploie vers le dépôt RELEASES
                        mvn deploy \
                            -DskipTests \
                            -s "$MAVEN_SETTINGS" \
                            -DaltDeploymentRepository=deploymentRepo::default::${NEXUS_URL}/repository/maven-releases/
                    '''
                }
            }
        }
    }
    post {

        success {
            echo '=========================================='
            echo 'PIPELINE SUCCESSFUL'
            echo '=========================================='
            echo 'Compilation: OK'
            echo 'Tests: OK'
            echo 'JaCoCo: OK'
            echo 'SonarQube: OK'
            echo 'JAR Package: OK'
            echo 'Nexus Snapshot: OK'
        }

        failure {
            echo '=========================================='
            echo 'PIPELINE FAILED'
            echo '=========================================='
            echo 'Check the failed stage and Console Output.'
        }

        always {
            echo 'Pipeline execution finished.'
        }
    }
}
