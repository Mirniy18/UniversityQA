import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class HomePage extends Page {
	public static final String url = "https://www.amazon.com/";

	@FindBy(id = "twotabsearchtextbox")
	private WebElement searchBox;

	@FindBy(id = "nav-search-submit-button")
	private WebElement searchButton;

	@FindBy(id = "icp-nav-flyout")
	private WebElement languageFlyout;

	@FindBy(xpath = "//a[@id='nav-cart']")
	private WebElement cart;

	public HomePage(WebDriver wd) {
		super(wd, url);
	}

	public SearchPage search(String query, boolean useUI) {
		if (useUI) {
			searchBox.sendKeys(query);
			searchButton.click();

			return new SearchPage(wd);
		} else {
			return new SearchPage(wd, query);
		}
	}

	public void changeLanguage(int index) {
		Actions builder = new Actions(wd);
		builder.moveToElement(languageFlyout).perform();

		new WebDriverWait(wd, Duration.ofSeconds(3)).until(
				ExpectedConditions.elementToBeClickable(languageFlyout.findElement(By.cssSelector("#nav-flyout-icp > div:nth-child(2) > a:nth-child(" + index + ")")))
		).click();

		try {
			Thread.sleep(3_000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public CartPage openCart() {
		cart.click();

		return new CartPage(wd);
	}
}
