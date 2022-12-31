package tlahui.labs.app.data;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.logging.Logger;

public class ProductManagement {
    private Map<Product, List<Review>> products = new HashMap<>();
    private static final ProductManagement pm = new ProductManagement();
    private final ResourceBundle config = ResourceBundle.getBundle("tlahui.labs.app.data.config");
    private final MessageFormat reviewFormat = new MessageFormat(config.getString("review.data.format"));
    private final MessageFormat productFormat = new MessageFormat(config.getString("product.data.format"));
    private static final Logger logger = Logger.getLogger(ProductManagement.class.getName());
    private final Path reportsFolder = Path.of(config.getString("reports.folder"));
    private final Path dataFolder = Path.of(config.getString("data.folder"));
    private final Path tempFolder = Path.of(config.getString("temp.folder"));

    private static final Map<String, ResourceFormatter> formatters =
            Map.of(
                    "en-GB", new ResourceFormatter(Locale.UK),
                    "en-US", new ResourceFormatter(Locale.US),
                    "fr-FR", new ResourceFormatter(Locale.FRANCE),
                    "ru-RU", new ResourceFormatter(new Locale("ru", "RU")),
                    "zh-CN", new ResourceFormatter(Locale.CHINA),
                    "es-MX", new ResourceFormatter(new Locale("es", "MX"))
            );
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock writeLock = lock.writeLock();
    private final Lock readLock = lock.readLock();

    private ProductManagement() {
        loadAllData();
    }

    public static Set<String> getSupportedLocales() {
        return formatters.keySet();
    }

    public static ProductManagement getInstance() {
        return pm;
    }

    public Product createProd(int id, String name, BigDecimal price, Rating rating, LocalDate bestBefore) {
        Product prod = null;
        try{
            writeLock.lock();
            prod = new Food(id, name, price, rating, bestBefore);
            products.putIfAbsent(prod, new ArrayList<Review>());
        } catch (Exception e){
            logger.log(Level.INFO,"Error Adding Food Product " + e.getMessage());
        } finally {
            writeLock.unlock();
        }
        return prod;
    }

    public Product createProd(int id, String name, BigDecimal price, Rating rating) {
        Product prod = null;
        try{
            writeLock.lock();
            prod = new Drink(id, name, price, rating);
            products.putIfAbsent(prod, new ArrayList<Review>());
        } catch (Exception e){
            logger.log(Level.INFO,"Error Adding Drink Product " + e.getMessage());
        } finally {
            writeLock.unlock();
        }
        return prod;
    }

    public Product findProduct(int id) throws ProductManagerException {
        try{
            readLock.lock();
            return products.keySet()
                    .stream()
                    .filter(p -> p.getId() == id)
                    .findFirst()
                    .orElseThrow(() -> new ProductManagerException("Product with ID: " + id + " not found"));
            //.get();
            //.orElseGet(() -> null);
        }  finally {
            readLock.unlock();
        }
    }

    public Product reviewProd(int id, Rating rating, String comments) {
        try {
            writeLock.lock();
            return reviewProd(findProduct(id), rating, comments);
        } catch (ProductManagerException e) {
            //throw new RuntimeException(e);
            logger.log(Level.INFO,e.getMessage());
        } finally {
            writeLock.unlock();
        }
        return null;
    }


    private Product reviewProd(Product prod, Rating rating, String comments) {

        List<Review> reviews = products.get(prod);
        products.remove(prod);
        reviews.add(new Review(rating, comments));

        prod = prod.applyRating(
                Rateable.convert(
                        (int) Math.round(
                                reviews.stream()
                                        .mapToInt(r -> r.getRating().ordinal())
                                        .average()
                                        .orElse(0)
                        )
                )
        );
        products.put(prod, reviews);
        return prod;
    }

