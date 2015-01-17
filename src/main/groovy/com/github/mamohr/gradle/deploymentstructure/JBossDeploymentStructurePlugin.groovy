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
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.WarPlugin
import org.gradle.api.tasks.StopExecutionException
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.bundling.War
import org.gradle.plugins.ear.Ear
import org.gradle.plugins.ear.EarPlugin

class JBossDeploymentStructurePlugin implements Plugin<Project> {

    JBossDeploymentStructurePlugin() {
    }

    void apply(Project target) {
        target.plugins.withType(EarPlugin) {
            target.extensions.create(JBossDeploymentStructure.EXTENSION_NAME, JBossDeploymentStructure, target.container(Subdeployment))
            def createTask = target.tasks.create(CreateJBossDeploymentStructureTask.TASK_NAME, CreateJBossDeploymentStructureTask)
            createTask.wireTo((Ear) target.tasks.findByName(EarPlugin.EAR_TASK_NAME))
        }
        if (!target.plugins.hasPlugin(EarPlugin)) {
            Subdeployment subdeployment = target.extensions.create(Subdeployment.EXTENSION_NAME, Subdeployment)
            target.afterEvaluate {
                if (!subdeployment.name) {
                    Jar task = (War) target.tasks.findByName(WarPlugin.WAR_TASK_NAME)
                    if (!task) {
                        task = (Jar) target.tasks.findByName(JavaPlugin.JAR_TASK_NAME)
                    }
                    if (!task) {
                        throw new StopExecutionException("No name for jboss subdeployment set and no war or jar task available")
                    }
                    subdeployment.setName(task.getArchiveName())
                }
            }
        }
    }
}