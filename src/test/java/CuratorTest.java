import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class CuratorTest {
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

    // 增加节点
    @Test
    public void testCrate() throws Exception {
        // 1.基本创建，节点值也输入，默认类型持久型
        client.create().forPath("/song", "songData".getBytes(StandardCharsets.UTF_8));
        // 2.类型为持久带序号的
        client.create().withMode(CreateMode.PERSISTENT_SEQUENTIAL).forPath("/kun", "kunData".getBytes(StandardCharsets.UTF_8));
        // 3.创建多级节点 creatingParentContainersIfNeeded
        client.create().creatingParentContainersIfNeeded().forPath("/zhang/liu", "liuData".getBytes(StandardCharsets.UTF_8));
    }


    @Test // 查节点
    public void getTese() throws Exception {
        // 1.查看节点上的数据  songData
        byte[] bytes = client.getData().forPath("/song");
        System.out.println(new String(bytes));
        // 2.查看子节点 [song, zhang, kun0000000001]
        List<String> list = client.getChildren().forPath("/");
        System.out.println(list);
        // 3.节点状态
        Stat stat = new Stat();
        byte[] bytes1 = client.getData().storingStatIn(stat).forPath("/song");
        System.out.println(stat);

    }

    @Test // 改
    public void setTest() throws Exception {
        Stat stat = new Stat();
        byte[] bytes1 = client.getData().storingStatIn(stat).forPath("/song");
        int version = stat.getVersion();
        client.setData().withVersion(version).forPath("/song", "songUpdate".getBytes(StandardCharsets.UTF_8));
    }

    @Test // 删除
    public void deleteTest() throws Exception {
        // 1.删除单个节点
        client.delete().forPath("/song");
        // 2.删除多级节点
        client.delete().deletingChildrenIfNeeded().forPath("/zhang/liu");
        // 3.回调，删除后执行的函数
        client.delete().inBackground(new BackgroundCallback() {
            @Override
            public void processResult(CuratorFramework curatorFramework, CuratorEvent curatorEvent) throws Exception {
                System.out.println("我被删除了");
                System.out.println(curatorEvent);
            }
        }).forPath("/kun0000000001");
    }


    @After
    public void close() {
        if (client != null) {
            client.close();
        }
    }
}
