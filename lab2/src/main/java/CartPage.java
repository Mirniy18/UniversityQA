import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class CartPage extends Page {
	private static final String url = "https://www.amazon.com/gp/cart/view.html";

	@FindBy(xpath = "//div[@class='a-row sc-your-amazon-cart-is-empty']/h2")
	private WebElement empty;

	public CartPage(WebDriver wd) {
		super(wd);

		var currentUrl = wd.getCurrentUrl();

		if (!currentUrl.startsWith(url)) {
			wd.get(url);
		}
	}

	public boolean isEmpty() {
		return empty.getText().contains("Your Amazon Cart is empty");
	}

	protected static String getURL(String query) {
		return url + query;
	}
}
