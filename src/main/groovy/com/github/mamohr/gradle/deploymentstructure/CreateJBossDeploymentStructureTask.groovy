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

package com.github.mamohr.gradle.deploymentstructure

import com.github.mamohr.gradle.deploymentstructure.model.JBossDeploymentStructure
import com.github.mamohr.gradle.deploymentstructure.model.Subdeployment
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.plugins.ear.Ear
import org.gradle.plugins.ear.EarPlugin

import javax.inject.Inject

/**
 * Created by mario on 10.01.2015.
 */
class CreateJBossDeploymentStructureTask extends DefaultTask {

    public static final String TASK_NAME = "createJBossDeploymentStructure"

    private File deploymentStructureFile

    Ear earTask

    @InputFiles
    def getDeployConfiguration() {
        project.configurations.getByName(EarPlugin.DEPLOY_CONFIGURATION_NAME).files
    }

    @TaskAction
    void writeFile() {
        def deployConfiguration = project.configurations.getByName(EarPlugin.DEPLOY_CONFIGURATION_NAME)
        def deploymentDependencies = deployConfiguration.getDependencies()
        JBossDeploymentStructure jbossDeploymentStructure = project.getExtensions().getByType(JBossDeploymentStructure)
        def subdeployments = deploymentDependencies.withType(ProjectDependency).findAll { p -> p.dependencyProject.extensions.findByType(Subdeployment) != null }.collect { p -> p.dependencyProject.extensions.findByType(Subdeployment) }
        subdeployments.each{subdeployment->jbossDeploymentStructure.getSubdeployments().add(subdeployment)}

        deployConfiguration.getResolvedConfiguration().resolvedArtifacts.each {artifact ->
            def subdeployment = new Subdeployment(artifact.file.getName())
            jbossDeploymentStructure.getSubdeployments().add(subdeployment)
        }

        jbossDeploymentStructure.applySubdeploymentConfiguration();

        jbossDeploymentStructure.getGlobalExcludes().each { exclude ->
            jbossDeploymentStructure.deployment.getExcludeModules().add(exclude)
            jbossDeploymentStructure.getSubdeployments().each {subdeployment -> subdeployment.getExcludeModules().add(exclude)}
        }

        Node root = jbossDeploymentStructure.saveToXml(null)
        getDeploymentStructureFile().withPrintWriter {writer -> new XmlNodePrinter(writer).print(root)}
    }

    @OutputFile
    File getDeploymentStructureFile() {
        if(deploymentStructureFile == null) {
            deploymentStructureFile = new File(project.getBuildDir(),"/$name/jboss-deployment-structure.xml")
        }
        return deploymentStructureFile
    }
}
