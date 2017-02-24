package com.github.mamohr.gradle.deploymentstructure.model

import org.apache.ivy.util.XMLHelper
import org.custommonkey.xmlunit.XMLUnit
import spock.lang.Specification

import static com.github.mamohr.gradle.deploymentstructure.XmlTestHelper.nodeIsSimilarToString;

/**
 * Created by cschmidt on 24.02.2017.
 */
public class DeploymentTest extends Specification {

    private Deployment deployment = new Deployment()

    def setupSpec() {
        XMLUnit.setIgnoreWhitespace(true)
    }

    def 'resources are added'() {
        String expectedXml =
                '''<deployment>
                    <dependencies />
                    <resources>
                        <resource-root path="my-library.jar" />
                        <resource-root path="lib/ext-library.jar" use-physical-code-source="true" />
                    </resources>
                </deployment>'''.stripIndent()
        when:
        deployment.resource 'my-library.jar'
        deployment.resource path: 'lib/ext-library.jar', physicalCodeSource: true
        Node xml = deployment.saveToXml(null)
        then:
        nodeIsSimilarToString(xml, expectedXml)
    }

    def 'resources are added with filter'() {
        String expectedXml =
                '''<deployment>
                    <dependencies />
                    <resources>
                        <resource-root path="my-library.jar">
                            <filter>
                                <include path="api" />
                                <exclude path="lib" />
                            </filter>
                        </resource-root>
                        <resource-root path="lib/ext-library.jar" use-physical-code-source="true">
                            <filter>
                                <include path="api" />
                                <exclude path="lib" />
                            </filter>
                        </resource-root>
                    </resources>
                </deployment>'''.stripIndent()
        when:
        deployment.resource('my-library.jar') {
            include 'api'
            exclude 'lib'
        }
        deployment.resource(path: 'lib/ext-library.jar', physicalCodeSource: true) {
            include 'api'
            exclude 'lib'
        }
        Node xml = deployment.saveToXml(null)
        then:
        nodeIsSimilarToString(xml, expectedXml)
    }

}
