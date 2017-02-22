package com.github.mamohr.gradle.deploymentstructure.model;

/**
 * Created by cschmidt on 22.02.2017.
 */
class ConfigurablePathSet {

    LinkedHashSet<String> includedPaths = []
    LinkedHashSet<String> excludedPaths = []

    void include(String path) {
        includedPaths.add(path)
    }

    void exclude(String path) {
        excludedPaths.add(path)
    }

    boolean isEmpty() {
        includedPaths.empty && excludedPaths.empty
    }

}
