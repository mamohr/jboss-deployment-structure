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

import nebula.test.IntegrationSpec
import nebula.test.functional.ExecutionResult
import spock.lang.Shared

import javax.xml.XMLConstants
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.SchemaFactory

class JBossDeploymentStructureIntSpec extends IntegrationSpec {
    @Shared
    private XmlParser parser = new XmlParser()
    def 'applying the plugin creates jboss-deployment-structure.xml'() {
        buildFile << '''
            apply plugin: 'ear'
            apply plugin: 'com.github.mamohr.jboss-deployment-structure'

        '''
        when:
        ExecutionResult result = runTasks('ear')

        then:
        result.wasExecuted("createJBossDeploymentStructure")
        fileExists('build/createJBossDeploymentStructure/jboss-deployment-structure.xml')
        fileIsValidForSchema(file('build/createJBossDeploymentStructure/jboss-deployment-structure.xml'))
        result.failure == null
    }

    def 'jbossDeploymentStructure extension configuration is saved to xml'() {

        buildFile << '''
            apply plugin: 'ear'
            apply plugin: 'com.github.mamohr.jboss-deployment-structure'

            jbossDeploymentStructure {
                dependency 'javax.faces.api:1.2'
                exclude 'javax.faces.api'
                subdeployments {
                    'my-war.war' {
                        dependency 'another.module'
                    }
                }
            }

        '''
        when:
        ExecutionResult result = runTasks('ear')

        then:
        result.wasExecuted("createJBossDeploymentStructure")
        fileIsValidForSchema(file('build/createJBossDeploymentStructure/jboss-deployment-structure.xml'))
        result.failure == null
    }

    def 'withXml can be used to change xml'() {
        buildFile << '''
            apply plugin: 'ear'
            apply plugin: 'com.github.mamohr.jboss-deployment-structure'

            jbossDeploymentStructure {
                dependency 'javax.faces.api:1.2'
                exclude 'javax.faces.api'
                subdeployments {
                    'my-war.war' {
                        dependency 'another.module'
                    }
                }
                withXml { node -> node.children().clear()}
            }

        '''
        when:
        ExecutionResult result = runTasks('ear')

        then:
        result.wasExecuted("createJBossDeploymentStructure")
        fileIsValidForSchema(file('build/createJBossDeploymentStructure/jboss-deployment-structure.xml'))
        def node = parser.parse(file('build/createJBossDeploymentStructure/jboss-deployment-structure.xml'))
        node.children().isEmpty()
        result.failure == null
    }

    def 'project deploy dependency creates subdeployment'() {
        helper.addSubproject("module")
        buildFile << '''
            apply plugin: 'ear'
            apply plugin: 'com.github.mamohr.jboss-deployment-structure'

            dependencies {
                deploy project(':module')
            }

            subprojects {
                apply plugin: 'java'
            }

        '''
        when:
        ExecutionResult result = runTasks('ear')

        then:
        result.wasExecuted("createJBossDeploymentStructure")
        fileIsValidForSchema(file('build/createJBossDeploymentStructure/jboss-deployment-structure.xml'))
        def node = parser.parse(file('build/createJBossDeploymentStructure/jboss-deployment-structure.xml'))
        ((NodeList) node.get('sub-deployment')).size() == 1
        result.failure == null
    }

    def 'global exclude is propagated to deployment and all subdeployments'() {
        helper.addSubproject('module')
        buildFile << '''
            apply plugin: 'ear'
            apply plugin: 'com.github.mamohr.jboss-deployment-structure'

            dependencies {
                deploy project(':module')
            }

            jbossDeploymentStructure {
                globalExclude 'javax.faces.api:1.2'
            }

            subprojects {
                apply plugin: 'java'
            }

        '''
        when:
        ExecutionResult result = runTasks('ear')

        then:
        result.wasExecuted("createJBossDeploymentStructure")
        fileIsValidForSchema(file('build/createJBossDeploymentStructure/jboss-deployment-structure.xml'))
        def node = parser.parse(file('build/createJBossDeploymentStructure/jboss-deployment-structure.xml'))
        node.'sub-deployment'.exclusions.module.@name.get(0) == 'javax.faces.api'
        node.'deployment'.exclusions.module.@name.get(0) == 'javax.faces.api'
        result.failure == null
    }


