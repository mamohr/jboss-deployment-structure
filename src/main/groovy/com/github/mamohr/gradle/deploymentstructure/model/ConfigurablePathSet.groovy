package com.github.mamohr.gradle.deploymentstructure.model;

/**
 * Created by cschmidt on 22.02.2017.
 */
class ConfigurablePathSet {

    Set<String> includedPaths = []

    void include(String path) {
        includedPaths.add(path)
    }

    boolean isEmpty() {
        includedPaths.empty
    }

}
