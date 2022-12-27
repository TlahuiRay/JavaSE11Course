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
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.logging.Logger;

public class ProductManagement {
    private static final Logger logger = Logger.getLogger(ProductManagement.class.getName());

    /*private Product prod;
    private Review[] reviews = new Review[5];*/
    private Map<Product, List<Review>> products = new HashMap<>();
    private ResourceFormatter formatter;
    private static Map<String, ResourceFormatter> formatters =
            Map.of(
                    "en-GB", new ResourceFormatter(Locale.UK),
                    "en-US", new ResourceFormatter(Locale.US),
                    "fr-FR", new ResourceFormatter(Locale.FRANCE),
                    "ru-RU", new ResourceFormatter(new Locale("ru", "RU")),
                    "zh-CN", new ResourceFormatter(Locale.CHINA),
                    "es-MX", new ResourceFormatter(new Locale("es", "MX"))
            );
    private ResourceBundle config = ResourceBundle.getBundle("tlahui.labs.app.data.config");
    private MessageFormat reviewFormat = new MessageFormat(config.getString("review.data.format"));
    private MessageFormat productFormat = new MessageFormat(config.getString("product.data.format"));

    private Path reportsFolder = Path.of(config.getString("reports.folder"));
    private Path dataFolder = Path.of(config.getString("data.folder"));
    private Path tempFolder = Path.of(config.getString("temp.folder"));

    public ProductManagement(Locale locale) {
        this(locale.toLanguageTag());
    }

    public ProductManagement(String languageTag) {
        changeLocale(languageTag);
    }

    public void changeLocale(String languageTag) {
        formatter = formatters.getOrDefault(languageTag, formatters.get("en-US"));
    }

    public static Set<String> getSupportedLocales() {
        return formatters.keySet();
    }

    public Product createProd(int id, String name, BigDecimal price, Rating rating,
                              LocalDate bestBefore) {
        Product prod = new Food(id, name, price, rating, bestBefore);
        products.putIfAbsent(prod, new ArrayList<Review>());
        return prod;

    }

    public Product createProd(int id, String name, BigDecimal price, Rating rating) {
        Product prod = new Drink(id, name, price, rating);
        products.putIfAbsent(prod, new ArrayList<Review>());
        return prod;
    }

    public Product reviewProd(int id, Rating rating, String comments) {
        try {
            return reviewProd(findProduct(id), rating, comments);
        } catch (ProductManagerException e) {
            //throw new RuntimeException(e);
            logger.log(Level.INFO,e.getMessage());
        }
        return null;
    }


    public Product reviewProd(Product prod, Rating rating, String comments) {
        // reviews = new Review(rating, comments);
//		if (reviews[reviews.length - 1] != null) {
//			reviews = Arrays.copyOf(reviews, reviews.length + 5);
//		}
        List<Review> reviews = products.get(prod);
        products.remove(prod);
        reviews.add(new Review(rating, comments));

        /*int sum = 0;
        for (Review review : reviews) {
            sum += review.getRating().ordinal();
        }*/

//		int sum = 0;
//		int i = 0;
//		boolean reviewed = false;
//		while (i < reviews.length && !reviewed) {
//			if (reviews[i] == null) {
//				reviews[i] = new Review(rating, comments);
//				reviewed = true;
//			}
//			sum += reviews[i].getRating().ordinal();
//			i++;
//		}
//      this.prod = prod.applyRating(rating);
        /*prod = prod.applyRating(Rateable.convert(Math.round((float) sum / reviews.size())));*/
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

    public void printProductReport(int id) {
        try {
            printProductReport(findProduct(id));
        } catch (ProductManagerException e) {
            //throw new RuntimeException(e);
            logger.log(Level.INFO,e.getMessage());
        } catch (IOException e) {
            logger.log(Level.SEVERE,"Error printing product report "+ e.getMessage(), e);
        }
    }

    public void printProductReport(Comparator<Product> sorter) {
        List<Product> productList = new ArrayList<>(products.keySet());
        productList.sort(sorter);

        StringBuilder txt = new StringBuilder();
        for (Product product : productList) {
            txt.append(formatter.formatProduct(product));
            txt.append('\n');
        }
        System.out.println(txt);
    }

    public void printProductReport(Product prod) throws IOException {
        List<Review> reviews = products.get(prod);
        Collections.sort(reviews);
        //StringBuilder txt = new StringBuilder();
        Path productFile = reportsFolder.resolve(
                MessageFormat.format(
                        config.getString("report.file"), prod.getId()));
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
        /*for (Review review : reviews) {
            txt.append(formatter.formatReviews(review));
            txt.append("\n");
        }
        if (reviews.isEmpty()) {
            txt.append(formatter.getText("no.reviews"));
            txt.append("\n");
        }
        System.out.println(txt);*/
    }

    public void printProducts(Predicate<Product> filter, Comparator<Product> sorter) {
        StringBuilder txt = new StringBuilder();
        products.keySet()
                .stream()
                .sorted(sorter)
                .filter(filter)
                .forEach(p -> txt.append(formatter.formatProduct(p) + '\n'));
        System.out.println(txt);
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
                logger.log(Level.WARNING, "Error Loading Review", e.getMessage());
            }
        }
        return reviews;
    }

    private Product loadProducts(Path file) {
        Product prod = null;
        try {
            prod = parseProduct(Files.lines(dataFolder.resolve(file), Charset.forName("UTF-8"))
                    .findFirst()
                    .orElseThrow());
        } catch (IOException e ){
            logger.log(Level.WARNING, "Error Loading Product", e.getMessage());
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
            //review = new Review(Rateable.convert(Integer.parseInt((String)values[0])), (String)values[1]);
            reviewProd(Integer.parseInt((String) values[0]),Rateable.convert(Integer.parseInt((String)values[1])), (String)values[2]);
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
                    //prod = new Drink(id,name,price,rating);
                    createProd(id,name,price,rating);
                    break;
                case "F":
                    LocalDate bestBefore = LocalDate.parse((String) values[5]);
                    //prod =  new Food(id,name,price,rating, bestBefore);
                    createProd(id,name,price,rating, bestBefore);
                    break;
            }
        } catch (ParseException |
                 NumberFormatException |
                 DateTimeParseException e) {
            logger.log(Level.WARNING, "Error Parsing Product " + text, e);
        }
        return prod;
    }
    public Product findProduct(int id) throws ProductManagerException {
        /*Product result = null;
        for (Product prod : products.keySet()) {
            if (prod.getId() == id) {
                result = prod;
                break;
            }
        }
        return result;*/
        return products.keySet()
                .stream()
                .filter(p -> p.getId() == id)
                .findFirst()
                .orElseThrow(() -> new ProductManagerException("Product with ID: " + id + " not found"));
                //.get();
                //.orElseGet(() -> null);
    }
    public Map<String, String> getDiscounts(){
        return products.keySet()
                .stream()
                .collect(
                        Collectors.groupingBy(
                                p -> p.getRating().getStars(),
                                Collectors.collectingAndThen(
                                        Collectors.summingDouble(
                                         p -> p.getDiscount().doubleValue()),
                                         d -> formatter.moneyFormat.format(d))));
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
