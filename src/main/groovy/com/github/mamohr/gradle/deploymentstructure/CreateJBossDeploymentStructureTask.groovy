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
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.plugins.ear.Ear
import org.gradle.plugins.ear.EarPlugin

/**
 * Created by mario on 10.01.2015.
 */
class CreateJBossDeploymentStructureTask extends DefaultTask {

    public static final String TASK_NAME = "createJBossDeploymentStructure"
    public static final String OUTPUT_FILENAME = "jboss-deployment-structure.xml"

    private File outputFile;

    public void wireTo(Ear earTask) {
        earTask.getMetaInf().from(this.outputs)
    }

    CreateJBossDeploymentStructureTask() {
        outputs.upToDateWhen { false }
    }

    @Internal
    Set<File> getDeployConfigurationFiles() {
        getDeployConfiguration().files
    }

    @OutputFile
    File getOutputFile() {
        if (outputFile == null) {
            outputFile = new File(project.getBuildDir(), "/$name/" + OUTPUT_FILENAME)
        }
        return outputFile
    }

    private Configuration getDeployConfiguration() {
        return project.configurations.getByName(EarPlugin.DEPLOY_CONFIGURATION_NAME)
    }

    @TaskAction
    void writeFile() {
        JBossDeploymentStructure jbossDeploymentStructure = project.getExtensions().getByType(JBossDeploymentStructure)

        jbossDeploymentStructure.addSubdeployments(getSubdeploymentsConfiguredInDeployProjectDependencies())
        jbossDeploymentStructure.addSubdeployments(createSubdeploymentsFromDeployArtifacts())

        Node root = jbossDeploymentStructure.saveToXml(null)
        getOutputFile().withPrintWriter { writer -> new XmlNodePrinter(writer).print(root) }
    }

    private List<Subdeployment> createSubdeploymentsFromDeployArtifacts() {
        return getDeployConfiguration().getResolvedConfiguration().resolvedArtifacts
                .collect { artifact -> new Subdeployment(artifact.file.getName()) }
    }

    private List<Subdeployment> getSubdeploymentsConfiguredInDeployProjectDependencies() {
        def deploymentDependencies = getDeployConfiguration().getDependencies()
        return deploymentDependencies.withType(ProjectDependency)
                .collect { p -> p.dependencyProject.extensions.findByType(Subdeployment) }
                .findAll { p -> p != null }
    }


}