    def 'Subdeployment in subproject is merged to subdeployment in ear'() {
        String subprojectGradle = '''
        apply plugin: 'java'
        apply plugin: 'com.github.mamohr.jboss-deployment-structure'

        jbossSubdeployment {
            exclude 'not_needed:1.2'
        }
        '''

        helper.addSubproject('module', subprojectGradle)
        buildFile << '''
        apply plugin: 'ear'
            apply plugin: 'com.github.mamohr.jboss-deployment-structure\'

            dependencies {
                deploy project(':module')
            }

            jbossDeploymentStructure {
                subdeployments {
                    'module.jar' {
                        exclude 'second_unneeded_module'
                    }
                }
            }

            subprojects {
                apply plugin: 'java'
            }
        '''
        when:
        runTasks('ear')
        then:
        fileIsValidForSchema(file('build/createJBossDeploymentStructure/jboss-deployment-structure.xml'))
        def node = this.parser.parse(file('build/createJBossDeploymentStructure/jboss-deployment-structure.xml'))
        node.'sub-deployment'.size() == 1
        node.'sub-deployment'.exclusions.module.size() == 2

    }

    def 'applying to project without jar, war or ear tasks throws exception'() {
        buildFile << '''
            apply plugin: 'com.github.mamohr.jboss-deployment-structure'
        '''
        when:
        ExecutionResult result = runTasks(CreateJBossDeploymentStructureTask.TASK_NAME)
        then:
        result.failure != null
    }

    def 'applying to project before ear task is working'() {
        buildFile << '''
            apply plugin: 'com.github.mamohr.jboss-deployment-structure'
            apply plugin: 'ear'

            jbossDeploymentStructure {
                dependency 'my-module:1.3'
            }
        '''
        when:
        ExecutionResult result = runTasks(CreateJBossDeploymentStructureTask.TASK_NAME)
        then:
        fileIsValidForSchema(file('build/createJBossDeploymentStructure/jboss-deployment-structure.xml'))
        result.failure == null
    }

    def 'applying module with services and meta-inf attribute is working'() {
        buildFile << '''
            apply plugin: 'com.github.mamohr.jboss-deployment-structure'
            apply plugin: 'ear'

            jbossDeploymentStructure {
                dependency ('my-module:1.3') {
                    services = 'NONE'
                    metaInf = 'IMPORT'
                }
            }
        '''
        when:
        ExecutionResult result = runTasks(CreateJBossDeploymentStructureTask.TASK_NAME)

        then:
        result.failure == null
        fileIsValidForSchema(file('build/createJBossDeploymentStructure/jboss-deployment-structure.xml'))
        def node = parser.parse(file('build/createJBossDeploymentStructure/jboss-deployment-structure.xml'))
        def module = node.'deployment'.dependencies.module.get(0)
        module.@services == 'none'
        module.'@meta-inf' == 'import'
        result.failure == null
    }

    def 'ear subdeployment isolated is respected'() {
        buildFile << '''
            apply plugin: 'com.github.mamohr.jboss-deployment-structure'
            apply plugin: 'ear'

            jbossDeploymentStructure {
                earSubdeploymentsIsolated = true
            }
        '''
        when:
        ExecutionResult result = runTasks(CreateJBossDeploymentStructureTask.TASK_NAME)

        then:
        result.failure == null
        fileIsValidForSchema(file('build/createJBossDeploymentStructure/jboss-deployment-structure.xml'))
        def node = parser.parse(file('build/createJBossDeploymentStructure/jboss-deployment-structure.xml'))
        def isolated = node.'ear-subdeployments-isolated'
        isolated.text().trim() == 'true'
    }

