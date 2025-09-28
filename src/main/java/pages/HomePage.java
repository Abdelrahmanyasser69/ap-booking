package pages;

import base.BasePage;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.Arrays;
import java.util.List;

public class HomePage extends BasePage {

    private final By dateFromInput = By.cssSelector("input[name='from']");
    private final By dateToInput   = By.cssSelector("input[name='to']");
    private final By adultsInput   = By.cssSelector("input[name='adult'], #adult");
    private final By childrenInput = By.cssSelector("input[name='child'], #child");

    private final By[] searchButtonCandidates = new By[]{
            By.cssSelector("button[type='submit'][name='search_product']"),
            By.cssSelector("button[name='search_product']"),
            By.cssSelector("button[type='submit']"),
            By.cssSelector(".search_button")
    };

    private final List<By> calendarRoots = Arrays.asList(
            By.cssSelector(".ui-datepicker"),
            By.cssSelector(".ui-datepicker-group"),
            By.cssSelector(".datepicker"),
            By.cssSelector(".flatpickr-calendar")
    );

    private final List<By> nextMonthBtn = Arrays.asList(
            By.cssSelector(".ui-datepicker-next"),
            By.cssSelector(".ui-datepicker-next .ui-icon-circle-triangle-e"),
            By.cssSelector("[aria-label*='Next'], [aria-label*='next']"),
            By.cssSelector(".datepicker-next, .next, .next-month"),
            By.cssSelector(".flatpickr-next-month")
    );

    private final List<By> prevMonthBtn = Arrays.asList(
            By.cssSelector(".ui-datepicker-prev"),
            By.cssSelector(".ui-datepicker-prev .ui-icon-circle-triangle-w"),
            By.cssSelector("[aria-label*='Previous'], [aria-label*='Prev'], [aria-label*='previous']"),
            By.cssSelector(".datepicker-prev, .prev, .prev-month"),
            By.cssSelector(".flatpickr-prev-month")
    );

    public HomePage(WebDriver driver) { super(driver); }

    // ====== Inputs setters/getters ======
    public HomePage setDateFrom(String date) { setValueJs(dateFromInput, date); return this; }
    public HomePage setDateTo(String date)   { setValueJs(dateToInput,   date); return this; }
    public HomePage setAdults(int n)         { setValueJs(adultsInput,   String.valueOf(n)); return this; }
    public HomePage setChildren(int n)       { setValueJs(childrenInput, String.valueOf(n)); return this; }

    public int getAdults()    { return readInt(adultsInput); }
    public int getChildren()  { return readInt(childrenInput); }
    public String getFromVal(){ return readValue(dateFromInput); }
    public String getToVal()  { return readValue(dateToInput); }

    public ResultsPage search() {
        for (By b : searchButtonCandidates) {
            List<WebElement> list = driver.findElements(b);
            if (!list.isEmpty()) {
                clickHard(b);
                return new ResultsPage(driver);
            }
        }
        throw new NoSuchElementException("Search button not found");
    }

    public HomePage clickSearch() {
        search();
        return this;
    }

    public HomePage openFromCalendar() { click(dateFromInput); waitForCalendarOpen(); return this; }
    public HomePage openToCalendar()   { click(dateToInput);   waitForCalendarOpen(); return this; }

    private void waitForCalendarOpen() {
        long end = System.currentTimeMillis() + 8_000;
        while (System.currentTimeMillis() < end) {
            WebElement r = findAnyDisplayed(calendarRoots);
            if (r != null) return;
            try { Thread.sleep(150); } catch (InterruptedException ignored) {}
        }
        throw new TimeoutException("Calendar did not open");
    }

    public HomePage pickDayInOpenCalendar(int dayOfMonth) {
        WebElement root = findAnyDisplayed(calendarRoots);
        if (root == null) throw new NoSuchElementException("Calendar root not found/open");

        List<WebElement> anchors = root.findElements(By.cssSelector(".ui-datepicker-calendar td a"));
        for (WebElement a : anchors) {
            if (a.isDisplayed() && a.isEnabled() && a.getText().trim().equals(String.valueOf(dayOfMonth))) {
                a.click();
                return this;
            }
        }

        List<By> dayButtons = Arrays.asList(
                By.cssSelector(".flatpickr-day[aria-label]:not(.prevMonthDay):not(.nextMonthDay)"),
                By.cssSelector("td.day:not(.disabled) button, td.available:not(.off) button, .day:not(.disabled) button"),
                By.cssSelector("td.day:not(.disabled), td.available:not(.off), .day:not(.disabled)")
        );

        for (By sel : dayButtons) {
            for (WebElement d : root.findElements(sel)) {
                String txt = d.getText().trim();
                if (txt.equals(String.valueOf(dayOfMonth)) && d.isDisplayed() && d.isEnabled()) {
                    d.click();
                    return this;
                }
            }
        }
        throw new NoSuchElementException("Could not pick day '" + dayOfMonth + "' in open calendar");
    }

