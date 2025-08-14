package com.quant.altdata.processing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Properties;

/**
 * 数据处理器工厂 - 创建适合不同场景的数据处理器
 */
public class DataProcessorFactory {
    private static final Logger logger = LoggerFactory.getLogger(DataProcessorFactory.class);

    /**
     * 创建数据处理器
     * @param config 配置属性
     * @return 数据处理器实例
     */
    public static DataProcessor createProcessor(Properties config) {
        String processorType = config.getProperty("processor.type", "parallel");
        int numThreads = Integer.parseInt(config.getProperty("processor.threads", "" + Runtime.getRuntime().availableProcessors() + ""));
        int batchSize = Integer.parseInt(config.getProperty("processor.batchSize", "1000"));
        int threshold = Integer.parseInt(config.getProperty("processor.threshold", "500"));

        logger.info("Creating {} data processor with {} threads", processorType, numThreads);

        DataProcessor singleThreadProcessor = new SimpleDataProcessor();

        switch (processorType.toLowerCase()) {
            case "parallel":
                return new ParallelDataProcessor(numThreads, batchSize, singleThreadProcessor);
            case "optimized":
                return new OptimizedParallelProcessor(numThreads, threshold, singleThreadProcessor);
            case "single":
                return singleThreadProcessor;
            default:
                logger.warn("Unknown processor type: {}. Using parallel processor.", processorType);
                return new ParallelDataProcessor(numThreads, batchSize, singleThreadProcessor);
        }
    }
}