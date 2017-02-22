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
import spock.lang.Specification

import static com.github.mamohr.gradle.deploymentstructure.XmlTestHelper.nodeIsSimilarToString

/**
 * Created by mario on 15.01.2015.
 */
class DependencyModuleTest extends Specification {

    def DependencyModule dependencyModule = new DependencyModule("my-dependency")

    def setupSpec() {
        XMLUnit.setIgnoreWhitespace(true)
    }

    def 'empty dependency creates valid xml'() {
        String expectedXml = '<module name="my-dependency" slot="main"/>';
        when:
        Node xml = dependencyModule.saveToXml(null);
        then:
        nodeIsSimilarToString(xml, expectedXml)
    }

    def 'optional attribute is created if true'() {
        String expectedXml =
                '<module name="my-dependency" slot="main" optional="true"/>'.stripIndent()
        when:
        dependencyModule.optional = true
        Node xml = dependencyModule.saveToXml(null);
        then:
        nodeIsSimilarToString(xml, expectedXml)
    }

    def 'exported attribute is created if true'() {
        String expectedXml =
                '<module name="my-dependency" slot="main" export="true"/>'.stripIndent()
        when:
        dependencyModule.export = true
        Node xml = dependencyModule.saveToXml(null);
        then:
        nodeIsSimilarToString(xml, expectedXml)
    }

    def 'annotations attribute is created if true'() {
        String expectedXml =
                '<module name="my-dependency" slot="main" annotations="true"/>'.stripIndent()
        when:
        dependencyModule.annotations = true
        Node xml = dependencyModule.saveToXml(null);
        then:
        nodeIsSimilarToString(xml, expectedXml)
    }

    def 'meta-inf attribute is created'() {
        String expectedXml =
                '<module name="my-dependency" slot="main" meta-inf="import"/>'.stripIndent()
        when:
        dependencyModule.metaInf = DependencyModule.DispositionType.IMPORT;
        Node xml = dependencyModule.saveToXml(null);
        then:
        nodeIsSimilarToString(xml, expectedXml)

    }

    def 'services attribute is created'() {
        String expectedXml =
                '<module name="my-dependency" slot="main" services="export"/>'.stripIndent()
        when:
        dependencyModule.services = DependencyModule.DispositionType.EXPORT;
        Node xml = dependencyModule.saveToXml(null);
        then:
        nodeIsSimilarToString(xml, expectedXml)

    }

}
