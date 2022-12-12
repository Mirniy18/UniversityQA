import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.util.Objects;

public class TestAmazon {
	private static final boolean USE_FIREFOX = true;

	private static WebDriver wd;

	@BeforeAll
	static void beforeAll() {
		var propertyKey = USE_FIREFOX ? "webdriver.gecko.driver" : "webdriver.chrome.driver";
		var driverName = USE_FIREFOX ? "geckodriver.exe" : "chromedriver.exe";

		var driverPath = Assertions.assertDoesNotThrow(
				() -> Objects.requireNonNull(TestAmazon.class.getClassLoader().getResource(driverName)).getPath().substring(1),
				"webdriver not found"
		);

		System.setProperty(propertyKey, driverPath);

		wd = Assertions.assertDoesNotThrow(
				() -> new FirefoxDriver(),
				"cannot start webdriver"
		);
	}

	@Test
	void testSearchBox() {
		var homePage = new HomePage(wd);

		var q = "samsung";

		homePage.search(q, true);

		Assertions.assertTrue(wd.getCurrentUrl().contains("k=" + q));
	}

	@Test
	void testSearchResultsContainQuery() {
		var q = "samsung";
		var searchPage = new SearchPage(wd, q);

		for (int i = 0; i < Math.min(searchPage.getResultsCount(), 3); ++i) {
			var title = searchPage.getResult(i).findElement(SearchPage.resultTitle).getText();

			Assertions.assertTrue(title.toLowerCase().contains(q.toLowerCase()), '"' + title + "\" does not contain \"" + q + '"');
		}
	}

	@Test
	void testSearchResultsDoesNotContainWrongQuery() {
		var q = "samsung";
		var searchPage = new SearchPage(wd, q);

		for (int i = 0; i < Math.min(searchPage.getResultsCount(), 3); ++i) {
			var title = searchPage.getResult(i).findElement(SearchPage.resultTitle).getText();

			Assertions.assertFalse(title.toLowerCase().contains("logitech"), '"' + title + "\" does not contain \"" + q + '"');
		}
	}

	@Test
	void testSearchResultsContain10Results() {
		var q = "samsung";
		var searchPage = new SearchPage(wd, q);

		Assertions.assertTrue(searchPage.getResultsCount() >= 10);
	}

	@Test
	void testProductHasSameTitle() {
		var q = "samsung";
		var searchPage = new SearchPage(wd, q);

		for (int i = 0; i < Math.min(searchPage.getResultsCount(), 3); ++i) {
			var title = searchPage.getResult(i).findElement(SearchPage.resultTitle).getText();

			Assertions.assertTrue(title.toLowerCase().contains(q.toLowerCase()), '"' + title + "\" does not contain \"" + q + '"');
		}
	}

	@Test
	void testSearchResultsSortByFeatured() {
		var q = "samsung";
		var searchPage = new SearchPage(wd, q);

		searchPage.sortByFeatured();

		Assertions.assertTrue(searchPage.isSortByFeatured());
	}

	@Test
	void testSearchResultsSortByPriceAsc() {
		var q = "samsung";
		var searchPage = new SearchPage(wd, q);

		searchPage.sortByPriceAsc();

		Assertions.assertTrue(searchPage.isSortByPriceAsc());
	}

	@Test
	void testSearchResultsSortByPriceDes() {
		var q = "samsung";
		var searchPage = new SearchPage(wd, q);

		searchPage.sortByPriceDes();

		Assertions.assertTrue(searchPage.isSortByPriceDes());
	}

	@Test
	void testSearchResultsSortByReviews() {
		var q = "samsung";
		var searchPage = new SearchPage(wd, q);

		searchPage.sortByReview();

		Assertions.assertTrue(searchPage.isSortByReview());
	}

	@Test
	void testSearchResultsSortByNew() {
		var q = "samsung";
		var searchPage = new SearchPage(wd, q);

		searchPage.sortByNew();

		Assertions.assertTrue(searchPage.isSortByNew());
	}

	@Test
	void testCartEmpty() {
		var homePage = new HomePage(wd);

		var cartPage = homePage.openCart();

		Assertions.assertTrue(cartPage.isEmpty());
	}

	@Test
	void testLangEN() {
		var homePage = new HomePage(wd);

		homePage.changeLanguage(1);

		Assertions.assertEquals("Hello, sign in", wd.findElement(By.id("nav-link-accountList-nav-line-1")).getText());
	}

	@Test
	void testLangES() {
		var homePage = new HomePage(wd);

		homePage.changeLanguage(2);

		Assertions.assertEquals("Hola, Identifícate", wd.findElement(By.id("nav-link-accountList-nav-line-1")).getText());
	}

	@Test
	void testLangAR() {
		var homePage = new HomePage(wd);

		homePage.changeLanguage(3);

		Assertions.assertEquals("مرحباً. تسجيل الدخول", wd.findElement(By.id("nav-link-accountList-nav-line-1")).getText());
	}

	@Test
	void testLangPO() {
		var homePage = new HomePage(wd);

		homePage.changeLanguage(7);

		Assertions.assertEquals("Olá, faça seu login", wd.findElement(By.id("nav-link-accountList-nav-line-1")).getText());
	}

	@AfterAll
	static void quitQA() {
		wd.close();
	}
}
