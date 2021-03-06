/*
 * Copyright 2017, OpenSkywalking Organization All rights reserved.
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
 *
 * Project repository: https://github.com/OpenSkywalking/skywalking
 */

package org.skywalking.apm.collector.ui.jetty;

import java.util.Properties;
import org.skywalking.apm.collector.cache.CacheModule;
import org.skywalking.apm.collector.cluster.ClusterModule;
import org.skywalking.apm.collector.cluster.service.ModuleListenerService;
import org.skywalking.apm.collector.cluster.service.ModuleRegisterService;
import org.skywalking.apm.collector.core.module.Module;
import org.skywalking.apm.collector.core.module.ModuleProvider;
import org.skywalking.apm.collector.core.module.ServiceNotProvidedException;
import org.skywalking.apm.collector.jetty.manager.JettyManagerModule;
import org.skywalking.apm.collector.jetty.manager.service.JettyManagerService;
import org.skywalking.apm.collector.naming.NamingModule;
import org.skywalking.apm.collector.naming.service.NamingHandlerRegisterService;
import org.skywalking.apm.collector.server.Server;
import org.skywalking.apm.collector.storage.StorageModule;
import org.skywalking.apm.collector.ui.UIModule;
import org.skywalking.apm.collector.ui.jetty.handler.SegmentTopGetHandler;
import org.skywalking.apm.collector.ui.jetty.handler.SpanGetHandler;
import org.skywalking.apm.collector.ui.jetty.handler.TraceDagGetHandler;
import org.skywalking.apm.collector.ui.jetty.handler.TraceStackGetHandler;
import org.skywalking.apm.collector.ui.jetty.handler.application.ApplicationsGetHandler;
import org.skywalking.apm.collector.ui.jetty.handler.instancehealth.InstanceHealthGetHandler;
import org.skywalking.apm.collector.ui.jetty.handler.instancemetric.InstanceMetricGetOneTimeBucketHandler;
import org.skywalking.apm.collector.ui.jetty.handler.instancemetric.InstanceMetricGetRangeTimeBucketHandler;
import org.skywalking.apm.collector.ui.jetty.handler.instancemetric.InstanceOsInfoGetHandler;
import org.skywalking.apm.collector.ui.jetty.handler.naming.UIJettyNamingHandler;
import org.skywalking.apm.collector.ui.jetty.handler.naming.UIJettyNamingListener;
import org.skywalking.apm.collector.ui.jetty.handler.servicetree.EntryServiceGetHandler;
import org.skywalking.apm.collector.ui.jetty.handler.servicetree.ServiceTreeGetByIdHandler;
import org.skywalking.apm.collector.ui.jetty.handler.time.AllInstanceLastTimeGetHandler;
import org.skywalking.apm.collector.ui.jetty.handler.time.OneInstanceLastTimeGetHandler;

/**
 * @author peng-yongsheng
 */
public class UIModuleJettyProvider extends ModuleProvider {

    public static final String NAME = "jetty";
    private static final String HOST = "host";
    private static final String PORT = "port";
    private static final String CONTEXT_PATH = "context_path";

    @Override public String name() {
        return NAME;
    }

    @Override public Class<? extends Module> module() {
        return UIModule.class;
    }

    @Override public void prepare(Properties config) throws ServiceNotProvidedException {
    }

    @Override public void start(Properties config) throws ServiceNotProvidedException {
        String host = config.getProperty(HOST);
        Integer port = (Integer)config.get(PORT);
        String contextPath = config.getProperty(CONTEXT_PATH);

        ModuleRegisterService moduleRegisterService = getManager().find(ClusterModule.NAME).getService(ModuleRegisterService.class);
        moduleRegisterService.register(UIModule.NAME, this.name(), new UIModuleJettyRegistration(host, port, contextPath));

        UIJettyNamingListener namingListener = new UIJettyNamingListener();
        ModuleListenerService moduleListenerService = getManager().find(ClusterModule.NAME).getService(ModuleListenerService.class);
        moduleListenerService.addListener(namingListener);

        NamingHandlerRegisterService namingHandlerRegisterService = getManager().find(NamingModule.NAME).getService(NamingHandlerRegisterService.class);
        namingHandlerRegisterService.register(new UIJettyNamingHandler(namingListener));

        JettyManagerService managerService = getManager().find(JettyManagerModule.NAME).getService(JettyManagerService.class);
        Server jettyServer = managerService.createIfAbsent(host, port, contextPath);
        addHandlers(jettyServer);
    }

    @Override public void notifyAfterCompleted() throws ServiceNotProvidedException {

    }

    @Override public String[] requiredModules() {
        return new String[] {ClusterModule.NAME, JettyManagerModule.NAME, NamingModule.NAME, CacheModule.NAME, StorageModule.NAME};
    }

    private void addHandlers(Server jettyServer) {
        jettyServer.addHandler(new ApplicationsGetHandler(getManager()));
        jettyServer.addHandler(new InstanceHealthGetHandler(getManager()));
        jettyServer.addHandler(new InstanceMetricGetOneTimeBucketHandler(getManager()));
        jettyServer.addHandler(new InstanceMetricGetRangeTimeBucketHandler(getManager()));
        jettyServer.addHandler(new InstanceOsInfoGetHandler(getManager()));
        jettyServer.addHandler(new EntryServiceGetHandler(getManager()));
        jettyServer.addHandler(new ServiceTreeGetByIdHandler(getManager()));
        jettyServer.addHandler(new AllInstanceLastTimeGetHandler(getManager()));
        jettyServer.addHandler(new OneInstanceLastTimeGetHandler(getManager()));
        jettyServer.addHandler(new SegmentTopGetHandler(getManager()));
        jettyServer.addHandler(new SpanGetHandler(getManager()));
        jettyServer.addHandler(new TraceDagGetHandler(getManager()));
        jettyServer.addHandler(new TraceStackGetHandler(getManager()));
    }
}
