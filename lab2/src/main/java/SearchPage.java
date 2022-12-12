import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class SearchPage extends Page {
	private static final String url = "https://www.amazon.com/s?k=";

	public static final By resultTitle = By.cssSelector("div.sg-row > div:nth-child(2)");

	public SearchPage(WebDriver wd, String query) {
		super(wd, url + query);
	}

	@FindBy(css="div.s-result-item.s-asin")
	private List<WebElement> results;

	@FindBy(id="s-result-sort-select")
	private WebElement sortBy;

	public SearchPage(WebDriver wd) {
		super(wd);

		var currentUrl = wd.getCurrentUrl();

		if (!currentUrl.startsWith(url)) {
			throw new IllegalStateException(String.format("expected url \"%s\", got \"%s\"", url, currentUrl));
		}
	}

	public WebElement getResult(int index) {
		return results.get(index);
	}

	public String getResultTitle(int index) {
		return getResult(index).findElement(SearchPage.resultTitle).getText();
	}

	public int getResultsCount() {
		return results.size();
	}

	private void sortBy(int index) {
		sortBy.click();

		wd.findElement(By.id("s-result-sort-select_" + index)).click();
	}

	public void sortByFeatured() {
		sortBy(1);
	}
	public void sortByPriceAsc() {
		sortBy(2);
	}
	public void sortByPriceDes() {
		sortBy(3);
	}
	public void sortByReview() {
		sortBy(4);
	}
	public void sortByNew() {
		sortBy(6);
	}

	private String getSortByText() {
		return wd.findElement(By.id(".a-dropdown-prompt")).getText();
	}

	public boolean isSortByFeatured() {
		return getSortByText().equals("Featured");
	}
	public boolean isSortByPriceAsc() {
		return getSortByText().equals("Price: Low to High");
	}
	public boolean isSortByPriceDes() {
		return getSortByText().equals("Price: High to Low");
	}
	public boolean isSortByReview() {
		return getSortByText().equals("Avg. Customer Review");
	}
	public boolean isSortByNew() {
		return getSortByText().equals("Newest Arrivals");
	}

	protected static String getURL(String query) {
		return url + query;
	}
}