    def 'Sample in readme creates valid xml'() {
        buildFile << '''
        apply plugin: 'com.github.mamohr.jboss-deployment-structure'
        apply plugin: 'ear'
        
        jbossDeploymentStructure {
            structureVersion ='1.2' //JBoss deployment structure schema version
            earSubdeploymentsIsolated = false
            globalExclude 'my-excluded-module:1.1' //global excludes will be added to the deployment and ALL subdeployments
            exclude 'my-other-excluded-module' // exclude will be added to the deployment
            dependency 'my-dependency:1.1' //Adds dependency 'my-dependency' with slot '1.1' to deployment
            dependency ('my-other-dependency') { //Adds dependency with additional attributes
                slot = '1.1'
                export = true
                optional = true
                annotations = true
                services = 'NONE' //possible values [NONE, IMPORT, EXPORT]
                metaInf = 'IMPORT' //possible values [NONE, IMPORT, EXPORT]

                imports { //Configure imports with exclusions or inclusions of paths
                    exclude 'lib/ext'
                    include 'ext'
                }

                exports { //Configure exports
                    exclude 'lib/ext'
                    include 'ext'
                }
            }
            resource 'my-library.jar\'
            resource path: 'lib/ext-library.jar', physicalCodeSource: true
            
            subdeployments { // Configure additional subdeployments
                'my-war.war' {
                    dependency 'another.module'
                    exclude 'excluded.module'
                }
            }
        }'''
        when:
        ExecutionResult result = runTasks(CreateJBossDeploymentStructureTask.TASK_NAME)

        then:
        result.failure == null
        fileIsValidForSchema(file('build/createJBossDeploymentStructure/jboss-deployment-structure.xml'))
    }

    def 'Global excludes are added to subdeployments defined in jbossDeploymentStructure'() {
        buildFile << '''
        apply plugin: 'com.github.mamohr.jboss-deployment-structure'
        apply plugin: 'ear'
        
        jbossDeploymentStructure {
            structureVersion ='1.2' //JBoss deployment structure schema version
            earSubdeploymentsIsolated = false
            globalExclude 'my-excluded-module:1.1' //global excludes will be added to the deployment and ALL subdeployments
            exclude 'my-other-excluded-module' // exclude will be added to the deployment          
            subdeployments { // Configure additional subdeployments
                'my-war.war' {
                    exclude 'excluded.module'
                }
            }
        }'''
        when:
        ExecutionResult result = runTasks(CreateJBossDeploymentStructureTask.TASK_NAME)

        then:
        result.failure == null
        fileIsValidForSchema(file('build/createJBossDeploymentStructure/jboss-deployment-structure.xml'))
        def node = parser.parse(file('build/createJBossDeploymentStructure/jboss-deployment-structure.xml'))
        def deploymentExclusions = node.'deployment'.exclusions.module
        deploymentExclusions.size() == 2
        def subdeploymentExclusions = node.'sub-deployment'.exclusions.module
        subdeploymentExclusions.size() == 2
    }


    def 'SubSystem exclude is inserted'() {
        helper.addSubproject('module')
        buildFile << '''
            apply plugin: 'ear'
            apply plugin: 'com.github.mamohr.jboss-deployment-structure'

            jbossDeploymentStructure {
                excludeSubSystem 'jaxrs'
            }

        '''
        when:
        ExecutionResult result = runTasks('ear')

        then:
        result.wasExecuted("createJBossDeploymentStructure")
        fileIsValidForSchema(file('build/createJBossDeploymentStructure/jboss-deployment-structure.xml'))
        def node = parser.parse(file('build/createJBossDeploymentStructure/jboss-deployment-structure.xml'))
        node.'deployment'['exclude-subsystems'].subsystem.@name.get(0) == 'jaxrs'
        result.failure == null
    }

    boolean fileIsValidForSchema(File file) {
        def factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)

        def stream = JBossDeploymentStructureIntSpec.getResourceAsStream('/jboss-deployment-structure-1_2.xsd')
        def schema = factory.newSchema(new StreamSource(stream))
        def validator = schema.newValidator()
        validator.validate(new StreamSource(new FileInputStream(file)))
        return true
    }

}