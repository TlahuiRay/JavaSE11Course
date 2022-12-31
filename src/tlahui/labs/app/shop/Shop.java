package tlahui.labs.app.shop;

import tlahui.labs.app.data.Product;
import tlahui.labs.app.data.ProductManagement;
import tlahui.labs.app.data.Rating;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Shop {
    private static final Logger logger = Logger.getLogger(ProductManagement.class.getName());

    public static void main(String[] args) {
        ProductManagement pm = ProductManagement.getInstance();
        AtomicInteger clientCount =  new AtomicInteger();

        Callable<String> client = () -> {
            String clientId = "Client " + clientCount.incrementAndGet();
            String threadName = Thread.currentThread().getName();
            int productId = ThreadLocalRandom.current().nextInt(6) + 101;
            String languageTag =
                            ProductManagement.getSupportedLocales()
                                    .stream()
                                    .skip(ThreadLocalRandom.current().nextInt(6))
                                    .findFirst()
                                    .get();
            StringBuilder log = new StringBuilder();
            log.append(clientId + " " + threadName + "\n\tstart of the log\t-\n");
            log.append(
                    pm.getDiscounts(languageTag)
                            .entrySet()
                            .stream()
                            .map(entry -> entry.getKey()+"\t"+entry.getValue())
                            .collect(Collectors.joining("\n")));
            Product prod = pm.reviewProd(productId, Rating.FOUR_START, "Yet another Review");
            log.append((prod != null) ? "\nProduct " + productId + "reviewed\n" : "\nProduct " + productId + "not reviewed");
            pm.printProductReport(productId, languageTag, clientId);
            log.append(clientId + " generated report for " + productId +productId + " product");
            log.append("\n-\tend of the log\n-\t");
            return log.toString();
        };

        List<Callable<String>> clients = Stream.generate(() -> client)
                .limit(5)
                .collect(Collectors.toList());

        ExecutorService executor = Executors.newFixedThreadPool(3);
        try {
            List<Future<String>> results = executor.invokeAll(clients);
            executor.shutdown();
            results.stream().forEach(
                    result -> {
                        try {
                            System.out.println(result.get());
                        } catch (InterruptedException | ExecutionException e) {
                            logger.log(Level.SEVERE, "Error retrieving client logs", e.getMessage());
                        }
                    });
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "Error invoking clients", e.getMessage());
        }



/*        pm.printProductReport(101,LANG);
        pm.printProductReport(102,LANG);
        pm.printProductReport(103,LANG);

        pm.dumpData();
        pm.restoreData();

        pm.printProductReport(104,LANG);
        pm.printProductReport(105,LANG);
        pm.printProductReport(106,LANG);

        pm.printProducts(p -> p.getPrice().floatValue() < 2,
                (pr1, pr2) -> pr2.getRating().ordinal() - pr1.getRating().ordinal(),
				LANG);

        pm.getDiscounts(LANG)
                .forEach((rating, discount) -> System.out.println(rating + "\t" + discount));*/
    }

}