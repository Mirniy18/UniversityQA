import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

public abstract class Page {
	protected final WebDriver wd;

	protected Page(WebDriver wd) {
		PageFactory.initElements(this.wd = wd, this);
	}

	protected Page(WebDriver wd, String url) {
		wd.get(url);

		PageFactory.initElements(this.wd = wd, this);
	}
}
