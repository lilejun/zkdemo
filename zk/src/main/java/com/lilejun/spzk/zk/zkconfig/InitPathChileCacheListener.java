package com.lilejun.spzk.zk.zkconfig;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class InitPathChileCacheListener implements CommandLineRunner {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(InitPathChileCacheListener.class);

    @Autowired
    private CuratorFramework zkClient;

    @Override
    public void run(String... args) throws Exception {
        try {
            log.info("开始监听子节点");
            // 监听父节点以下所有的子节点, 当子节点发生变化的时候(增删改)都会监听到
            // 为子节点添加watcher事件
            // PathChildrenCache：监听数据节点的增删改
            PathChildrenCache childrenCache = new PathChildrenCache(zkClient, "/test1234", true);
            // NORMAL:异步初始化, BUILD_INITIAL_CACHE:同步初始化, POST_INITIALIZED_EVENT:异步初始化,初始化之后会触发事件
            childrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
            // 当前数据节点的子节点数据列表
            List<ChildData> childDataList = childrenCache.getCurrentData();
            childrenCache.getListenable().addListener(new PathChildrenCacheListener() {
                @Override
                public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                    if (event.getType().equals(PathChildrenCacheEvent.Type.INITIALIZED)) {
                        log.info("子节点初始化ok..");
                    } else if (event.getType().equals(PathChildrenCacheEvent.Type.CHILD_ADDED)) {
                        log.info("添加子节点, path:{}, data:{}", event.getData().getPath(), new String(event.getData().getData()));
                    } else if (event.getType().equals(PathChildrenCacheEvent.Type.CHILD_REMOVED)) {
                        log.info("删除子节点, path:{}", event.getData().getPath());
                    } else if (event.getType().equals(PathChildrenCacheEvent.Type.CHILD_UPDATED)) {
                        log.info("修改子节点, path:{}, data:{}", event.getData().getPath(), event.getData().getData());
                    }
                }
            });
        } catch (Exception e) {
            log.error("监听path=/test1234节点出错{}", e);
        }
    }
}
