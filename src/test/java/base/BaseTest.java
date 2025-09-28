package base;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.Assert;
import org.testng.annotations.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Duration;
import java.util.List;

public class BaseTest {
    protected static WebDriver driver;
    protected static String baseUrlMemo = "https://demo1.leotheme.com/ap_booking_demo/en/home-2.html";
    private static Path userDataDir;
    private static boolean cfCheckedOnce = false;

    @Parameters({"baseUrl"})
    @BeforeSuite(alwaysRun = true)
    public void setUpSuite(@Optional("https://demo1.leotheme.com/ap_booking_demo/en/home-2.html") String baseUrl) {
        baseUrlMemo = baseUrl;
        if (driver != null) return;

        WebDriverManager.chromedriver().setup();

        userDataDir = Paths.get("selenium-profile");
        try { Files.createDirectories(userDataDir); } catch (IOException ignore) {}

        ChromeOptions opts = new ChromeOptions();
        opts.addArguments("user-data-dir=" + userDataDir.toAbsolutePath());
        opts.addArguments("profile-directory=Default");
        opts.addArguments("--no-first-run", "--no-default-browser-check", "--start-maximized");
        opts.addArguments("--disable-blink-features=AutomationControlled");
        opts.addArguments("--lang=en-GB,en-US;q=0.9,ar;q=0.8");
        opts.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        opts.setExperimentalOption("useAutomationExtension", false);

        driver = new ChromeDriver(opts);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));

        driver.get(baseUrlMemo);
        waitForCloudflareIfPresentOrFail();
        cfCheckedOnce = true;
    }

    @BeforeMethod(alwaysRun = true)
    public void goHomeBeforeEach() {
        if (driver == null) throw new IllegalStateException("Driver is null");
        driver.navigate().to(baseUrlMemo);
        if (!"true".equalsIgnoreCase(System.getenv("CF_SKIP")) && looksLikeCf()) {
            waitForCloudflareIfPresentOrFail();
        }
    }

    @AfterSuite(alwaysRun = true)
    public void tearDownSuite() {
        try { if (driver != null) driver.quit(); } catch (Exception ignore) {}
        driver = null;
    }

    protected void waitForCloudflareIfPresentOrFail() {
        if ("true".equalsIgnoreCase(System.getenv("CF_SKIP"))) return;
        if (!looksLikeCf()) return;

        System.out.println("== CF detected. Solve in the browser, then press ENTER ONLY AFTER the page fully loads ==");
        dump("debug_page.html", safePageSource());
        try {
            File ss = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Files.copy(ss.toPath(), Path.of("debug_snapshot.png"), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception ignore) {}
        System.out.println("URL: " + driver.getCurrentUrl());

        try { System.in.read(); } catch (IOException ignored) {}
        try { driver.navigate().refresh(); } catch (Exception ignore) {}

        long start = System.currentTimeMillis();
        long timeout = 240_000;
        while (System.currentTimeMillis() - start < timeout) {
            if (!looksLikeCf()) { System.out.println("== CF cleared =="); return; }
            sleep(400);
        }
        Assert.fail("Cloudflare still blocking after timeout. Please whitelist automation or complete Turnstile faster.");
    }

    protected boolean looksLikeCf() {
        try {
            String title = (driver.getTitle() == null ? "" : driver.getTitle()).toLowerCase();
            String src   = safePageSource().toLowerCase();
            List<WebElement> frames = driver.findElements(By.cssSelector(
                    "iframe[title*='cloudflare'],iframe[id^='cf-chl-widget'],iframe[src*='challenges.cloudflare.com']"));
            return title.contains("just a moment") || src.contains("cf-turnstile") || !frames.isEmpty();
        } catch (Exception ignore) { return false; }
    }

    private String safePageSource() {
        try { return driver.getPageSource(); } catch (Exception e) { return ""; }
    }

    private void dump(String name, String content) {
        try {
            Files.writeString(Path.of(name), content, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception ignore) {}
    }

    protected void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
