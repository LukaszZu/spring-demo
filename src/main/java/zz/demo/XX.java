package zz.demo;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Created by zulk on 24.02.17.
 */
public class XX {
    public static void main(String[] args) {
        Stream<String> aaaaa = StreamSupport.stream(new Spliterator<String>() {
            int c = 0;
            @Override
            public boolean tryAdvance(Consumer<? super String> consumer) {
                c++;
                if(c<100000000) {
                    consumer.accept("AAAAA");
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public Spliterator<String> trySplit() {
                return null;
            }

            @Override
            public long estimateSize() {
                return 0;
            }

            @Override
            public int characteristics() {
                return ORDERED;
            }
        }, false);

        aaaaa.iterator().forEachRemaining(System.out::println);

        JdbcTemplate jdbcTemplate = new JdbcTemplate();
//        jdbcTemplate.query("", );
    }
}
