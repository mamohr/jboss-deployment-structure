package com.github.mamohr.gradle.deploymentstructure.model
/**
 * Created by cschmidt on 22.02.2017.
 */
class ConfigurablePathSet implements XmlSerializable {

    final String xmlNodeName

    final LinkedHashSet<String> includedPaths = []
    final LinkedHashSet<String> excludedPaths = []

    ConfigurablePathSet(String nodeName) {
        this.xmlNodeName = nodeName
    }

    void include(String path) {
        includedPaths.add(path)
    }

    void exclude(String path) {
        excludedPaths.add(path)
    }

    boolean isEmpty() {
        includedPaths.empty && excludedPaths.empty
    }

    def saveToXml(Node root) {
        Node node = new Node(root, xmlNodeName)

        includedPaths.each { String path ->
            new Node(node, 'include', [path: path])
        }
        excludedPaths.each { String path ->
            new Node(node, 'exclude', [path: path])
        }
    }
}
