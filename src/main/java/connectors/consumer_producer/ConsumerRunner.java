package connectors.consumer_producer;

public class ConsumerRunner {
    public static void main(String[] args) throws InterruptedException {
        runConsumer();
    }

    private static void runConsumer() throws InterruptedException {
        TrainDataConsumer.runConsumer();
    }
}
