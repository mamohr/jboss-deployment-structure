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

import org.custommonkey.xmlunit.Diff

/**
 * Created by mario on 15.01.2015.
 */
class XmlTestHelper {
    def static boolean nodeIsSimilarToString(Node node, String expectedString) {
        StringWriter sw = new StringWriter()
        new XmlNodePrinter(new PrintWriter(sw)).print(node)
        String nodeString = sw.toString()
        def diff = new Diff(expectedString, nodeString)
        diff.similar()
    }
}
