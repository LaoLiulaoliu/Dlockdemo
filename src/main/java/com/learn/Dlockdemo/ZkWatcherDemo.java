package com.learn.Dlockdemo;

import org.apache.zookeeper.*;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;

/**
 * @author zhangchaoyang
 * @description Zookeeper Watcher演示类
 * @date 2014-6-22
 */
public class ZkWatcherDemo {
    private static ZooKeeper zkp = null;
    private static final int TIMEOUT = 6000;
    private static final String conStr = "192.168.1.107:2181,192.168.1.113:2181,192.168.1.114:2181";


    private static Watcher getWatcher(final String msg) {
        return new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                System.out.println(msg + "上的监听被触发\t事件类型" + event.getType()
                        + "\t发生变化的节点" + event.getPath());
            }
        };
    }

    public static void main(String[] args) throws IOException, KeeperException,
            InterruptedException {
        System.out.println("--------------1----------------");
        //CONNECT上的监听被触发	事件类型None	发生变化的节点null
        zkp = new ZooKeeper(conStr, TIMEOUT, getWatcher("CONNECT"));
        Thread.sleep(1000);

        System.out.println("--------------2----------------");
        zkp.create("/znodename",
                "znodedata".getBytes(),
                Ids.OPEN_ACL_UNSAFE,
                CreateMode.PERSISTENT);
        zkp.create("/znodename/childnode",
                new byte[0],
                Ids.OPEN_ACL_UNSAFE,
                CreateMode.PERSISTENT);

        Stat stat = zkp.exists("/znodename", getWatcher("EXISTS"));
        zkp.getChildren("/", getWatcher("GETCHILDREN"));
        zkp.getData("/znodename", getWatcher("GETDATA"), stat);

        stat = zkp.exists("/znodename/childnode", getWatcher("EXISTS"));
        zkp.getChildren("/znodename", getWatcher("GETCHILDREN"));
        zkp.getData("/znodename/childnode", getWatcher("GETDATA"), stat);

        Thread.sleep(20000);

        System.out.println("--------------3----------------");
        zkp.delete("/znodename/childnode", -1);
        zkp.delete("/znodename", -1);
        zkp.close();
    }
}

/* Output:
--------------1----------------
log4j:WARN No appenders could be found for logger (org.apache.zookeeper.ZooKeeper).
log4j:WARN Please initialize the log4j system properly.
log4j:WARN See http://logging.apache.org/log4j/1.2/faq.html#noconfig for more info.
CONNECT上的监听被触发	事件类型None	发生变化的节点null
--------------2----------------
--------------3----------------
GETDATA上的监听被触发	事件类型NodeDeleted	发生变化的节点/znodename/childnode
EXISTS上的监听被触发	事件类型NodeDeleted	发生变化的节点/znodename/childnode
GETCHILDREN上的监听被触发	事件类型NodeChildrenChanged	发生变化的节点/znodename
GETDATA上的监听被触发	事件类型NodeDeleted	发生变化的节点/znodename
EXISTS上的监听被触发	事件类型NodeDeleted	发生变化的节点/znodename
GETCHILDREN上的监听被触发	事件类型NodeChildrenChanged	发生变化的节点/
 */