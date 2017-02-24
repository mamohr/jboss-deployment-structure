package com.github.mamohr.gradle.deploymentstructure.model

/**
 * Created by cschmidt on 21.02.2017.
 */
class Resource implements XmlSerializable {
    String path
    Boolean physicalSourceCode

    ConfigurablePathSet filter = new ConfigurablePathSet()

    Resource(String path, Boolean physicalSourceCode) {
        this.path = path
        this.physicalSourceCode = physicalSourceCode
    }

    @Override
    def saveToXml(Node root) {
        Map attributes = [path: path]

        if (physicalSourceCode) {
            attributes.'use-physical-code-source' = true
        }

        return new Node(root, 'resource-root', attributes)
    }
}
