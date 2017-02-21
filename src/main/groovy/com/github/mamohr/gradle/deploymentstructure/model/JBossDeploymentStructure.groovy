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

class JBossDeploymentStructure implements XmlSerializable {

    public static final String EXTENSION_NAME = "jbossDeploymentStructure"

    private Set<Module> globalExcludes = []
    private Deployment deployment = new Deployment()
    private final NamedDomainObjectContainer<Subdeployment> subdeployments
    private List<Action<NamedDomainObjectContainer<Subdeployment>>> subdeploymentActions = new ArrayList<>();
    private List<Action<? super Node>> xmlActions = []

    Boolean earSubdeploymentsIsolated
    String structureVersion = '1.2'


    JBossDeploymentStructure(NamedDomainObjectContainer subdeployments) {
        this.subdeployments = subdeployments;
    }

    void globalExclude(String moduleIdentifier) {
        Module excludedModule = new Module(moduleIdentifier)
        globalExcludes.add(excludedModule)
    }

    void dependency(String moduleIdentifier) {
        deployment.dependency(moduleIdentifier)
    }

    void dependency(String moduleIdentifier, Closure closure) {
        deployment.dependency(moduleIdentifier, closure)
    }

    void exclude(String moduleIdentifier) {
        deployment.exclude(moduleIdentifier)
    }

    void excludeSubSystem(String subSIdentifier) {
        deployment.excludeSubSystem(subSIdentifier)
    }

    void resource(String path, Boolean physicalCodeSource = false) {
        deployment.resource(path, physicalCodeSource)
    }

    void resource(Map args) {
        resource((String)args.get('path'), (Boolean)args.get('physicalCodeSource'))
    }

    void addSubdeployments(Collection<? extends Subdeployment> subdeployments) {
        this.subdeployments.addAll(subdeployments)
    }

    void subdeployments(Action<NamedDomainObjectContainer<Subdeployment>> configure) {
        subdeploymentActions.add(configure);
    }

    void withXml(Action<? super Node> rootNodeAction) {
        xmlActions.add(rootNodeAction);
    }

    @Override
    Node saveToXml(Node root) {
        if (!root) {
            root = new Node(null, "jboss-deployment-structure", [xmlns: "urn:jboss:deployment-structure:$structureVersion"])
        }
        applyGlobalExcludes();
        applySubdeploymentConfiguration()
        if (earSubdeploymentsIsolated != null) {
            root.appendNode("ear-subdeployments-isolated", earSubdeploymentsIsolated)
        }
        deployment.saveToXml(root);
        subdeployments.each { subdeployment ->
            subdeployment.saveToXml(root)
        }
        xmlActions.each { xmlAction ->
            xmlAction.execute(root)
        }
        root;
    }

    NamedDomainObjectContainer<Subdeployment> getSubdeployments() {
        return subdeployments
    }

    Deployment getDeployment() {
        return deployment;
    }

    private Set<Module> getGlobalExcludes() {
        return globalExcludes
    }

    private void applySubdeploymentConfiguration() {
        subdeploymentActions.each { action -> action.execute(getSubdeployments()) }
    }

    private void applyGlobalExcludes() {
        this.getGlobalExcludes().each { module ->
            getDeployment().exclude(module)
            getSubdeployments().each { subdeployment -> subdeployment.exclude(module) }
        }

    }
}
