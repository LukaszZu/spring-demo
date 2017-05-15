package zz.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Created by zulk on 17.02.17.
 */
@RestController
public class SimpleControler {

    private static Logger log = LoggerFactory.getLogger(SimpleControler.class);


    @Autowired
    RestTemplateBuilder rt;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    XlsService xlsService;

    @GetMapping("/go")
    public ResponseEntity<String> go() {
        log.info("GOOO");
        return ResponseEntity.ok("Juhu");
    }

    public void a() {
    }


    @GetMapping("/exec")
    public String exec() {

        log.info("exec");
        return rt.build().getForObject("http://localhost:8080/go", String.class);
    }

    @GetMapping("/stream")
    public ResponseEntity<StreamingResponseBody> testStream() {

        StreamingResponseBody bb = outputStream -> {
            xlsService.sheetGenerate(outputStream);
        };

        ResponseEntity<StreamingResponseBody> body = ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=a.xlsx")
                .body(bb);

        return body;
    }

    @Service
    class XlsService {

        public void sheetGenerate(OutputStream outputStream) throws IOException {
            SXSSFWorkbook sheets = new SXSSFWorkbook(100);
            SXSSFSheet sheet = sheets.createSheet("llll");
            ScrolableElasticResultSet resultSet = new ScrolableElasticResultSet(null);
            AtomicInteger rowNum = new AtomicInteger(1);
            for (SearchHit[] o : resultSet) {
                for (SearchHit  s:o) {
                    AtomicInteger c = new AtomicInteger(0);
                    SXSSFRow row = sheet.createRow(rowNum.getAndIncrement());
                    s.sourceAsMap().entrySet().stream().forEach(e -> {
                        row.createCell(c.getAndIncrement(), CellType.STRING).setCellValue(String.valueOf(e.getValue()));
                    });
                }
                System.out.println(o.length);
            }
            sheets.write(outputStream);
            sheets.dispose();
        }

    }

    class ScrolableElasticResultSet implements Iterable<SearchHit[]> {

        private QueryBuilder query;
        private SearchResponse shakespeare;
        private PreBuiltTransportClient transportClient;

        public ScrolableElasticResultSet(QueryBuilder query) {
            this.query = query;
        }

        @Override
        public Iterator iterator() {
            try {
                queryBuild();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return new Iterator() {
                @Override
                public boolean hasNext() {
                    return shakespeare.getHits().getHits().length != 0;
                }

                @Override
                public SearchHit[] next() {
                    try {
                        shakespeare = transportClient.prepareSearchScroll(shakespeare.getScrollId()).setScroll(new TimeValue(60000)).execute().get();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    return shakespeare.getHits().getHits();
                }
            };
        }

        @Override
        public void forEach(Consumer consumer) {

        }

        @Override
        public Spliterator spliterator() {
            throw new UnsupportedOperationException();
        }

        private void queryBuild() throws UnknownHostException, ExecutionException, InterruptedException {
            Settings settings = Settings.builder()
                    .put("cluster.name", "zz-cluster")
                    .build();
            transportClient = new PreBuiltTransportClient(settings);
            transportClient.addTransportAddress(
                    new InetSocketTransportAddress(
                            InetAddress.getByName("192.168.100.105"), 9300));

            transportClient.listedNodes().forEach(System.out::println);
            QueryStringQueryBuilder q1 = QueryBuilders.queryStringQuery("*");
            ConstantScoreQueryBuilder q2 = QueryBuilders.constantScoreQuery(q1);
            shakespeare = transportClient.prepareSearch("shakespeare")
                    .setScroll(new TimeValue(6000))
                    .setQuery(q2).setSize(1000).get();

            long l = shakespeare.getHits().totalHits();
            System.out.println(l);
        }

    }
}
