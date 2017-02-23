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

import org.custommonkey.xmlunit.XMLUnit
import org.gradle.api.InvalidUserDataException
import org.gradle.api.internal.AbstractNamedDomainObjectContainer
import org.gradle.internal.reflect.DirectInstantiator
import spock.lang.Specification

import static com.github.mamohr.gradle.deploymentstructure.XmlTestHelper.nodeIsSimilarToString

/**
 * Created by mario on 12.01.2015.
 */
class JBossDeploymentStructureTest extends Specification {
    private JBossDeploymentStructure structure = new JBossDeploymentStructure(new AbstractNamedDomainObjectContainer<Subdeployment>
    (Subdeployment, new DirectInstantiator()) {
        @Override
        protected Subdeployment doCreate(String name) {
            def subdeployment = new Subdeployment()
            subdeployment.setName(name)
            return subdeployment
        }
    })


    def setupSpec() {
        XMLUnit.setIgnoreWhitespace(true)
    }

    def 'empty deployment structure creates valid xml'() {
        String expectedXml =
                '''<jboss-deployment-structure xmlns="urn:jboss:deployment-structure:1.2">
              <deployment>
                <dependencies/>
              </deployment>
            </jboss-deployment-structure>'''.stripIndent()
        when:
        Node xml = structure.saveToXml(null);
        then:
        nodeIsSimilarToString(xml, expectedXml)
    }

    def 'dependecy module with export is created'() {
        String expectedXml =
                '''<jboss-deployment-structure xmlns="urn:jboss:deployment-structure:1.2">
              <deployment>
                <dependencies>
                    <module name="my-dependency" slot="1.1" export="true"/>
                </dependencies>
              </deployment>
            </jboss-deployment-structure>'''.stripIndent()
        when:
        structure.dependency('my-dependency:1.1') { dep ->
            dep.export = true
        }
        Node xml = structure.saveToXml(null);
        then:
        nodeIsSimilarToString(xml, expectedXml)
    }

    def 'subdeployment is added'() {
        String expectedXml = '''
            <jboss-deployment-structure xmlns="urn:jboss:deployment-structure:1.2">
              <deployment>
                <dependencies/>
              </deployment>
              <sub-deployment name="my-ejb.jar">
                <dependencies/>
              </sub-deployment>
            </jboss-deployment-structure>'''.stripIndent()
        when:
        structure.subdeployments.create("my-ejb.jar")
        Node xml = structure.saveToXml(null);
        then:
        nodeIsSimilarToString(xml, expectedXml)
    }

    def 'ear subdeployment isolation tag is added if set to false'() {
        String expectedXml = '''
            <jboss-deployment-structure xmlns="urn:jboss:deployment-structure:1.2">
                <ear-subdeployments-isolated>false</ear-subdeployments-isolated>
                <deployment>
                    <dependencies/>
                </deployment>
            </jboss-deployment-structure>'''.stripIndent()
        when:
        structure.earSubdeploymentsIsolated = false
        Node xml = structure.saveToXml(null);
        then:
        nodeIsSimilarToString(xml, expectedXml)
    }

    def 'resources are added'() {
        String expectedXml = '''
            <jboss-deployment-structure xmlns="urn:jboss:deployment-structure:1.2">
                <deployment>
                    <dependencies/>
                    <resources>
                        <resource-root path="my-library.jar"/>
                        <resource-root path="lib/ext-library.jar" use-physical-code-source="true"/>
                    </resources>
                </deployment>
            </jboss-deployment-structure>'''.stripIndent()
        when:
        structure.resource 'my-library.jar'
        structure.resource path: 'lib/ext-library.jar', physicalCodeSource: true
        Node xml = structure.saveToXml(null)
        then:
        nodeIsSimilarToString(xml, expectedXml)
    }

    def 'resource filter is added to subdeployment'() {
        Subdeployment subdeployment = new Subdeployment('my-war.war')
        String expectedXml = '''
            <jboss-deployment-structure xmlns="urn:jboss:deployment-structure:1.2">
                <deployment>
                    <dependencies />
                </deployment>
                <sub-deployment name="my-war.war">
                    <dependencies />
                    <resources>
                        <resource-root path="ext-library.jar"/>
                        <filter>
                            <include path="api/123"/>
                            <exclude path="lib/456"/>
                        </filter>
                    </resources>
                </sub-deployment>
            </jboss-deployment-structure>'''.stripIndent()
        when:
        subdeployment.resource 'ext-library.jar'
        subdeployment.resourceFilter {
            include 'api/123'
            exclude 'lib/456'
        }
        structure.subdeployments.add(subdeployment)
        Node xml = structure.saveToXml(null)
        then:
        nodeIsSimilarToString(xml, expectedXml)
    }

    def 'throws exception when no resources are present and filter is applied'() {
        Subdeployment subdeployment = new Subdeployment('my-war.war')
        when:
        subdeployment.resourceFilter {
            include 'api'
        }
        structure.subdeployments.add(subdeployment)
        Node xml = structure.saveToXml(null)
        then:
        thrown InvalidUserDataException
    }

}
