import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class ProductPage extends Page {
	private static final String url = "https://www.amazon.com/dp/";

	public ProductPage(WebDriver wd, String id) {
		super(wd, url + id);
	}

	@FindBy(id="productTitle")
	private WebElement title;

	public ProductPage(WebDriver wd) {
		super(wd);

		var currentUrl = wd.getCurrentUrl();

		if (!currentUrl.startsWith(url)) {
			throw new IllegalStateException(String.format("expected url \"%s\", got \"%s\"", url, currentUrl));
		}
	}

	public String getTitle() {
		return title.getText();
	}

	protected static String getURL(String query) {
		return url + query;
	}
}
