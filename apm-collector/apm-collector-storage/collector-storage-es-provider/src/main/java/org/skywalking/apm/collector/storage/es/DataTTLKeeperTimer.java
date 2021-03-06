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

package org.skywalking.apm.collector.storage.es;

import org.skywalking.apm.collector.core.module.ModuleManager;
import org.skywalking.apm.collector.storage.StorageModule;
import org.skywalking.apm.collector.storage.dao.*;

import java.util.Calendar;

/**
 * 过期数据删除定时器
 *
 * @author peng-yongsheng
 */
public class DataTTLKeeperTimer {

    private final ModuleManager moduleManager;
    private final StorageModuleEsNamingListener namingListener;
    private final String selfAddress;
    private final int daysBefore;

    public DataTTLKeeperTimer(ModuleManager moduleManager,
        StorageModuleEsNamingListener namingListener, String selfAddress, int daysBefore) {
        this.moduleManager = moduleManager;
        this.namingListener = namingListener;
        this.selfAddress = selfAddress;
        this.daysBefore = daysBefore;
    }

    public void start() {
        //TODO: Don't release auto delete feature, yet
//        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this::delete, 1, 8, TimeUnit.HOURS);
//        delete();
    }

    private void delete() {
        // 计算 删除开始时间与结束时间
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.DAY_OF_MONTH, -daysBefore);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long startTimestamp = calendar.getTimeInMillis();

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        long endTimestamp = calendar.getTimeInMillis();

        // 执行删除
        deleteJVMRelatedData(startTimestamp, endTimestamp);
        deleteTraceRelatedData(startTimestamp, endTimestamp);
    }

    private void deleteJVMRelatedData(long startTimestamp, long endTimestamp) {
        // CpuMetric
        ICpuMetricPersistenceDAO cpuMetricPersistenceDAO = moduleManager.find(StorageModule.NAME).getService(ICpuMetricPersistenceDAO.class);
        cpuMetricPersistenceDAO.deleteHistory(startTimestamp, endTimestamp);

        // CMetric
        IGCMetricPersistenceDAO gcMetricPersistenceDAO = moduleManager.find(StorageModule.NAME).getService(IGCMetricPersistenceDAO.class);
        gcMetricPersistenceDAO.deleteHistory(startTimestamp, endTimestamp);

        // MemoryMetric
        IMemoryMetricPersistenceDAO memoryMetricPersistenceDAO = moduleManager.find(StorageModule.NAME).getService(IMemoryMetricPersistenceDAO.class);
        memoryMetricPersistenceDAO.deleteHistory(startTimestamp, endTimestamp);

        // MemoryPoolMetric
        IMemoryPoolMetricPersistenceDAO memoryPoolMetricPersistenceDAO = moduleManager.find(StorageModule.NAME).getService(IMemoryPoolMetricPersistenceDAO.class);
        memoryPoolMetricPersistenceDAO.deleteHistory(startTimestamp, endTimestamp);
    }

    private void deleteTraceRelatedData(long startTimestamp, long endTimestamp) {
        // GlobalTrace
        IGlobalTracePersistenceDAO globalTracePersistenceDAO = moduleManager.find(StorageModule.NAME).getService(IGlobalTracePersistenceDAO.class);
        globalTracePersistenceDAO.deleteHistory(startTimestamp, endTimestamp);

        // InstPerformance
        IInstPerformancePersistenceDAO instPerformancePersistenceDAO = moduleManager.find(StorageModule.NAME).getService(IInstPerformancePersistenceDAO.class);
        instPerformancePersistenceDAO.deleteHistory(startTimestamp, endTimestamp);

        // NodeComponent
        INodeComponentPersistenceDAO nodeComponentPersistenceDAO = moduleManager.find(StorageModule.NAME).getService(INodeComponentPersistenceDAO.class);
        nodeComponentPersistenceDAO.deleteHistory(startTimestamp, endTimestamp);

        // NodeMapping
        INodeMappingPersistenceDAO nodeMappingPersistenceDAO = moduleManager.find(StorageModule.NAME).getService(INodeMappingPersistenceDAO.class);
        nodeMappingPersistenceDAO.deleteHistory(startTimestamp, endTimestamp);

        // NodeReference
        INodeReferencePersistenceDAO nodeReferencePersistenceDAO = moduleManager.find(StorageModule.NAME).getService(INodeReferencePersistenceDAO.class);
        nodeReferencePersistenceDAO.deleteHistory(startTimestamp, endTimestamp);

        // SegmentCost
        ISegmentCostPersistenceDAO segmentCostPersistenceDAO = moduleManager.find(StorageModule.NAME).getService(ISegmentCostPersistenceDAO.class);
        segmentCostPersistenceDAO.deleteHistory(startTimestamp, endTimestamp);

        // Segment
        ISegmentPersistenceDAO segmentPersistenceDAO = moduleManager.find(StorageModule.NAME).getService(ISegmentPersistenceDAO.class);
        segmentPersistenceDAO.deleteHistory(startTimestamp, endTimestamp);

        // ServiceReference
        IServiceReferencePersistenceDAO serviceReferencePersistenceDAO = moduleManager.find(StorageModule.NAME).getService(IServiceReferencePersistenceDAO.class);
        serviceReferencePersistenceDAO.deleteHistory(startTimestamp, endTimestamp);
    }
}
