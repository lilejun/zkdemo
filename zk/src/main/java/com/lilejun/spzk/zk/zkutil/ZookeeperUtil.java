package com.lilejun.spzk.zk.zkutil;


import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CountDownLatch;


@Component
@Slf4j
public class ZookeeperUtil {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(ZookeeperUtil.class);

    private final static String ROOT_PATH_LOCK = "rootlock";
    private CountDownLatch countDownLatch = new CountDownLatch(1);

    @Autowired
    private CuratorFramework zkClient;

    /**
     * 获取某个路径下的节点信息
     * path必须使类似/a/b这种路径
     */
    public List<String> getZKNodeForPath(String path) {
        try {
            return zkClient.getChildren().forPath(path);
        } catch (Exception e) {
            log.error("获取path=" + path + "下的子节点出错{}", e);
            return null;
        }finally {
            CloseableUtils.closeQuietly(zkClient);
        }
    }

    /**
     * 检查节点是否存在
     */
    public Boolean checkZkNodeExists(String path) {
        try {
            Stat stat = zkClient.checkExists().forPath(path);
            if (stat == null)
                return false;
            return true;
        } catch (Exception e) {
            log.error("检查path=" + path + "节点是否存在出错{}", e);
            return false;
        }
    }

    /**
     * 创建节点
     * path节点路径
     * value节点数据
     * createMode节点类型
     */
    public String createZkNode(String path, String value, CreateMode createMode) {
        try {
            String createPath = null;
            if (!this.checkZkNodeExists(path)) {
                createPath = zkClient.create().creatingParentContainersIfNeeded().withMode(createMode)
                        .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE).forPath(path, value.getBytes());
                return createPath;
            } else {
                log.info("该路径已存在");
                return createPath;
            }

        } catch (Exception e) {
            log.error("创建path=" + path + "节点出错{}", e);
            return null;
        }
    }

    /**
     * 更新节点数据
     */
    public Boolean updateZkNode(String path, String value) {
        try {
            Stat stat = zkClient.setData().forPath(path, value.getBytes());
            if (stat == null) {
                //更新失败
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("更新path=" + path + "节点出错{}", e);
            return false;
        }
    }

    /**
     * 获取节点数据
     *
     * @param path
     * @return
     */
    public String getZkNodeData(String path) {
        Stat stat = new Stat(); // Stat就是对znode所有属性的一个映射，stat=null表示节点不存在
        try {
            String re = new String(zkClient.getData()
                    .storingStatIn(stat) // 在获取节点内容的同时把状态信息存入Stat对象，如果不写的话只会读取节点数据
                    .forPath("/search/business/test"));
            return re;
        } catch (Exception e) {
            log.error("获取path=" + path + "节点数据出错{}", e);
            return null;
        }
    }

    /**
     * 删除节点
     */
    public void deleteZkNode(String path) {
        try {
            zkClient.delete().guaranteed()//保障机制，若未删除成功，只要会话有效会在后台一直尝试删除
                    .deletingChildrenIfNeeded()//若存在子节点，子节点也删除
                    .forPath(path);
        } catch (Exception e) {
            log.error("删除path=" + path + "节点出错{}", e);
        }
    }
    /**
     * 获取分布式锁
     */
    public void acquireDistributedLock(String path) {
        String keyPath = "/" + ROOT_PATH_LOCK + "/" + path;
        while (true) {
            try {
                zkClient
                        .create()
                        .creatingParentsIfNeeded()
                        .withMode(CreateMode.EPHEMERAL)
                        .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                        .forPath(keyPath);
                log.info("success to acquire lock for path:{}", keyPath);
                break;
            } catch (Exception e) {
                log.info("failed to acquire lock for path:{}", keyPath);
                log.info("while try again .......");
                try {
                    if (countDownLatch.getCount() <= 0) {
                        countDownLatch = new CountDownLatch(1);
                    }
                    countDownLatch.await();
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    /**
     * 释放分布式锁
     */
    public boolean releaseDistributedLock(String path) {
        try {
            String keyPath = "/" + ROOT_PATH_LOCK + "/" + path;
            if (zkClient.checkExists().forPath(keyPath) != null) {
                zkClient.delete().forPath(keyPath);
            }
        } catch (Exception e) {
            log.error("failed to release lock");
            return false;
        }
        return true;
    }

    /**
     * 创建 watcher 事件
     */
    private void addWatcher(String path) throws Exception {
        String keyPath;
        if (path.equals(ROOT_PATH_LOCK)) {
            keyPath = "/" + path;
        } else {
            keyPath = "/" + ROOT_PATH_LOCK + "/" + path;
        }
        final PathChildrenCache cache = new PathChildrenCache(zkClient, keyPath, false);
        cache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
        cache.getListenable().addListener((client, event) -> {
            if (event.getType().equals(PathChildrenCacheEvent.Type.CHILD_REMOVED)) {
                String oldPath = event.getData().getPath();
                log.info("上一个节点 "+ oldPath + " 已经被断开");
                if (oldPath.contains(path)) {
                    //释放计数器，让当前的请求获取锁
                    countDownLatch.countDown();
                }
            }
        });
    }
    //创建父节点，并创建永久节点
    @Bean
    public void afterPropertiesSet() {
        zkClient = zkClient.usingNamespace("lock-namespace");
        String path = "/" + ROOT_PATH_LOCK;
        try {
            if (zkClient.checkExists().forPath(path) == null) {
                zkClient.create()
                        .creatingParentsIfNeeded()
                        .withMode(CreateMode.PERSISTENT)
                        .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                        .forPath(path);
            }
            addWatcher(ROOT_PATH_LOCK);
            log.info("root path 的 watcher 事件创建成功");
        } catch (Exception e) {
            log.error("connect zookeeper fail，please check the log >> {}", e.getMessage(), e);
        }
    }
}
