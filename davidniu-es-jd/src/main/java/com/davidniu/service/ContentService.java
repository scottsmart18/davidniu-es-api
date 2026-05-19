package com.davidniu.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Time;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.Highlight;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.util.NamedValue;
import com.davidniu.pojo.Content;
import com.davidniu.utils.HtmlParseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ContentService {

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Autowired
    private HtmlParseUtil htmlParseUtil;


    //解析数据, 放入ES索引�?
    public Boolean parseContent(String keywords) throws IOException {
        List<Content> contents = htmlParseUtil.parseJD(keywords);
        if (contents == null || contents.isEmpty()) {
            return false;
        }
        
        //把查询的数据放入ES�?
        BulkRequest.Builder bulkBuilder = new BulkRequest.Builder();
        bulkBuilder.timeout(Time.of(t -> t.time("2m")));

        for (Content content : contents) {
            bulkBuilder.operations(op -> op
                .index(idx -> idx
                    .index("jd_goods")
                    .document(content)
                )
            );
        }

        BulkResponse bulkResponse = elasticsearchClient.bulk(bulkBuilder.build());
        return !bulkResponse.errors();
    }

    //实现搜索高亮
    public List<Map<String, Object>> searchPage(String keyword, int pageNo, int pageSize) throws IOException {
        if (pageNo <= 1) {
            pageNo = 1;
        }
        
        // 计算分页起始位置
        int from = (pageNo - 1) * pageSize;

        // 构建高亮配置
        HighlightField highlightField = HighlightField.of(hf -> hf
                .preTags("<span style='color:red'>")
                .postTags("</span>")
        );

        Highlight highlight = Highlight.of(h -> h
                .fields(NamedValue.of("title", highlightField))
                .requireFieldMatch(false)
        );
        
        // 执行搜索
        SearchResponse<JsonData> response = elasticsearchClient.search(s -> s
            .index("jd_goods")
            .query(q -> q
                .match(m -> m
                    .field("title")
                    .query(keyword)
                )
            )
            .highlight(highlight)
            .from(from)
            .size(pageSize)
        , JsonData.class);
        
        // 解析结果
        List<Map<String, Object>> list = new ArrayList<>();
        for (Hit<JsonData> hit : response.hits().hits()) {
            Map<String, Object> sourceMap = new HashMap<>();
            
            // 解析原始数据
            if (hit.source() != null) {
                sourceMap = hit.source().to(Map.class);
            }
            
            // 解析高亮字段
            Map<String, List<String>> highlightFields = hit.highlight();
            if (highlightFields != null && highlightFields.containsKey("title")) {
                List<String> highlightTitleList = highlightFields.get("title");
                if (highlightTitleList != null && !highlightTitleList.isEmpty()) {
                    String highlightTitle = String.join("", highlightTitleList);
                    sourceMap.put("title", highlightTitle);
                }
            }
            
            list.add(sourceMap);
        }
        
        return list;
    }


}

