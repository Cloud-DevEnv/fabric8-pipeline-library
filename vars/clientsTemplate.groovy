#!/usr/bin/groovy
def call(Map parameters = [:], body) {

    def defaultLabel = "clients.${env.JOB_NAME}.${env.BUILD_NUMBER}".replace('-', '_').replace('/', '_')
    def label = parameters.get('label', defaultLabel)

    def clientsImage = parameters.get('clientsImage', 'armdocker.rnd.ericsson.se/proj_adp/fabric8/builder-clients:0.14-1')
    def inheritFrom = parameters.get('inheritFrom', 'base')

    def flow = new io.fabric8.Fabric8Commands()

    if (flow.isOpenShift()) {
        echo 'Runnning on openshift so using S2I binary source and Docker strategy'
        podTemplate(label: label, serviceAccount: 'jenkins', inheritFrom: "${inheritFrom}",
                containers: [[name: 'clients', image: "${clientsImage}", command: 'cat', ttyEnabled: true, envVars: [[key: 'DOCKER_CONFIG', value: '/home/jenkins/.docker/']]]],
                volumes: [
                        secretVolume(secretName: 'jenkins-docker-cfg', mountPath: '/home/jenkins/.docker'),
                        secretVolume(secretName: 'jenkins-hub-api-token', mountPath: '/home/jenkins/.apitoken')]) {
            body()
        }
    } else {
        echo 'Mounting docker socket to build docker images'
        podTemplate(label: label, serviceAccount: 'jenkins', inheritFrom: "${inheritFrom}",containers: [
            containerTemplate(name: 'clients', image: "${clientsImage}", ttyEnabled: true, command: 'cat', privileged: true, envVars: [[key: 'DOCKER_CONFIG', value: '/home/jenkins/.docker/']]),
            containerTemplate(name: 'python', image: 'python:2.7-wheezy', ttyEnabled: true, command: 'cat', privileged: true, envVars: [[key: 'DOCKER_CONFIG', value: '/home/jenkins/.docker/']]),
            containerTemplate(name: 'golang', image: 'golang:1.8.0', ttyEnabled: true, command: 'cat',  privileged: true, envVars: [[key: 'DOCKER_CONFIG', value: '/home/jenkins/.docker/']]),                
            containerTemplate(name: 'postgres', image: 'postgres:9.1', ttyEnabled: true, command: 'cat',  privileged: true, envVars: [[key: 'DOCKER_CONFIG', value: '/home/jenkins/.docker/']]),                
            containerTemplate(name: 'jmeter', image: 'floodio/jmeter:latest', ttyEnabled: true, command: 'cat', privileged: true, envVars: [[key: 'DOCKER_CONFIG', value: '/home/jenkins/.docker/']])],
   
                volumes: [
                        secretVolume(secretName: 'arts01-secret', mountPath: '/home/jenkins/.arts01'),
                        secretVolume(secretName: 'jenkins-docker-cfg', mountPath: '/home/jenkins/.docker'),
                        secretVolume(secretName: 'jenkins-hub-api-token', mountPath: '/home/jenkins/.apitoken'),
                        secretVolume(secretName: 'jenkins-ssh-config', mountPath: '/root/.ssh',
                        items: 
                             [[key: 'id_rsa', value: 'id_rsa', mode: 256], [key: 'id_rsa.pub', value: 'id_rsa.pub', mode: 256]],[key: 'id_rsa_adpauto', value: 'id_rsa_adpauto', mode: 256]]),
                        hostPathVolume(hostPath: '/var/run/docker.sock', mountPath: '/var/run/docker.sock')],
                envVars: [[key: 'DOCKER_HOST', value: 'unix:/var/run/docker.sock'], [key: 'DOCKER_CONFIG', value: '/home/jenkins/.docker/']]) {
            body()
        }
    }

}
