node {
    /* Requires the Docker Pipeline plugin to be installed */
    checkout scm
    try {
        stage('build') {
            docker.image('hseeberger/scala-sbt:8u212_1.2.8_2.12.9').inside('-v /usr/share/sbt/conf:/usr/share/sbt/conf') {
                sh 'sbt compile'
            }
        }
        if (env.BRANCH_NAME == 'master' && (currentBuild.result == null || currentBuild.result == 'SUCCESS')) {
            stage('deploy_artifactory') {
                docker.image('hseeberger/scala-sbt:8u212_1.2.8_2.12.9').inside('-v /usr/share/sbt/conf:/usr/share/sbt/conf') {
                    sh 'sbt ";publish; dist"'
                }
            }
            stage('docker') {
                docker.withRegistry('http://95.169.186.20:8082/repository/compmanager-registry/', 'Nexus') {
                    env.IMAGE_NAME = "bjj-bot"
                    def customImage = null
                    stage("build_docker") {
                        try {
                            customImage = docker.build("${env.IMAGE_NAME}/${env.BRANCH_NAME}")
                        } catch (err) {
                            currentBuild.result = 'FAILURE'
                            print "Failed: ${err}"
                            throw err
                        }
                    }
                    if (customImage != null && currentBuild.result != 'FAILURE') {
                        stage("push_image") {
                            customImage.push("${env.BUILD_NUMBER}")
                            customImage.push("latest")
                        }
                    }
                }
            }

        }
    } finally {
        //unit tests results
    }
}