/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dtstack.batch.service.datasource.impl;

import com.dtstack.batch.dao.BatchDataSourceTaskRefDao;
import com.dtstack.batch.dto.BatchDataSourceTaskDto;
import com.dtstack.batch.web.pager.PageQuery;
import com.dtstack.engine.common.annotation.Forbidden;
import com.dtstack.engine.common.enums.Deleted;
import com.dtstack.engine.domain.BatchDataSourceTaskRef;
import com.dtstack.engine.domain.BatchTask;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 处理datasource 和 task之间的关联关系
 * Date: 2017/8/22
 * Company: www.dtstack.com
 * @author xuchao
 */

@Service
public class BatchDataSourceTaskRefService {

    @Autowired
    private BatchDataSourceTaskRefDao dataSourceTaskRefDao;

    @Forbidden
    public void addRef(Long dataSourceId, long taskId, Long tenantId) {
        BatchDataSourceTaskRef ref = dataSourceTaskRefDao.getBySourceIdAndTaskId(dataSourceId, taskId);
        Timestamp current = Timestamp.valueOf(LocalDateTime.now());

        if (ref == null) {//不存在则插入
            ref = new BatchDataSourceTaskRef();
            ref.setDataSourceId(dataSourceId);
            ref.setTaskId(taskId);
            ref.setTenantId(tenantId);
            ref.setGmtCreate(current);
            ref.setGmtModified(current);
            ref.setIsDeleted(Deleted.NORMAL.getStatus());
        } else {//更新修改时间
            ref.setGmtModified(current);
            ref.setTenantId(tenantId);
        }

        addOrUpdate(ref);
    }

    @Forbidden
    public BatchDataSourceTaskRef addOrUpdate(BatchDataSourceTaskRef batchDataSourceTaskRef) {
        if (batchDataSourceTaskRef.getId() > 0) {
            dataSourceTaskRefDao.update(batchDataSourceTaskRef);
        } else {
            dataSourceTaskRefDao.insert(batchDataSourceTaskRef);
        }
        return batchDataSourceTaskRef;
    }

    @Forbidden
    public void removeRef(Long taskId) {
        if (taskId != null && taskId > 0){
            dataSourceTaskRefDao.deleteByTaskId(taskId);
        }
    }

    @Forbidden
    public Integer getSourceRefCount(long dataSourceId) {
        return dataSourceTaskRefDao.countBySourceId(dataSourceId,null);
    }

    @Forbidden
    public void copyTaskDataSource(Long srcTaskId, BatchTask distTask){
        List<Long> sourceIds = dataSourceTaskRefDao.listSourceIdByTaskId(srcTaskId);
        if(CollectionUtils.isNotEmpty(sourceIds)){
            for (Long sourceId : sourceIds) {
                addRef(sourceId, distTask.getId(), distTask.getTenantId());
            }
        }
    }

    public Integer countBySourceId(Long dataSourceId, String taskName) {
        return dataSourceTaskRefDao.countBySourceId(dataSourceId, taskName);
    }

    public List<BatchTask> pageQueryBySourceId(PageQuery<BatchDataSourceTaskDto> pageQuery) {
        return dataSourceTaskRefDao.pageQueryBySourceId(pageQuery);
    }
}
