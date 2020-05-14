package com.lilejun.spzk.zk.zkconfig;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * zk初始化
 */
@Configuration
public class ZookeeperConfig {

    /**
     * zk服务器地址
     */
    @Value("${zookeeper.zkServer}")
    private String zkServer;

    /**
     * zk的会话超时时间
     */
    @Value("${zookeeper.zkSessionTimeoutMs}")
    private int zksessionTimeoutMs;

    /**
     * zk连接超时时间
     */
    @Value("${zookeeper.zkConnectionTimeoutMs}")
    private int zkConnectionTimeoutMs;

    /**
     * zk重试次数
     */
    @Value("${zookeeper.zkMaxRetries}")
    private int zkMaxRetries;
    /**
     * zk初始睡眠时间
     */
    @Value("${zookeeper.zkBaseSleepTimeMs}")
    private int zkBaseSleepTimeMs;


    /**
     * 初始化zk:以 builder 模式创建，Fluent 风格，建议使用这种
     * @return
     */
    @Bean
    public CuratorFramework getCuratorFramework(){
        CuratorFramework client = CuratorFrameworkFactory.builder().connectString(zkServer).sessionTimeoutMs(zksessionTimeoutMs)
                .connectionTimeoutMs(zkConnectionTimeoutMs).retryPolicy(new ExponentialBackoffRetry(zkBaseSleepTimeMs,zkMaxRetries))
                .build();
        client.start();
        return client;
    }

    public String getZkServer() {
        return zkServer;
    }

    public void setZkServer(String zkServer) {
        this.zkServer = zkServer;
    }

    public int getZksessionTimeoutMs() {
        return zksessionTimeoutMs;
    }

    public void setZksessionTimeoutMs(int zksessionTimeoutMs) {
        this.zksessionTimeoutMs = zksessionTimeoutMs;
    }

    public int getZkConnectionTimeoutMs() {
        return zkConnectionTimeoutMs;
    }

    public void setZkConnectionTimeoutMs(int zkConnectionTimeoutMs) {
        this.zkConnectionTimeoutMs = zkConnectionTimeoutMs;
    }

    public int getZkMaxRetries() {
        return zkMaxRetries;
    }

    public void setZkMaxRetries(int zkMaxRetries) {
        this.zkMaxRetries = zkMaxRetries;
    }

    public int getZkBaseSleepTimeMs() {
        return zkBaseSleepTimeMs;
    }

    public void setZkBaseSleepTimeMs(int zkBaseSleepTimeMs) {
        this.zkBaseSleepTimeMs = zkBaseSleepTimeMs;
    }
}
