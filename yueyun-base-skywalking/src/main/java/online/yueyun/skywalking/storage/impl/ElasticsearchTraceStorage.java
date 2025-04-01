package online.yueyun.skywalking.storage.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import online.yueyun.skywalking.model.TraceData;
import online.yueyun.skywalking.model.TraceQueryCondition;
import online.yueyun.skywalking.storage.TraceStorage;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Elasticsearch 存储实现
 */
@Slf4j
@Component
public class ElasticsearchTraceStorage implements TraceStorage {

    @Autowired
    private RestHighLevelClient elasticsearchClient;
    
    @Autowired
    private ObjectMapper objectMapper;

    private static final String INDEX_PREFIX = "skywalking_trace_";
    private static final String INDEX_TYPE = "_doc";

    @Override
    public void store(TraceData data) {
        try {
            String indexName = INDEX_PREFIX + data.getServiceName().toLowerCase();
            IndexRequest request = new IndexRequest(indexName, INDEX_TYPE, data.getTraceId())
                    .source(convertToMap(data), XContentType.JSON);
            IndexResponse response = elasticsearchClient.index(request, RequestOptions.DEFAULT);
            log.debug("存储链路追踪数据成功: {}", response.getId());
        } catch (IOException e) {
            log.error("存储链路追踪数据失败", e);
            throw new RuntimeException("存储链路追踪数据失败", e);
        }
    }

    @Override
    public void storeBatch(List<TraceData> dataList) {
        try {
            BulkRequest request = new BulkRequest();
            for (TraceData data : dataList) {
                String indexName = INDEX_PREFIX + data.getServiceName().toLowerCase();
                IndexRequest indexRequest = new IndexRequest(indexName, INDEX_TYPE, data.getTraceId())
                        .source(convertToMap(data), XContentType.JSON);
                request.add(indexRequest);
            }
            BulkResponse response = elasticsearchClient.bulk(request, RequestOptions.DEFAULT);
            log.debug("批量存储链路追踪数据成功: {}", response.getItems().length);
        } catch (IOException e) {
            log.error("批量存储链路追踪数据失败", e);
            throw new RuntimeException("批量存储链路追踪数据失败", e);
        }
    }

    @Override
    public TraceData query(String traceId) {
        try {
            SearchRequest request = new SearchRequest(INDEX_PREFIX + "*");
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            sourceBuilder.query(QueryBuilders.termQuery("traceId", traceId));
            request.source(sourceBuilder);
            SearchResponse response = elasticsearchClient.search(request, RequestOptions.DEFAULT);
            if (response.getHits().getTotalHits().value > 0) {
                SearchHit hit = response.getHits().getAt(0);
                return convertToTraceData(hit.getSourceAsMap());
            }
            return null;
        } catch (IOException e) {
            log.error("查询链路追踪数据失败", e);
            throw new RuntimeException("查询链路追踪数据失败", e);
        }
    }

