package tlahui.labs.app.data;

public enum Rating {

	NOT_RATED("Not Rated"), ONE_START(" 1 Stars *"),
	TWO_START("2 Stars **"), THREE_START("3 Stars ***"),
	FOUR_START("4 Stars ****"), FIVE_START("5 Stars *****");
	private String stars;

	private Rating(String stars) {
		this.stars = stars;
	}

	public String getStars() {
		return stars;
	}
	
	
	
	

}
