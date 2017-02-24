package com.github.mamohr.gradle.deploymentstructure.model

/**
 * Created by cschmidt on 21.02.2017.
 */
class Resource implements XmlSerializable {
    String path
    Boolean physicalSourceCode

    ConfigurablePathSet pathFilter = new ConfigurablePathSet()

    Resource(String path, Boolean physicalSourceCode) {
        this.path = path
        this.physicalSourceCode = physicalSourceCode
    }

    @Override
    def saveToXml(Node root) {
        Map attributes = [path: path]
        Node node = new Node(root, 'resource-root', attributes)

        if (physicalSourceCode) {
            attributes.'use-physical-code-source' = true
        }

        if(!pathFilter.empty) {
            Node filter = new Node(node, 'filter')
            appendPathSetToNode(filter, pathFilter)
        }

        return node
    }

    /**
     * Appends the elements of the ConfigurablePathSet as &lt;include&gt; or &lt;exclude&gt; tag
     * @param filterNode
     * @param pathSet
     */
    protected void appendPathSetToNode(Node filterNode, ConfigurablePathSet pathSet) {
        pathSet.includedPaths.each { String path ->
            new Node(filterNode, 'include', [path: path])
        }
        pathSet.excludedPaths.each { String path ->
            new Node(filterNode, 'exclude', [path: path])
        }
    }
}