    @Override
    public List<TraceData> queryList(TraceQueryCondition condition) {
        try {
            SearchRequest request = new SearchRequest(INDEX_PREFIX + "*");
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            
            // 构建查询条件
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            if (condition.getServiceName() != null) {
                boolQuery.must(QueryBuilders.termQuery("serviceName", condition.getServiceName()));
            }
            if (condition.getOperationName() != null) {
                boolQuery.must(QueryBuilders.termQuery("operationName", condition.getOperationName()));
            }
            if (condition.getStartTime() != null) {
                boolQuery.must(QueryBuilders.rangeQuery("startTime").gte(condition.getStartTime()));
            }
            if (condition.getEndTime() != null) {
                boolQuery.must(QueryBuilders.rangeQuery("endTime").lte(condition.getEndTime()));
            }
            if (condition.getMinDuration() != null) {
                boolQuery.must(QueryBuilders.rangeQuery("duration").gte(condition.getMinDuration()));
            }
            if (condition.getMaxDuration() != null) {
                boolQuery.must(QueryBuilders.rangeQuery("duration").lte(condition.getMaxDuration()));
            }
            if (condition.getError() != null) {
                boolQuery.must(QueryBuilders.termQuery("error", condition.getError()));
            }
            if (condition.getUserId() != null) {
                boolQuery.must(QueryBuilders.termQuery("userId", condition.getUserId()));
            }
            if (condition.getClassName() != null) {
                boolQuery.must(QueryBuilders.termQuery("className", condition.getClassName()));
            }
            if (condition.getMethodName() != null) {
                boolQuery.must(QueryBuilders.termQuery("methodName", condition.getMethodName()));
            }
            
            sourceBuilder.query(boolQuery);
            sourceBuilder.from((condition.getPageNum() - 1) * condition.getPageSize())
                    .size(condition.getPageSize());
            request.source(sourceBuilder);
            
            SearchResponse response = elasticsearchClient.search(request, RequestOptions.DEFAULT);
            List<TraceData> result = new ArrayList<>();
            for (SearchHit hit : response.getHits().getHits()) {
                result.add(convertToTraceData(hit.getSourceAsMap()));
            }
            return result;
        } catch (IOException e) {
            log.error("查询链路追踪数据列表失败", e);
            throw new RuntimeException("查询链路追踪数据列表失败", e);
        }
    }

    @Override
    public void delete(String traceId) {
        try {
            SearchRequest searchRequest = new SearchRequest(INDEX_PREFIX + "*");
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            sourceBuilder.query(QueryBuilders.termQuery("traceId", traceId));
            searchRequest.source(sourceBuilder);
            SearchResponse response = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT);
            
            if (response.getHits().getTotalHits().value > 0) {
                SearchHit hit = response.getHits().getAt(0);
                DeleteRequest deleteRequest = new DeleteRequest(hit.getIndex(), INDEX_TYPE, hit.getId());
                DeleteResponse deleteResponse = elasticsearchClient.delete(deleteRequest, RequestOptions.DEFAULT);
                log.debug("删除链路追踪数据成功: {}", deleteResponse.getId());
            }
        } catch (IOException e) {
            log.error("删除链路追踪数据失败", e);
            throw new RuntimeException("删除链路追踪数据失败", e);
        }
    }

    @Override
    public void deleteBatch(List<String> traceIds) {
        try {
            BulkRequest request = new BulkRequest();
            for (String traceId : traceIds) {
                SearchRequest searchRequest = new SearchRequest(INDEX_PREFIX + "*");
                SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
                sourceBuilder.query(QueryBuilders.termQuery("traceId", traceId));
                searchRequest.source(sourceBuilder);
                SearchResponse response = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT);
                
                if (response.getHits().getTotalHits().value > 0) {
                    SearchHit hit = response.getHits().getAt(0);
                    DeleteRequest deleteRequest = new DeleteRequest(hit.getIndex(), INDEX_TYPE, hit.getId());
                    request.add(deleteRequest);
                }
            }
            BulkResponse response = elasticsearchClient.bulk(request, RequestOptions.DEFAULT);
            log.debug("批量删除链路追踪数据成功: {}", response.getItems().length);
        } catch (IOException e) {
            log.error("批量删除链路追踪数据失败", e);
            throw new RuntimeException("批量删除链路追踪数据失败", e);
        }
    }

    /**
     * 将 TraceData 转换为 Map
     */
    private Map<String, Object> convertToMap(TraceData data) {
        try {
            return objectMapper.convertValue(data, Map.class);
        } catch (Exception e) {
            log.error("转换 TraceData 到 Map 失败", e);
            throw new RuntimeException("转换 TraceData 到 Map 失败", e);
        }
    }

    /**
     * 将 Map 转换为 TraceData
     */
    private TraceData convertToTraceData(Map<String, Object> map) {
        try {
            return objectMapper.convertValue(map, TraceData.class);
        } catch (Exception e) {
            log.error("转换 Map 到 TraceData 失败", e);
            throw new RuntimeException("转换 Map 到 TraceData 失败", e);
        }
    }
} 