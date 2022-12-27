package tlahui.labs.app.data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

import static tlahui.labs.app.data.Rating.*;

/**
 * @author Tlahui
 *
 */
public abstract class Product implements Rateable<Product>, Serializable {

	private int id;
	private String name;
	private BigDecimal price;
	private Rating rating;
	private LocalDate bestBefore;
	public static final BigDecimal DISCOUNT_RATE = BigDecimal.valueOf(0.1);

	public Product(int id, String name, BigDecimal price, Rating rating) {
		this.id = id;
		this.name = name;
		this.price = price;
		this.rating = rating;
	}

	public Product(int id, String name, BigDecimal price) {
		this(id, name, price, NOT_RATED);
	}

	public Product() {
		this(0, "not defined", BigDecimal.valueOf(0), NOT_RATED);
	}

	public int getId() {
		return id;
	}

//	public void setId(final int id) {
//		this.id = id;
//	}

	public String getName() {
		return name;
	}

//	public void setName(final String name) {
//		this.name = name;
//	}

	public BigDecimal getPrice() {
		return price;
	}

//	public void setPrice(final BigDecimal price) {
//		this.price = price;
//	}

	public BigDecimal getDiscount() {
		return price.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP);

	}

	public Rating getRating() {
		return rating;
	}

	public LocalDate getBestBefore() {
		return LocalDate.now();
	}

	public abstract Product applyRating(Rating newRating);
//	{
//		return new Product(this.id, this.name, this.price, newRating);
//	}

	@Override
	public String toString() {
		return id + ", " + name + ", " + price + ", " + getDiscount() + ", " + rating.getStars() + ", " + getBestBefore();
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof Product)) {
			return false;
		}

		Product other = (Product) obj;
		return id == other.id; // && Objects.equals(name, other.name);
	}

}
