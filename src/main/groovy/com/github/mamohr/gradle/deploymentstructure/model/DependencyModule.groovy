/*
 * Copyright [2015] Mario Mohr <mario_mohr@web.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.mamohr.gradle.deploymentstructure.model

/**
 * Created by mario on 09.01.2015.
 */
class DependencyModule extends Module {

    public static enum DispositionType {
        NONE,
        IMPORT,
        EXPORT
    }

    boolean export
    boolean optional
    boolean annotations

    ConfigurablePathSet imports
    ConfigurablePathSet exports

    DispositionType services
    DispositionType metaInf

    DependencyModule(String name) {
        super(name)
        imports = new ConfigurablePathSet()
        exports = new ConfigurablePathSet()
    }

    void imports(@DelegatesTo(ConfigurablePathSet) Closure cl) {
        cl.delegate = imports
        cl.resolveStrategy = Closure.DELEGATE_FIRST
        cl()
    }

    void exports(@DelegatesTo(ConfigurablePathSet) Closure cl) {
        cl.delegate = exports
        cl.resolveStrategy = Closure.DELEGATE_FIRST
        cl()
    }

    @Override
    Node saveToXml(Node node) {
        Node module = super.saveToXml(node)
        if (export) {
            module.attributes().export = export
        }
        if (optional) {
            module.attributes().optional = optional
        }
        if (annotations) {
            module.attributes().annotations = annotations
        }
        if (services) {
            module.attributes().services = services.name().toLowerCase()
        }
        if (metaInf) {
            module.attributes().'meta-inf' = metaInf.name().toLowerCase()
        }
        if(!imports.empty) {
            Node importsTag = new Node(module, 'imports')
            appendPathSetToNode(importsTag, imports)
        }
        if(!exports.empty) {
            Node exportsTag = new Node(module, 'exports')
            appendPathSetToNode(exportsTag, exports)
        }
        return module
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