    public void printProductReport(int id, String languageTag, String client) {
        try {
            readLock.lock();
            printProductReport(findProduct(id), languageTag, client);
        } catch (ProductManagerException e) {
            //throw new RuntimeException(e);
            logger.log(Level.INFO,e.getMessage());
        } catch (IOException e) {
            logger.log(Level.SEVERE,"Error printing product report "+ e.getMessage(), e);
        } finally {
            readLock.unlock();
        }
    }

    public void printProductReport(Comparator<Product> sorter, String languageTag) {
        ResourceFormatter formatter = formatters.getOrDefault(languageTag, formatters.get("en-GB"));
        List<Product> productList = new ArrayList<>(products.keySet());
        productList.sort(sorter);

        StringBuilder txt = new StringBuilder();
        for (Product product : productList) {
            txt.append(formatter.formatProduct(product));
            txt.append('\n');
        }
        System.out.println(txt);
    }

    private void printProductReport(Product prod, String languageTag, String client) throws IOException {
        ResourceFormatter formatter = formatters.getOrDefault(languageTag, formatters.get("en-GB"));
        List<Review> reviews = products.get(prod);
        Collections.sort(reviews);
        Path productFile = reportsFolder.resolve(
                MessageFormat.format(
                        config.getString("report.file"), prod.getId(), client));
        System.out.println("");
        try (PrintWriter out = new PrintWriter(
                new OutputStreamWriter(
                        Files.newOutputStream(productFile, StandardOpenOption.CREATE), "UTF-8")))
        {
            out.append(formatter.formatProduct(prod) + System.lineSeparator());
            if (reviews.isEmpty()) {
                out.append(formatter.getText("no.reviews")  + System.lineSeparator());
            } else {
                out.append(reviews.stream()
                        .map(r -> formatter.formatReviews(r) + System.lineSeparator())
                        .collect(Collectors.joining()));
            }
        }
    }

    public void printProducts(Predicate<Product> filter, Comparator<Product> sorter, String languageTag) {
        try {
            readLock.lock();
            ResourceFormatter formatter = formatters.getOrDefault(languageTag, formatters.get("en-GB"));
            StringBuilder txt = new StringBuilder();
            products.keySet()
                    .stream()
                    .sorted(sorter)
                    .filter(filter)
                    .forEach(p -> txt.append(formatter.formatProduct(p) + '\n'));
            System.out.println(txt);
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Error printing products "+ e.getMessage(), e);
        } finally {
            readLock.unlock();
        }
    }

    private List<Review> loadReviews(Product prod) {
        List<Review> reviews = null;
        Path file = dataFolder.resolve(
               MessageFormat.format(config.getString("reviews.data.file"), prod.getId()));
        if(Files.notExists(file)){
            reviews = new ArrayList<>();
        } else {
            try {
                reviews = Files.lines(file, Charset.forName("UTF-8"))
                        .map(text -> parseReview(text))
                        .filter(review -> review != null)
                        .collect(Collectors.toList());
            } catch(IOException e) {
                logger.log(Level.WARNING, "Error Loading Review", e);
            }
        }
        return reviews;
    }

    private Product loadProducts(Path file) {
        Product prod = null;
        try {
            prod = parseProduct(Files.lines(file, Charset.forName("UTF-8"))
                    .findFirst()
                    .orElseThrow());
        } catch (IOException e ){
            logger.log(Level.WARNING, "Error Loading Product", e);
        }
        return prod;
    }

    private void loadAllData(){
        try {
            products = Files.list(dataFolder)
                    .filter(file -> file.getFileName().toString().startsWith("product"))
                    .map(file -> loadProducts(file))
                    .filter(prod -> prod != null)
                    .collect(Collectors.toMap(prod -> prod, prod -> loadReviews(prod)));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error Finding Files", e.getMessage());
        }
    }

    public Review parseReview(String text){
        Review review = null;
        try {
            Object[] values = reviewFormat.parse(text);
            review = new Review(Rateable.convert(Integer.parseInt((String)values[0])), (String)values[1]);
            //reviewProd(Integer.parseInt((String) values[0]),Rateable.convert(Integer.parseInt((String)values[1])), (String)values[2]);
        } catch (ParseException | NumberFormatException e) {
            logger.log(Level.WARNING, "Error Parsing Review " + text, e);
        }
        return review;
    }

