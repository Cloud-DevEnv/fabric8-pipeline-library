#!/usr/bin/groovy
def call(Map parameters = [:], body) {
     def defaultLabel = "halyard.${env.JOB_NAME}.${env.BUILD_NUMBER}".replace('-', '_').replace('/', '_')

    //def defaultLabel = buildId('halyard')
    def label = parameters.get('label', defaultLabel)

    halyardTemplate(parameters) {
        node(label) {
            body()
        }
    }
}

