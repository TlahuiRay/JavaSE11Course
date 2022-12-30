package tlahui.labs.app.shop;

import tlahui.labs.app.data.Product;
import tlahui.labs.app.data.ProductManagement;

import java.time.LocalDate;


public class Shop {

	public static void main(String[] args) {

		ProductManagement pm = ProductManagement.getInstance();

/*		pm.parseProduct("D,101,Tea,1.99,0,2000-01-01");
		pm.parseReview("101,4,Nice hot cup of tea");
		pm.parseReview("101,5,The Perfect Tea");
		pm.parseReview("101,4,Fine Tea");
		pm.parseReview("101,4,Good Tea");
		pm.parseReview("101,3,Just add some lemon");

		*//*pm.createProd(101, "Tea", BigDecimal.valueOf(1.99), Rating.NOT_RATED);
		pm.reviewProd(101, Rating.FOUR_START, "Nice Hot Cup of Tea");
		pm.reviewProd(101, Rating.TWO_START, "Rather weak Tea");
		pm.reviewProd(101, Rating.FOUR_START, "Fine Tea");
		pm.reviewProd(101, Rating.FOUR_START, "Good Tea");
		pm.reviewProd(101, Rating.FIVE_START, "The Perfect Tea");
		pm.reviewProd(101, Rating.THREE_START, "Just add some lemon");*//*

		pm.parseProduct("D,102,Coffee,1.75,0,2022-12-11");
		pm.parseReview("102,3,Coffee was ok");
		pm.parseReview("102,2,There was no milk with the coffee");
		pm.parseReview("102,5,It's perfect with enough sugar");

		pm.parseProduct("F,103,Cake,3.99,0,"+LocalDate.now().plusDays(2).toString());
		pm.parseReview("103,5,Very nice cake");
		pm.parseReview("103,4,It's good, but I was expecting more chocolate");
		pm.parseReview("103,5,This cake is perfect");*/

		pm.printProductReport(101,"en-GB");
		pm.printProductReport(102,"en-GB");
		pm.printProductReport(103,"en-GB");

		pm.dumpData();
		pm.restoreData();

/*		pm.parseProduct("F,104,Cookie,2.99,0,"+LocalDate.now().toString());
		pm.parseReview("104,3,Just another cookie");
		pm.parseReview("104,3,Ok");

		pm.parseProduct("D,105,Hot Chocolate,2.5,0,2000-01-01");
		pm.parseReview("105,4,Not bad at all");
		pm.parseReview("105,4,Tasty !");

		pm.parseProduct("F,106,Chocolate Cake,2.00,0,"+LocalDate.now().plusDays(3).toString());
		pm.parseReview("106,2,Too sweet");
		pm.parseReview("106,3,Better then cookie");
		pm.parseReview("106,4,Too bitter");
		pm.parseReview("106,1,I don't get it!");*/

		pm.printProductReport(104,"en-GB");
		pm.printProductReport(105,"en-GB");
		pm.printProductReport(106,"en-GB");

		pm.printProducts(p-> p.getPrice().floatValue() < 2,
				(pr1,pr2) -> pr2.getRating().ordinal() - pr1.getRating().ordinal(),
				"en-GB");

/*		Comparator<Product> ratingSorter = (prod1,prod2) -> prod2.getRating().ordinal() - prod1.getRating().ordinal();
		Comparator<Product> priceSorter = Comparator.comparing(Product::getPrice);

		pm.printProductReport(ratingSorter.thenComparing(priceSorter));
		pm.printProductReport(ratingSorter.thenComparing(priceSorter).reversed());*/

		pm.getDiscounts("en-GB")
				.forEach((rating, discount) -> System.out.println(rating + "\t" + discount));
		
//		pm.printProductReport();
//		Product p3 = pm.createProd(103, "Cake", BigDecimal.valueOf(3.99), Rating.FIVE_START, LocalDate.now().plusDays(2));
//		Product p4 = pm.createProd(105, "Cookie", BigDecimal.valueOf(3.99), Rating.TWO_START, LocalDate.now().plusDays(5));
//		Product p5 = p3.applyRating(Rating.THREE_START);
//		Product p6 = pm.createProd(104, "Chocolate", BigDecimal.valueOf(2.99), Rating.FIVE_START);
//		Product p7 = pm.createProd(104, "Chocolate", BigDecimal.valueOf(2.99), Rating.FIVE_START, LocalDate.now().plusDays(2));
//		Product p8 = p4.applyRating(Rating.FIVE_START);
//		Product p9 = p1.applyRating(Rating.FOUR_START);
//		System.out.println(p6.equals(p7));
////		p3 = p3.applyRating(Rating.THREE_START);
//		
////		if(p3 instanceof Food) {
////			LocalDate bestBefore =  ((Food) p3).getBestBefore();
////		}
//
////		System.out.println(p1.getId() + " " + p1.getName() + " " + p1.getPrice() + " " + p1.getDiscount() + " "
////				+ p1.getRating().getStars());
////		System.out.println(p2.getId() + " " + p2.getName() + " " + p2.getPrice() + " " + p2.getDiscount() + " "
////				+ p2.getRating().getStars());
////		System.out.println(p3.getId() + " " + p3.getName() + " " + p3.getPrice() + " " + p3.getDiscount() + " "
////				+ p3.getRating().getStars());
////		System.out.println(p4.getId() + " " + p4.getName() + " " + p4.getPrice() + " " + p4.getDiscount() + " "
////				+ p4.getRating().getStars());
////		System.out.println(p5.getId() + " " + p5.getName() + " " + p5.getPrice() + " " + p5.getDiscount() + " "
////				+ p5.getRating().getStars());
////		System.out.println(p1);
////		System.out.println(p2);
////		System.out.println(p3);
////		System.out.println(p4);
////		System.out.println(p5);
////		System.out.println(p6);
////		System.out.println(p7);
////		System.out.println(p8);
////		System.out.println(p9);
	}

}
