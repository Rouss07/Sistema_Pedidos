pipeline {
    agent any

    tools {
        maven 'Maven3'
        jdk 'JDK17'
    }

    environment {
        SONAR_PROJECT_KEY = 'com.espigapedidos:espigapedidos'
        DOCKER_IMAGE = 'espigapedidos'
        ADMIN_PASSWORD = credentials('admin-password')
        TIENDA_PASSWORD = credentials('tienda-password')
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                bat 'mvn clean package -DskipTests'
            }
        }

        stage('Tests') {
            steps {
                bat 'mvn test'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    bat 'mvn sonar:sonar'
                }
            }
        }

        stage('Docker Build') {
            steps {
                bat "docker build -t %DOCKER_IMAGE% ."
            }
        }

        stage('Docker Run') {
            steps {
                bat "docker stop %DOCKER_IMAGE% || true"
                bat "docker rm %DOCKER_IMAGE% || true"
                bat "docker run -d --name %DOCKER_IMAGE% -p 8080:8080 -e ADMIN_PASSWORD=%ADMIN_PASSWORD% -e TIENDA_PASSWORD=%TIENDA_PASSWORD% %DOCKER_IMAGE%"
            }
        }
    }

    post {
        success {
            echo 'Pipeline ejecutado exitosamente'
        }
        failure {
            echo 'Pipeline falló'
        }
    }
}