/*
 * Jenkins Declarative Pipeline for OrangeHRM Selenium Framework
 *
 * Prerequisites in Jenkins (configure once under Manage Jenkins -> Tools):
 *   - JDK installation named "JDK17"
 *   - Maven installation named "Maven3"
 *   - Allure Commandline installation named "Allure"
 *
 * Required Jenkins plugins:
 *   - Pipeline, Git, Maven Integration, JDK Tool
 *   - Allure Jenkins Plugin (for Allure report rendering)
 *
 * On Linux Jenkins agents, Chrome must be installed:
 *   sudo apt install google-chrome-stable
 */
pipeline {
    agent any

    tools {
        jdk 'JDK17'
        maven 'Maven3'
    }

    // Parameters allow ad-hoc runs from the "Build with Parameters" button
    parameters {
        choice(
            name: 'BROWSER',
            choices: ['chrome', 'firefox', 'edge'],
            description: 'Browser to run tests against'
        )
        choice(
            name: 'HEADLESS',
            choices: ['true', 'false'],
            description: 'Run browsers in headless mode (must be true on CI agents without a display)'
        )
        string(
            name: 'SUITE_FILE',
            defaultValue: 'testng.xml',
            description: 'TestNG suite XML to execute'
        )
    }

    options {
        timeout(time: 45, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '15'))
        timestamps()
        ansiColor('xterm')
    }

    environment {
        // Surface params as plain env vars for shell readability
        BROWSER  = "${params.BROWSER}"
        HEADLESS = "${params.HEADLESS}"
        SUITE    = "${params.SUITE_FILE}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Tool Versions') {
            // Sanity check - confirms Jenkins picked up the right JDK/Maven
            steps {
                sh '''
                    echo "=== Java ===" && java -version
                    echo "=== Maven ===" && mvn -v
                '''
            }
        }

        stage('Compile') {
            steps {
                sh 'mvn clean compile -DskipTests --batch-mode'
            }
        }

        stage('Run Tests') {
            steps {
                // System properties flow through to ConfigReader and DriverFactory
                sh """
                    mvn test \
                        -DsuiteXmlFile=${SUITE} \
                        -Dbrowser=${BROWSER} \
                        -Dheadless=${HEADLESS} \
                        --batch-mode
                """
            }
        }
    }

    post {
        always {
            // Publish TestNG -> JUnit XML results (built-in surefire output)
            junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'

            // Publish Allure report - requires Allure Jenkins plugin
            allure([
                includeProperties: false,
                jdk: '',
                reportBuildPolicy: 'ALWAYS',
                results: [[path: 'target/allure-results']]
            ])

            // Archive screenshots, logs, and the raw surefire reports for download
            archiveArtifacts artifacts: 'target/screenshots/**/*.png',     allowEmptyArchive: true
            archiveArtifacts artifacts: 'logs/**/*.log',                   allowEmptyArchive: true
            archiveArtifacts artifacts: 'target/surefire-reports/**',      allowEmptyArchive: true
            archiveArtifacts artifacts: 'test-output/**',                  allowEmptyArchive: true
        }
        success {
            echo "Build #${BUILD_NUMBER} passed - all tests green."
        }
        unstable {
            echo "Build #${BUILD_NUMBER} unstable - some tests failed. Check Allure report."
        }
        failure {
            echo "Build #${BUILD_NUMBER} failed - check console output for compile/setup errors."
        }
    }
}
