package com.github.mamohr.gradle.deploymentstructure.model

/**
 * Created by cschmidt on 21.02.2017.
 */
class Resource implements XmlSerializable {
    String path
    Boolean physicalSourceCode

    ConfigurablePathSet pathFilter = new ConfigurablePathSet('filter')

    Resource(String path, Boolean physicalSourceCode) {
        this.path = path
        this.physicalSourceCode = physicalSourceCode
    }

    @Override
    def saveToXml(Node root) {
        Node node = new Node(root, 'resource-root', [path: path])

        if (physicalSourceCode) {
            node.attributes().'use-physical-code-source' = true
        }

        if(!pathFilter.empty) {
            pathFilter.saveToXml(node)
        }

        return node
    }

}
