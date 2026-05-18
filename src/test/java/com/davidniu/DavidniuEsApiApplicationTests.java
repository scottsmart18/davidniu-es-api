package com.davidniu;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Result;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.*;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import com.davidniu.pojo.User;
import net.minidev.json.JSONArray;
import org.elasticsearch.client.RequestOptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
class DavidniuEsApiApplicationTests {
    @Autowired
    private ElasticsearchClient elasticsearchClient;

    private final String INDEX_NAME = "davidniu-index";

    @Test
    void contextLoads() {
        // 测试Elasticsearch客户端是否正常注入
        System.out.println("ElasticsearchClient injected successfully: " + (elasticsearchClient != null));
    }

    @Test
    void testCreateIndex() {
        // 测试Elasticsearch客户端是否正常工作
        //System.out.println("ElasticsearchClient is working: " + elasticsearchClient.ping().value());
        try {
            // ping() 返回一个 ObjectResponse，需要调用 .value() 获取布尔值
            boolean isAlive = elasticsearchClient.ping().value();
            System.out.println("ElasticsearchClient is working: " + isAlive);
            CreateIndexRequest createIndexRequest = new CreateIndexRequest.Builder()
                    .index("davidniu-index")
                    .build();
            CreateIndexResponse createIndexResponse = elasticsearchClient.indices().create(createIndexRequest);
            System.out.println("Index created: " + createIndexResponse.acknowledged());
        } catch (Exception e) {
            System.err.println("Elasticsearch connection failed: " + e.getMessage());
        }

    }
    //测试获取索引
    @Test
    void testGetIndex() throws Exception {
        ExistsRequest existsRequest = new ExistsRequest.Builder()
                .index("davidniu-index")
                .build();
        boolean indexExists = elasticsearchClient.indices().exists(existsRequest).value();
        // 测试获取索引
        System.out.println("Index exists: " + indexExists);
    }
    //测试删除索引
    @Test
    void testDeleteIndex() throws Exception {
        String indexName = "davidniu-index";

        // 1. 先检查索引是否存在
        boolean exists = elasticsearchClient.indices()
                .exists(req -> req.index(indexName))
                .value();

        if (exists) {
            // 2. 存在则删除
            DeleteIndexResponse deleteResponse = elasticsearchClient.indices()
                    .delete(b -> b.index(indexName));

            System.out.println("Index deleted: " + deleteResponse.acknowledged());
        } else {
            System.out.println("Index '" + indexName + "' does not exist, nothing to delete");
        }
    }
    //测试添加文档
    @Test
    void testAddDocument() throws Exception {
        User user = new User("David 牛", 18);

        // 使用新版 API 添加文档
        IndexResponse response = elasticsearchClient.index(i -> i
                .index("davidniu-index")
                .id("1")  // id 是字符串类型
                .document(user)  // 直接传入对象，客户端会自动序列化
        );

        // 使用无前缀方法
        System.out.println("Document added: " + response.result());
        System.out.println("Document ID: " + response.id());
        System.out.println("Index: " + response.index());

        // result() 返回 Result 枚举，可以直接 toString()
        if (response.result() == Result.Created) {
            System.out.println("✓ New document created");
        } else if (response.result() == Result.Updated) {
            System.out.println("✓ Existing document updated");
        }
        }

    // 2. 获取文档
    @Test
    void testGetDocument() throws Exception {
        GetResponse<User> response = elasticsearchClient.get(g -> g
                        .index(INDEX_NAME)
                        .id("1"),
                User.class
        );

        if (response.found()) {
            User user = response.source();
            System.out.println("Found user: " + user.getName() + ", " + user.getAge());
        } else {
            System.out.println("Document not found");
        }
    }

    // 3. 更新文档
    @Test
    void testUpdateDocument() throws Exception {
        User user = new User("David Niu1", 19);

        UpdateResponse<User> response = elasticsearchClient.update(u -> u
                        .index(INDEX_NAME)
                        .id("1")
                        .doc(user)
                        .upsert(user),
                User.class
        );

        System.out.println("Document updated: " + response.result());
    }

    // 4. 删除文档
    @Test
    void testDeleteDocument() throws Exception {
        DeleteResponse response = elasticsearchClient.delete(d -> d
                .index(INDEX_NAME)
                .id("1")
        );

        System.out.println("Document deleted: " + response.result());
    }

    // 5. 搜索文档
    @Test
    void testSearchDocument() throws Exception {
        SearchResponse<User> response = elasticsearchClient.search(s -> s
                        .index(INDEX_NAME)
                        .query(q -> q
                                .match(m -> m
                                        .field("name")
                                        .query("David")
                                )
                        ),
                User.class
        );

        List<Hit<User>> hits = response.hits().hits();
        for (Hit<User> hit : hits) {
            User user = hit.source();
            System.out.println("Found: " + user.getName() + ", Score: " + hit.score());
        }
    }

    // 6. 批量添加文档
    @Test
    void testBulkAddDocuments() throws Exception {
        List<User> users = List.of(
                new User("Alice", 25),
                new User("Bob", 30),
                new User("Charlie", 35),
                new User("David牛力", 18)
        );

        BulkResponse response = elasticsearchClient.bulk(b -> {
            for (int i = 0; i < users.size(); i++) {
                final int index = i;  // 创建 effectively final 的临时变量
                b.operations(op -> op
                        .index(idx -> idx
                                .index(INDEX_NAME)
                                .id(String.valueOf(index + 2))
                                .document(users.get(index))
                        )
                );
            }
            return b;
        });

        System.out.println("Bulk operation errors: " + (response.errors() ? "yes" : "no"));
        if (!response.errors()) {
            System.out.println("All documents added successfully");
        }
    }

}
