package base;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class BasePage {
    protected final WebDriver driver;
    protected final WebDriverWait wait;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    protected void setValueJs(By locator, String value) {
        WebElement el = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = arguments[1];", el, value);
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].dispatchEvent(new Event('input',{bubbles:true}));" +
                        "arguments[0].dispatchEvent(new Event('change',{bubbles:true}));", el);
    }

    protected void click(By locator) {
        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(locator));
        el.click();
    }

    protected void clickHard(By locator) {
        WebElement el = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", el);
        try {
            wait.until(ExpectedConditions.elementToBeClickable(el)).click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        }
    }

    protected WebElement findAnyDisplayed(List<By> locators) {
        long end = System.currentTimeMillis() + 5_000;
        while (System.currentTimeMillis() < end) {
            for (By by : locators) {
                try {
                    List<WebElement> els = driver.findElements(by);
                    for (WebElement e : els) {
                        if (e.isDisplayed()) return e;
                    }
                } catch (StaleElementReferenceException | NoSuchElementException ignored) {}
            }
            try { Thread.sleep(150); } catch (InterruptedException ignored) {}
        }
        return null;
    }

    protected int readInt(By sel) {
        WebElement el = wait.until(ExpectedConditions.presenceOfElementLocated(sel));
        String v = el.getAttribute("value");
        try { return Integer.parseInt(v.trim()); } catch (Exception e) { return 0; }
    }

    protected String readValue(By sel) {
        WebElement el = wait.until(ExpectedConditions.presenceOfElementLocated(sel));
        return el.getAttribute("value");
    }
}
