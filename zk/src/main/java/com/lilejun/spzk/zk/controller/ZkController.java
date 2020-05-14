package com.lilejun.spzk.zk.controller;

import com.lilejun.spzk.zk.zkutil.ZookeeperUtil;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/zkController")
public class ZkController {

    @Autowired
    private ZookeeperUtil zkUtil;


    @PostMapping("/queryZkPath")
    public List<String> queryZkPath(String path){
        List<String> statList = zkUtil.getZKNodeForPath(path);
        return statList;
    }

    @PostMapping("/createZkPersistentPath")
    public String createZkPath(String path,String value){
        String node = zkUtil.createZkNode(path,value,CreateMode.PERSISTENT);
        return node;
    }
    @PostMapping("/createzkEphemeralpath")
    public String createzkEphemeralpath(String path,String value){
        String node = zkUtil.createZkNode(path,value,CreateMode.EPHEMERAL);
        return node;
    }
    /**
     * 监听
     */
    @PostMapping("/updatePathValue")
    public String updatePathValue(String path,String value){
        zkUtil.updateZkNode(path,value);
        return "更新成功";
    }
}
