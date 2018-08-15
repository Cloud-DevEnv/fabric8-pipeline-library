#!/usr/bin/groovy
def call(Map parameters = [:], body) {

    def defaultLabel = buildId('halyard')
    def label = parameters.get('label', defaultLabel)

    halyardTemplate(parameters) {
        node(label) {
            body()
        }
    }
}
