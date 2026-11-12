
        
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

        // Adresse Docker de SonarQube
        SONAR_URL = 'http://sonarqube:9000'
        SONAR_PROJECT_KEY = 'eventsProject'

        // Version SNAPSHOT actuelle du projet
        SNAPSHOT_VERSION = '1.0.0-SNAPSHOT'
DOCKER_IMAGE = 'mayssahamdanii/docker-spring-boot'
DOCKER_CREDENTIALS = 'dockerhub'
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
        // Vérifier que le rapport JaCoCo existe
        sh '''
            echo "Verification du rapport JaCoCo..."
            ls -lh target/site/jacoco/
            test -f target/site/jacoco/jacoco.xml
        '''

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
                -Dsonar.token="$SONAR_TOKEN" \
                -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
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
            def releaseVersion = params.RELEASE_VERSION?.trim()

            // Vérifier le format x.y.z
            if (!(releaseVersion ==~ /^\d+\.\d+\.\d+$/)) {
                error('RELEASE_VERSION doit être au format x.y.z, par exemple 1.0.1')
            }

            // Vérifier que la version Release est différente de SNAPSHOT
            if (releaseVersion == env.SNAPSHOT_VERSION) {
                error('La version Release doit être différente de la version SNAPSHOT.')
            }

            try {
                echo "Déploiement de la version Release : ${releaseVersion}"

                sh """
                    ./mvnw -B -ntp versions:set \
                    -DnewVersion=${releaseVersion} \
                    -DgenerateBackupPoms=false
                """

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
                echo "Restauration de la version SNAPSHOT : ${env.SNAPSHOT_VERSION}"

                sh """
                    ./mvnw -B -ntp versions:set \
                    -DnewVersion=${env.SNAPSHOT_VERSION} \
                    -DgenerateBackupPoms=false
                """
            }
        }
    }
}
stage('8. Build Docker Image') {
    steps {
        sh '''
            echo "Building Docker image..."
            docker build -t ${DOCKER_IMAGE}:${BUILD_NUMBER} .
            docker tag ${DOCKER_IMAGE}:${BUILD_NUMBER} ${DOCKER_IMAGE}:latest
        '''
    }
}

stage('9. Push Docker Image') {
    steps {
        withCredentials([
            string(
                credentialsId: "${DOCKER_CREDENTIALS}",
                variable: 'DOCKER_PASSWORD'
            )
        ]) {
            withEnv(["DOCKER_USERNAME=mayssahamdanii"]) {
                sh '''
                    echo "$DOCKER_PASSWORD" | docker login \
                        -u "$DOCKER_USERNAME" \
                        --password-stdin

                    docker push ${DOCKER_IMAGE}:${BUILD_NUMBER}
                    docker push ${DOCKER_IMAGE}:latest

                    docker logout
                '''
            }
        }
    }
}

  
       } // Fin de stages

} // Fin de pipeline



       
           
            
         
  
    
