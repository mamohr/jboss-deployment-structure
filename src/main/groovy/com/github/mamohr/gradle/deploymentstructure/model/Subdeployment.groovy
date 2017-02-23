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

import org.gradle.api.InvalidUserDataException
import org.gradle.api.Named

/**
 * Created by mario on 10.01.2015.
 */
class Subdeployment extends Deployment implements Named, XmlSerializable {
    static String EXTENSION_NAME = 'jbossSubdeployment'

    String name
    private final ConfigurablePathSet resourceFilter = new ConfigurablePathSet()

    public Subdeployment(String name) {
        this.name = name
    }

    public Subdeployment() {

    }


    /**
     * Filters resource with include and exclude directives
     * @param cl
     */
    void resourceFilter(@DelegatesTo(ConfigurablePathSet) Closure cl) {
        cl.delegate = resourceFilter
        cl.resolveStrategy = Closure.DELEGATE_FIRST
        cl()
    }

    @Override
    def saveToXml(Node node) {
        Node subdeployment = super.saveToXml(node)
        subdeployment.attributes().name = name

        if(!resourceFilter.empty) {
            Node resources = (Node)subdeployment.find{ it.name() == 'resources' }
            Node filter = new Node(resources, 'filter')

            // Resource tag not found, thus no resources are declared and a filter is useless
            if(!resources) {
                throw new InvalidUserDataException('Cannot apply resource filter on empty resource set')
            }

            appendResourceFilterToNode(filter, resourceFilter)
        }

        return subdeployment
    }

    /**
     * Appends the elements of the ConfigurablePathSet as &lt;include&gt; or &lt;exclude&gt; tag
     * @param filterNode
     * @param pathSet
     */
    protected void appendResourceFilterToNode(Node filterNode, ConfigurablePathSet pathSet) {
        pathSet.includedPaths.each { String path ->
            new Node(filterNode, 'include', [path: path])
        }
        pathSet.excludedPaths.each { String path ->
            new Node(filterNode, 'exclude', [path: path])
        }
    }

    @Override
    protected String getXmlNodeName() {
        return 'sub-deployment'
    }
}
