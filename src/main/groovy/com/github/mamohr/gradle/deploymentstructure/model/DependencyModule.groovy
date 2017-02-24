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

/**
 * Created by mario on 09.01.2015.
 */
class DependencyModule extends Module {

    public static enum DispositionType {
        NONE,
        IMPORT,
        EXPORT
    }

    boolean export
    boolean optional
    boolean annotations

    final ConfigurablePathSet imports = new ConfigurablePathSet('imports')
    final ConfigurablePathSet exports = new ConfigurablePathSet('exports')

    DispositionType services
    DispositionType metaInf

    DependencyModule(String name) {
        super(name)
    }

    void imports(@DelegatesTo(ConfigurablePathSet) Closure cl) {
        cl.delegate = imports
        cl.resolveStrategy = Closure.DELEGATE_FIRST
        cl()
    }

    void exports(@DelegatesTo(ConfigurablePathSet) Closure cl) {
        cl.delegate = exports
        cl.resolveStrategy = Closure.DELEGATE_FIRST
        cl()
    }

    @Override
    Node saveToXml(Node node) {
        Node module = super.saveToXml(node)
        if (export) {
            module.attributes().export = export
        }
        if (optional) {
            module.attributes().optional = optional
        }
        if (annotations) {
            module.attributes().annotations = annotations
        }
        if (services) {
            module.attributes().services = services.name().toLowerCase()
        }
        if (metaInf) {
            module.attributes().'meta-inf' = metaInf.name().toLowerCase()
        }
        if(!imports.empty) {
            imports.saveToXml(module)
        }
        if(!exports.empty) {
            exports.saveToXml(module)
        }
        return module
    }

}