    public Product parseProduct(String text) {
        Product prod =  null;
        try {
            Object[] values = productFormat.parse(text);
            int id = Integer.parseInt((String) values[1]);
            String name = (String)  values[2];
            BigDecimal price = BigDecimal.valueOf(Double.parseDouble((String) values[3]));
            Rating rating = Rateable.convert(Integer.parseInt((String)values[4]));
            switch ((String) values[0]) {
                case "D":
                    prod = new Drink(id,name,price,rating);
                    //createProd(id,name,price,rating);
                    break;
                case "F":
                    LocalDate bestBefore = LocalDate.parse((String) values[5]);
                    prod =  new Food(id,name,price,rating, bestBefore);
                    //createProd(id,name,price,rating, bestBefore);
                    break;
            }
        } catch (ParseException |
                 NumberFormatException |
                 DateTimeParseException e) {
            logger.log(Level.WARNING, "Error Parsing Product " + text, e);
        }
        return prod;
    }

    public void dumpData(){
        try{
            if(Files.notExists(tempFolder)){
                Files.createDirectory(tempFolder);
            }
            String timestamp = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
            Path tempFile = tempFolder.resolve(MessageFormat.format(config.getString("temp.file"), timestamp));
            try(ObjectOutputStream out = new ObjectOutputStream(
                    Files.newOutputStream(tempFile, StandardOpenOption.CREATE))){
                out.writeObject(products);
                //products = new HashMap<>();
            } catch (IOException e){
                logger.log(Level.WARNING, "Error Creating Temp File" , e.getMessage());
            }
        } catch (IOException e){
            logger.log(Level.WARNING, "Error Dumping Data", e.getMessage());
        }
    }

    public void restoreData(){
        try{
            if(Files.notExists(tempFolder)){
                Files.createDirectory(tempFolder);
            }
            Path tempFile = Files.list(tempFolder)
                    .filter(path -> path.getFileName().toString().endsWith("tmp"))
                    .findFirst().orElseThrow();
            try(ObjectInputStream in = new ObjectInputStream(
                    Files.newInputStream(tempFile, StandardOpenOption.DELETE_ON_CLOSE))){
            products = (HashMap)in.readObject();
            } catch (IOException e) {

            }
        }catch (Exception e) {
            logger.log(Level.WARNING, "Error Restoring Data", e.getMessage());
        }
    }

    public Map<String, String> getDiscounts(String languageTag){
        try{
            readLock.lock();
            ResourceFormatter formatter = formatters.getOrDefault(languageTag, formatters.get("en-GB"));
            return products.keySet()
                    .stream()
                    .collect(
                            Collectors.groupingBy(
                                    p -> p.getRating().getStars(),
                                    Collectors.collectingAndThen(
                                            Collectors.summingDouble(
                                                    p -> p.getDiscount().doubleValue()),
                                            d -> formatter.moneyFormat.format(d))));
        } finally {
            readLock.unlock();
        }
    }

    private static class ResourceFormatter {
        private Locale locale;
        private ResourceBundle resources;
        private DateTimeFormatter dateFormat;
        private NumberFormat moneyFormat;

        private ResourceFormatter(Locale locale) {
            this.locale = locale;
            resources = ResourceBundle.getBundle("tlahui.labs.app.data.resource", locale);
            dateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
            moneyFormat = NumberFormat.getCurrencyInstance(locale);
        }

        private String formatProduct(Product product) {
            return MessageFormat.format(resources.getString("product"), product.getName(),
                    moneyFormat.format(product.getPrice()), product.getRating().getStars(),
                    dateFormat.format(product.getBestBefore()));
        }

        private String formatReviews(Review review) {
            return MessageFormat.format(resources.getString("review"), review.getRating().getStars(),
                    review.getComments());
        }

        private String getText(String key) {
            return resources.getString(key);
        }
    }
}
