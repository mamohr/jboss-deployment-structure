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
import nebula.test.PluginProjectSpec
import org.gradle.plugins.ear.EarPlugin

/**
 * Created by mario on 10.01.2015.
 */
class JBossDeploymentStructurePluginSpec extends PluginProjectSpec {
    @Override
    String getPluginName() {
        return "com.github.mamohr.jboss-deployment-structure"
    }

    def 'applying to ear project adds jbossDeploymentStrcuture extension'() {
        when:
        project.plugins.apply(EarPlugin)
        project.plugins.apply(JBossDeploymentStructurePlugin)
        then:
        project.extensions.findByName(JBossDeploymentStructure.EXTENSION_NAME) != null
    }

    def 'applying on ear project creates task'() {
        project.plugins.apply(EarPlugin)

        when:
        project.plugins.apply(JBossDeploymentStructurePlugin)

        then:
        project.tasks.getByName(CreateJBossDeploymentStructureTask.TASK_NAME) != null
    }

    def 'applying on non ear project does not create task'() {
        when:
        project.plugins.apply(JBossDeploymentStructurePlugin)

        then:
        project.tasks.findByName(CreateJBossDeploymentStructureTask.TASK_NAME) == null

    }
}
