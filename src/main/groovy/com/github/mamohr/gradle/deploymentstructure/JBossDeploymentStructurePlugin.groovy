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
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.StopExecutionException
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.bundling.War
import org.gradle.internal.reflect.Instantiator
import org.gradle.plugins.ear.Ear
import org.gradle.plugins.ear.EarPlugin

import javax.inject.Inject

class JBossDeploymentStructurePlugin implements Plugin<Project> {

    private JBossDeploymentStructure jBossDeploymentStructureExtension;
    private Subdeployment subdeployment;
    private Instantiator instantiator

    @Inject
    JBossDeploymentStructurePlugin(Instantiator instantiator) {
        this.instantiator = instantiator
    }

    void apply(Project target) {
        if(target.plugins.hasPlugin(EarPlugin)) {
            jBossDeploymentStructureExtension = target.extensions.create(JBossDeploymentStructure.EXTENSION_NAME, JBossDeploymentStructure, instantiator)
            target.tasks.withType(Ear) { earTask->
                def deploymentStructureTask = target.tasks.create(CreateJBossDeploymentStructureTask.TASK_NAME, CreateJBossDeploymentStructureTask);
                deploymentStructureTask.earTask = earTask
                earTask.getMetaInf().from(deploymentStructureTask.outputs)
                earTask.eachFile {f->logger.info(f.getPath())}
            }
        } else {
            subdeployment = target.extensions.create(Subdeployment.EXTENSION_NAME, Subdeployment)
            target.afterEvaluate {
                if (!subdeployment.name) {
                    Jar task = (War) target.tasks.findByName('war')
                    if (!task) {
                        task = (Jar) target.tasks.findByName('jar')
                    }
                    if(!task) {
                        throw new StopExecutionException("No name for jboss subdeployment set and no war or jar task available")
                    }
                    subdeployment.setName(task.getArchiveName())
                }
            }
        }

    }
}