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

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.internal.AbstractNamedDomainObjectContainer
import org.gradle.internal.reflect.Instantiator

class JBossDeploymentStructure implements XmlSerializable {

    public static final String EXTENSION_NAME = "jbossDeploymentStructure"

    private globalExcludes = [] as Set
    private Deployment deployment = new Deployment()
    private NamedDomainObjectContainer<Subdeployment> subdeployments
    private Instantiator instantiator

    String structureVersion = '1.2'

    private List<Action<? super Node>> xmlActions = []

    JBossDeploymentStructure(Instantiator instantiator) {
        this.instantiator = instantiator
        subdeployments = new AbstractNamedDomainObjectContainer<Subdeployment>(Subdeployment,instantiator) {
            @Override
            protected Subdeployment doCreate(String name) {
                def subdeployment = new Subdeployment()
                subdeployment.setName(name)
                return subdeployment
            }
        }
    }

    void globalExclude(String moduleIdentifier) {
        Module excludedModule = new Module(moduleIdentifier)
        globalExcludes.add(excludedModule)
    }

    void dependency(String moduleIdentifier) {
       deployment.dependency(moduleIdentifier)
    }

    void dependency(String moduleIdentifier, Closure closure) {
       deployment.dependency(moduleIdentifier,closure)
    }

    void exclude(String moduleIdentifier) {
        deployment.exclude(moduleIdentifier)
    }

    NamedDomainObjectContainer<Subdeployment> getSubdeployments() {
        return subdeployments
    }

    void subdeployments(Action<? super NamedDomainObjectContainer<Subdeployment>> configure) {
        configure.execute(subdeployments)
    }

    protected Deployment getDeployment() {
        return deployment;
    }

    protected Set<Module> getGlobalExcludes() {
        return globalExcludes
    }

    void withXml(Action<? super Node> rootNodeAction) {
        xmlActions.add(rootNodeAction);
    }

    @Override
    Node saveToXml(Node root) {
        if(!root) {
            root = new Node(null, "jboss-deployment-structure", [xmlns: "urn:jboss:deployment-structure:$structureVersion"])
        }

        deployment.saveToXml(root);
        getSubdeployments().each { subdeployment ->
            subdeployment.saveToXml(root)
        }
        xmlActions.each { xmlAction ->
            xmlAction.execute(root)
        }
        root;
    }
}
