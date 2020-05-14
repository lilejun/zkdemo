package com.lilejun.spzk.zk.zkconfig;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InitTreeCacheListener implements CommandLineRunner {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(InitTreeCacheListener.class);

    @Autowired
    private CuratorFramework zkClient;

    @Override
    public void run(String... args) throws Exception {
        log.info("开始监听该目录全部节点");
        TreeCache treeCache = TreeCache.newBuilder(zkClient,"/test123")
                .setCacheData(true)
                .setMaxDepth(2).build();
        treeCache.start();
        treeCache.getListenable().addListener(new TreeCacheListener() {
            @Override
            public void childEvent(CuratorFramework curatorFramework, TreeCacheEvent treeCacheEvent) throws Exception {
                if(treeCacheEvent.getType().equals(TreeCacheEvent.Type.INITIALIZED)){
                    log.info("全目录节点初始化ok..");
                }else if(treeCacheEvent.getType().equals(TreeCacheEvent.Type.NODE_ADDED)){
                    log.info("添加子节点, path:{}, data:{}", treeCacheEvent.getData().getPath(), new String(treeCacheEvent.getData().getData()));
                }else if(treeCacheEvent.getType().equals(TreeCacheEvent.Type.NODE_UPDATED)){
                    log.info("修改节点, path:{}, data:{}", treeCacheEvent.getData().getPath(), new String(treeCacheEvent.getData().getData()));
                }
            }
        });
    }
}
