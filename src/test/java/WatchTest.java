import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class WatchTest {
    private CuratorFramework client;

    @Before
    public void testConnect() {
        ExponentialBackoffRetry retry = new ExponentialBackoffRetry(3000, 10);

        String connectString = "192.168.10.102:2181,192.168.10.103:2181,192.168.10.104:2181";


        client = CuratorFrameworkFactory.builder().connectString(connectString)
                .sessionTimeoutMs(60 * 1000)  // 会话超时时间
                .connectionTimeoutMs(15 * 1000) // 连接超时时间
                .namespace("gzhu")
                .retryPolicy(retry).build(); // 重试策略

        client.start();
    }

    // 1.监听某一个具体的节点
    @Test
    public void testNodeCache() throws Exception {
        // ①.创建NodeCache
       NodeCache nodeCache = new NodeCache(client,"/zhang");
       // ②.注册监听
       nodeCache.getListenable().addListener(new NodeCacheListener() {
           @Override
           public void nodeChanged() throws Exception {
               System.out.println("节点变了");

               byte[] bytes = nodeCache.getCurrentData().getData();
               System.out.println(new String(bytes));
           }
       });
       // ③.开启监听
       nodeCache.start();
       while (true){

       }
    }

    // 2.监听某一个节点的子节点（该节点本身不会被监听）
    @Test
    public void parentchildCache() throws Exception {
        // ①.创建PathChildrenCache对象
        PathChildrenCache cache = new PathChildrenCache(client, "/zhang",true);
        // ②.注册监听
        cache.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent event) throws Exception {
                System.out.println("子节点变化了");
                System.out.println(event);
                // 获取变的类型
                PathChildrenCacheEvent.Type type = event.getType();
                // 如果是update，则输出改变后的值
                if(type.equals(PathChildrenCacheEvent.Type.CHILD_UPDATED)){
                    System.out.println("修改了子节点");
                    byte[] bytes = event.getData().getData();
                    System.out.println(new String(bytes));
                }
            }
        });

        // ③.开启监听
        cache.start();

        while(true){

        }
    }

    // 3.TreeNode  监听自己和孩子
    @Test
    public void treeTest() throws Exception {
        TreeCache treeCache = new TreeCache(client,"/zhang");

        treeCache.getListenable().addListener(new TreeCacheListener() {
            @Override
            public void childEvent(CuratorFramework curatorFramework, TreeCacheEvent treeCacheEvent) throws Exception {
                System.out.println("节点变化了");
                TreeCacheEvent.Type type = treeCacheEvent.getType();
                System.out.println(treeCacheEvent);

                if(type.equals(TreeCacheEvent.Type.NODE_UPDATED)){
                    System.out.println("节点变了");
                    System.out.println(treeCacheEvent);
                }
            }
        });

        // ③.开启监听
        treeCache.start();

        while (true){

        }
    }



    @After
    public void close() {
        if (client != null) {
            client.close();
        }
    }


}
