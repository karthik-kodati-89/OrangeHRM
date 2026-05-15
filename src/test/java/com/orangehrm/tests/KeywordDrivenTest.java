package com.orangehrm.tests;

import com.orangehrm.base.BaseTest;
import com.orangehrm.keywords.KeywordEngine;
import com.orangehrm.utils.ExcelReader;
import org.testng.annotations.Test;
import java.util.List;

/**
 * KEYWORD-DRIVEN test.
 * The .xlsx file holds rows of [keyword, locator, data] - the engine
 * executes each row sequentially. Add new test cases by editing Excel only,
 * no Java code changes needed.
 */
public class KeywordDrivenTest extends BaseTest {

    @Test(description = "Run a keyword-driven flow loaded from Excel")
    public void testKeywordDrivenLogin() {
        KeywordEngine engine = new KeywordEngine(driver);
        List<String[]> steps = ExcelReader.getSheetAsList(
                "src/test/resources/testdata/KeywordTests.xlsx", "LoginFlow");

        for (String[] step : steps) {
            String keyword = step[0];
            String locator = step.length > 1 ? step[1] : "";
            String data    = step.length > 2 ? step[2] : "";
            engine.execute(keyword, locator, data);
        }
    }
}
