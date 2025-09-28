package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.HomePage;
import pages.ResultsPage;
import utils.DateUtil;

public class AllScenariosTest extends BaseTest {

    // P0-1: Datepicker in-range (اليوم -> +6)
    @Test
    public void P0_datepicker_inRange_today_to_plus6_returnsResults() {
        HomePage home = new HomePage(driver);
        home.selectDateRange(0, 6).clickSearch();
        ResultsPage results = new ResultsPage(driver);
        Assert.assertTrue(results.hasResults(), "Expected results after date range search.");
    }

    // P0-2: Typing out-of-range — المفروض يجيب نتائج بردو (الموقع بيسمح بالتاريخ المكتوب)
    @Test
    public void P0_typing_outOfRange_should_apply_and_return_results() {
        HomePage home = new HomePage(driver);
        home.typeDateRange("2025-10-10", "2025-10-12").clickSearch();
        ResultsPage results = new ResultsPage(driver);
        Assert.assertTrue(results.hasResults(), "Expected results after typing out-of-range dates.");
    }

    // P0-3: Guests via +/- (3 بالغين، 2 أطفال) + تحقق كل الكروت
    @Test
    public void P0_guests_dropdown_plusMinus_then_search_expect_results_and_tooltips() {
        HomePage home = new HomePage(driver);
        home.setGuests(3, 2).clickSearch();
        ResultsPage results = new ResultsPage(driver);
        Assert.assertTrue(results.allCardsShowAdults(3), "Every card must show 'Adults: 3'.");
        Assert.assertTrue(results.allCardsShowChildren(2), "Every card must show 'Childrens: 2'.");
    }

    // P1-1: Guests via direct input (2 بالغين، 1 طفل)
    @Test
    public void P1_guests_inputs_then_search_expect_results_and_tooltips() {
        HomePage home = new HomePage(driver);
        home.inputGuests("2", "1").clickSearch();
        ResultsPage results = new ResultsPage(driver);
        Assert.assertTrue(results.allCardsShowAdults(2), "Every card must show 'Adults: 2'.");
        Assert.assertTrue(results.allCardsShowChildren(1), "Every card must show 'Childrens: 1'.");
    }

    // P1-2: Filtration Smoke (كلمة عامة)
    @Test
    public void P1_filtration_smoke() {
        HomePage home = new HomePage(driver);
        home.applyFilter("Spa").clickSearch();
        ResultsPage results = new ResultsPage(driver);
        Assert.assertTrue(results.hasResults(), "Expected filtered results to appear.");
    }

    // P0-4: Datepicker custom range (10 -> 12)
    @Test
    public void P0_datepicker_customRange_then_search_returnsResults() {
        HomePage home = new HomePage(driver);
        home.selectDateRange(10, 12).clickSearch();
        ResultsPage results = new ResultsPage(driver);
        Assert.assertTrue(results.hasResults(), "Expected results for custom date range.");
    }
}
