package tlahui.labs.app.shop;

import tlahui.labs.app.data.ProductManagement;

public class Shop {
	private static final String LANG = "en-GB";

    public static void main(String[] args) {

        ProductManagement pm = ProductManagement.getInstance();

        pm.printProductReport(101,LANG);
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
                .forEach((rating, discount) -> System.out.println(rating + "\t" + discount));
    }

}