    public boolean nextMonthInOpenCalendar() { return clickFirstInRoot(nextMonthBtn); }
    public boolean prevMonthInOpenCalendar() { return clickFirstInRoot(prevMonthBtn); }

    private boolean clickFirstInRoot(List<By> candidates) {
        WebElement root = findAnyDisplayed(calendarRoots);
        if (root == null) return false;
        for (By sel : candidates) {
            List<WebElement> els = root.findElements(sel);
            if (!els.isEmpty()) {
                WebElement e = els.get(0);
                if (safeClick(e)) return true;
                try { WebElement parent = e.findElement(By.xpath("./..")); if (safeClick(parent)) return true; } catch (Exception ignore) {}
            }
        }
        return false;
    }

    private boolean safeClick(WebElement e) {
        try {
            if (e.isDisplayed() && e.isEnabled()) { e.click(); return true; }
        } catch (Exception ignore) {}
        return false;
    }

    public HomePage adultsPlus()    { plusMinus(adultsInput, true);  return this; }
    public HomePage adultsMinus()   { plusMinus(adultsInput, false); return this; }
    public HomePage childrenPlus()  { plusMinus(childrenInput, true);  return this; }
    public HomePage childrenMinus() { plusMinus(childrenInput, false); return this; }

    private void plusMinus(By inputSel, boolean increment) {
        if (clickNearButtons(inputSel, increment)) return;
        if (clickGlobalButtons(increment)) return;
        WebElement input = wait.until(ExpectedConditions.presenceOfElementLocated(inputSel));
        input.sendKeys(increment ? Keys.ARROW_UP : Keys.ARROW_DOWN);
    }

    private boolean clickNearButtons(By inputSel, boolean increment) {
        By[] plus = new By[]{
                By.cssSelector(".bootstrap-touchspin-up, .btn-plus, .plus, .increment, [aria-label*='increase']"),
                By.cssSelector("button[title*='+'], button[aria-label*='+']")
        };
        By[] minus = new By[]{
                By.cssSelector(".bootstrap-touchspin-down, .btn-minus, .minus, .decrement, [aria-label*='decrease']"),
                By.cssSelector("button[title*='-'], button[aria-label*='-']")
        };
        By[] btnCandidates = increment ? plus : minus;

        WebElement input = wait.until(ExpectedConditions.presenceOfElementLocated(inputSel));
        WebElement scope = input;
        for (int up = 0; up < 3 && scope != null; up++) {
            for (By bSel : btnCandidates) {
                List<WebElement> btns = scope.findElements(bSel);
                for (WebElement b : btns) {
                    if (b.isDisplayed() && b.isEnabled()) { b.click(); return true; }
                }
            }
            try { scope = scope.findElement(By.xpath("..")); } catch (Exception e) { scope = null; }
        }
        return false;
    }

    private boolean clickGlobalButtons(boolean increment) {
        By[] candidates = increment
                ? new By[]{ By.cssSelector(".bootstrap-touchspin-up, .btn-plus, .plus, .increment, [aria-label*='increase']") }
                : new By[]{ By.cssSelector(".bootstrap-touchspin-down, .btn-minus, .minus, .decrement, [aria-label*='decrease']") };
        for (By sel : candidates) {
            List<WebElement> btns = driver.findElements(sel);
            for (WebElement b : btns) {
                if (b.isDisplayed() && b.isEnabled()) { b.click(); return true; }
            }
        }
        return false;
    }


    public HomePage selectDateRange(int fromOffsetDays, int toOffsetDays) {
        java.time.LocalDate base = java.time.LocalDate.now().plusDays(fromOffsetDays);
        java.time.LocalDate end  = java.time.LocalDate.now().plusDays(toOffsetDays);
        setDateFrom(utils.DateUtil.fmt(base));
        setDateTo(utils.DateUtil.fmt(end));
        return this;
    }

    public HomePage typeDateRange(String from, String to) {
        setDateFrom(from);
        setDateTo(to);
        return this;
    }

    public HomePage setGuests(int adults, int children) {
        setAdults(adults);
        setChildren(children);
        return this;
    }

    public HomePage inputGuests(String adults, String children) {
        try { setAdults(Integer.parseInt(adults.trim())); } catch (Exception ignore) { setAdults(2); }
        try { setChildren(Integer.parseInt(children.trim())); } catch (Exception ignore) { setChildren(0); }
        return this;
    }

    public HomePage applyFilter(String text) {
        List<By> candidates = Arrays.asList(
                By.cssSelector("input[name='search_product']"),
                By.cssSelector("input[type='search']"),
                By.cssSelector("#search_query, #search, .search_input")
        );
        for (By c : candidates) {
            List<WebElement> found = driver.findElements(c);
            if (!found.isEmpty()) {
                WebElement inp = found.get(0);
                ((JavascriptExecutor) driver).executeScript("arguments[0].value = arguments[1];", inp, text);
                ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].dispatchEvent(new Event('input',{bubbles:true}));" +
                                "arguments[0].dispatchEvent(new Event('change',{bubbles:true}));", inp);
                break;
            }
        }
        return this;
    }
}
