#!/usr/bin/groovy


def call(Map parameters = [:], body) {

    def defaultLabel = "halyard.${env.JOB_NAME}.${env.BUILD_NUMBER}".replace('-', '_').replace('/', '_')
    def label = parameters.get('label', defaultLabel)

    def halyardImage = parameters.get('halyardImage', 'gcr.io/spinnaker-marketplace/halyard:stable')
    def clientsImage = parameters.get('clientsImage', 'armdocker.rnd.ericsson.se/proj_adp/fabric8/builder-clients:0.14-1')
    def inheritFrom = parameters.get('inheritFrom', 'base')
   

        podTemplate(label: label, serviceAccount: 'jenkins', inheritFrom: "${inheritFrom}",containers: [
            containerTemplate(name: 'clients', image: "${clientsImage}", ttyEnabled: true, command: 'cat', privileged: true, envVars: [[key: 'DOCKER_CONFIG', value: '/home/jenkins/.docker/']]),
                            
            containerTemplate(name: 'halyard', image: "${halyardImage}", ttyEnabled: true, command: '/opt/halyard/bin/halyard', privileged: true, envVars: [[key: 'DOCKER_CONFIG', value: '/home/jenkins/.docker/']])],
   
                volumes: [
                        secretVolume(secretName: 'jenkins-docker-cfg', mountPath: '/home/jenkins/.docker'),
                        secretVolume(secretName: 'jenkins-hub-api-token', mountPath: '/home/jenkins/.apitoken'),
                        hostPathVolume(hostPath: '/var/run/docker.sock', mountPath: '/var/run/docker.sock')],
                envVars: [[key: 'DOCKER_HOST', value: 'unix:/var/run/docker.sock'], [key: 'DOCKER_CONFIG', value: '/home/jenkins/.docker/']]) {
            body()
        }


    
}

