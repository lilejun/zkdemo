package com.lilejun.spzk.zk.zkconfig;


import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InitNodeCacheListener implements CommandLineRunner {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(InitNodeCacheListener.class);

    @Autowired
    private CuratorFramework zkClient;

    @Override
    public void run(String... args) throws Exception {
        log.info("开始监听当前节点");
        NodeCache nodeCache = new NodeCache(zkClient,"/test123");
        nodeCache.start();
        nodeCache.getListenable().addListener(new NodeCacheListener() {
            @Override
            public void nodeChanged() throws Exception {
                log.info("/test123对应的数据是{}",new String(nodeCache.getCurrentData().getData()));
            }
        });
    }
}
