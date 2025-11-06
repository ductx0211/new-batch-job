package com.yourcompany.batch.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.stereotype.Component;
import com.yourcompany.batch.batch.step.AbstractStepBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Sample Step Builder để demo cách sử dụng AbstractStepBuilder
 * Step này đọc danh sách số, xử lý (nhân 2) và ghi log
 */
@Component
public class SampleStepBuilder extends AbstractStepBuilder<Integer, Integer> {

    private static final Logger log = LoggerFactory.getLogger(SampleStepBuilder.class);

    @Override
    protected String getStepName() {
        return "SAMPLE_STEP";
    }

    @Override
    protected int countTotalItems() {
        // Tạo danh sách 10 số từ 1 đến 10
        return 10;
    }

    @Override
    protected String getLogMessageAtStart() {
        return "Starting sample step - processing numbers from 1 to 10";
    }

    @Override
    protected ItemReader<Integer> reader() {
        // Tạo danh sách số từ 1 đến 10
        List<Integer> numbers = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            numbers.add(i);
        }
        return new ListItemReader<>(numbers);
    }

    @Override
    protected ItemProcessor<Integer, Integer> processor() {
        return item -> {
            // Xử lý: nhân số với 2
            int result = item * 2;
            log.debug("Processing: {} -> {}", item, result);
            return result;
        };
    }

    @Override
    protected ItemWriter<Integer> writer() {
        return items -> {
            // Ghi kết quả (trong thực tế có thể ghi vào database, file, etc.)
            for (Integer item : items) {
                log.info("Writing result: {}", item);
            }
            log.info("Written {} items", items.size());
        };
    }

    @Override
    protected void beforeStep(org.springframework.batch.core.StepExecution stepExecution) {
        super.beforeStep(stepExecution);
        addJobLog(com.yourcompany.batch.domain.enumeration.LogTypeEnum.INFO, 
            "Sample step started", "Will process 10 numbers");
    }

    @Override
    protected void afterStep(org.springframework.batch.core.StepExecution stepExecution) {
        super.afterStep(stepExecution);
        addJobLog(com.yourcompany.batch.domain.enumeration.LogTypeEnum.INFO, 
            "Sample step completed", "Processed " + getNumberOfProcessed() + " items");
    }
}

