package com.learn.Dlockdemo;

import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class DLock implements Watcher {
    private ZooKeeper zk;
    String root = "/bits";
    CountDownLatch latch;

    private String path;
    private String currentNode;
    private String waitNode;

    public DLock(String host, String path) {
        this.path = path;

        try {
            zk = new ZooKeeper(host, 2000, this);//10 threads, need 10 times of sessionTimeout
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Stat stat = zk.exists(root, false);
            if (stat == null) {
                // when create root Znode,
                // other thread may create Znode before this thread,
                // Exception raise
                zk.create(root, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT); //parent node
            }
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String lock() {
        try {
            currentNode = zk.create(root+"/"+path, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

            while (true) {

                List<String> lockObjNodes = zk.getChildren(root, false);
                Collections.sort(lockObjNodes);

                //排序取到最小节点，则拿到锁。
                if (currentNode.equals(lockObjNodes.get(0))) {
                    return currentNode;
                } else {
                    // lock_0001
                    String childZnode = currentNode.substring(currentNode.lastIndexOf("/")+1);
                    int num = Collections.binarySearch(lockObjNodes, childZnode);
                    if (num == 0) { //表示上两条语句执行的时间，前面的节点们已经被删除释放
                        continue;
                    }
                    waitNode = lockObjNodes.get(num - 1);
                    //监视前一个子节点，5s内被释放删除时(true)，则return表示获取到锁；
                    //若超时(false)，则循环等待。
                    Stat stat = zk.exists(root + "/" + waitNode, true);
                    if (stat == null) {
                        latch = new CountDownLatch(1);
                        boolean ret = latch.await(5000, TimeUnit.MILLISECONDS);
                        if (ret == true) {
                            return currentNode;
                        }
                    }
                }
            }
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void unlock() {
        try {
            zk.delete(currentNode, -1);
            currentNode = null;
            zk.close();
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        if (latch != null) {
            latch.countDown();
        }
//        if (watchedEvent.getType() == null || "".equals(watchedEvent.getType())) {
//            return;
//        }
//        System.out.println("已经触发了" + watchedEvent.getType() + "事件！");
    }
}
