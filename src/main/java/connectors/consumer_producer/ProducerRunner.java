package connectors.consumer_producer;

public class ProducerRunner {

    public static void main(String[] args) throws Exception {
        runProducer();
    }

    private static void runProducer() throws Exception {
        TrainDataProducer.runProducer(5);
    }
}
