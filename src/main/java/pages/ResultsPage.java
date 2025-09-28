package pages;

import base.BasePage;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResultsPage extends BasePage {

    private static final By[] RESULT_SELECTORS = new By[] {
            By.cssSelector(".products .product-miniature, .js-product-miniature"),
            By.cssSelector(".product_list .product"),
            By.cssSelector(".product-grid .product"),
            By.cssSelector(".ap-result, .result-card, .hotel-card"),
            By.cssSelector(".room-list .room-item, .apartment-item")
    };

    public ResultsPage(WebDriver driver) { super(driver); }

    public int waitAndCountResults() {
        long end = System.currentTimeMillis() + 20_000;
        while (System.currentTimeMillis() < end) {
            for (By sel : RESULT_SELECTORS) {
                List<WebElement> items = driver.findElements(sel);
                if (!items.isEmpty()) {
                    try { wait.until(ExpectedConditions.visibilityOf(items.get(0))); } catch (Exception ignore) {}
                    return items.size();
                }
            }
            try { Thread.sleep(400); } catch (InterruptedException ignored) {}
        }
        return 0;
    }

    public boolean hasResults() {
        return waitAndCountResults() > 0;
    }

    private List<WebElement> getCards() {
        List<WebElement> all = new ArrayList<>();
        for (By sel : RESULT_SELECTORS) {
            all.addAll(driver.findElements(sel));
        }
        return all;
    }

    private Integer extractFirstInt(String text) {
        if (text == null) return null;
        Matcher m = Pattern.compile("(\\d+)").matcher(text);
        if (m.find()) return Integer.parseInt(m.group(1));
        return null;
    }

    public boolean allCardsShowAdults(int expected) {
        List<WebElement> cards = getCards();
        if (cards.isEmpty()) return false;

        for (WebElement card : cards) {
            List<WebElement> labels = card.findElements(By.cssSelector(".label-tooltip, .capacity, .meta, span, div"));
            boolean ok = false;
            for (WebElement lab : labels) {
                String t = lab.getText().trim();
                if (t.toLowerCase().contains("adult")) {
                    Integer n = extractFirstInt(t);
                    if (n != null && n == expected) { ok = true; break; }
                }
            }
            if (!ok) {
                List<WebElement> globals = driver.findElements(By.cssSelector(".label-tooltip, .capacity, .meta, span"));
                boolean foundGlobal = false;
                for (WebElement g : globals) {
                    String t = g.getText().trim().toLowerCase();
                    if (t.contains("adult")) {
                        Integer n = extractFirstInt(g.getText());
                        if (n != null && n == expected) { foundGlobal = true; break; }
                    }
                }
                if (!foundGlobal) return false;
            }
        }
        return true;
    }

    public boolean allCardsShowChildren(int expected) {
        List<WebElement> cards = getCards();
        if (cards.isEmpty()) return false;

        for (WebElement card : cards) {
            List<WebElement> labels = card.findElements(By.cssSelector(".label-tooltip, .capacity, .meta, span, div"));
            boolean ok = false;
            for (WebElement lab : labels) {
                String t = lab.getText().trim();
                String low = t.toLowerCase();
                if (low.contains("child")) {
                    Integer n = extractFirstInt(t);
                    if (n != null && n == expected) { ok = true; break; }
                }
            }
            if (!ok) {
                List<WebElement> globals = driver.findElements(By.cssSelector(".label-tooltip, .capacity, .meta, span"));
                boolean foundGlobal = false;
                for (WebElement g : globals) {
                    String t = g.getText().trim().toLowerCase();
                    if (t.contains("child")) {
                        Integer n = extractFirstInt(g.getText());
                        if (n != null && n == expected) { foundGlobal = true; break; }
                    }
                }
                if (!foundGlobal) return false;
            }
        }
        return true;
    }

    public double firstCardPrice() {
        try {
            String txt = driver.findElement(By.cssSelector(
                            ".product-miniature .price, .product .price, .hotel-card .price, .room-item .price"))
                    .getText().replaceAll("[^0-9.,]", "").replace(",", ".");
            return Double.parseDouble(txt);
        } catch (Exception e) {
            return -1;
        }
    }
}
