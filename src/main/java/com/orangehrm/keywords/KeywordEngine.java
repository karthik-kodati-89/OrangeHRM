package com.orangehrm.keywords;

import com.orangehrm.exceptions.FrameworkException;
import com.orangehrm.interfaces.IKeywordAction;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * KEYWORD-DRIVEN engine.
 *
 * Excel rows like:
 *   keyword=type,  locator=name=username,  data=Admin
 *   keyword=type,  locator=name=password,  data=admin123
 *   keyword=click, locator=css=button[type='submit']
 *
 * The engine looks up the keyword in 'actions' and invokes the lambda.
 *
 * Why this design? It demonstrates:
 *   - Functional interface (IKeywordAction)  -> abstraction
 *   - Map<String, IKeywordAction>            -> registry pattern
 *   - Lambda expressions                     -> behavior parameterisation
 */
public class KeywordEngine {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final Map<String, IKeywordAction> actions = new HashMap<>();

    public KeywordEngine(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        registerKeywords();
    }

    private void registerKeywords() {
        actions.put("openUrl", (d, loc, data) -> d.get(data));

        actions.put("type", (d, loc, data) -> {
            WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(parse(loc)));
            el.clear();
            el.sendKeys(data);
        });

        actions.put("click", (d, loc, data) ->
            wait.until(ExpectedConditions.elementToBeClickable(parse(loc))).click());

        actions.put("verifyText", (d, loc, data) -> {
            String actual = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(parse(loc))).getText();
            if (!actual.contains(data)) {
                throw new FrameworkException("verifyText failed - expected: " + data
                        + " | actual: " + actual);
            }
        });

        actions.put("waitFor", (d, loc, data) -> {
            try { Thread.sleep(Long.parseLong(data)); }
            catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
        });

        actions.put("verifyTitle", (d, loc, data) -> {
            if (!d.getTitle().contains(data))
                throw new FrameworkException("Title mismatch. Got: " + d.getTitle());
        });
    }

    public void execute(String keyword, String locator, String data) {
        IKeywordAction action = actions.get(keyword);
        if (action == null) throw new FrameworkException("Unknown keyword: " + keyword);
        try {
            action.execute(driver, locator, data);
        } catch (Exception e) {
            throw new FrameworkException(
                    "Keyword '" + keyword + "' failed - locator: " + locator + ", data: " + data, e);
        }
    }

    /** "id=foo" / "name=foo" / "css=foo" / "xpath=foo" */
    private By parse(String locator) {
        if (locator == null || !locator.contains("="))
            throw new FrameworkException("Bad locator: " + locator);
        String[] parts = locator.split("=", 2);
        return switch (parts[0].trim().toLowerCase()) {
            case "id"     -> By.id(parts[1]);
            case "name"   -> By.name(parts[1]);
            case "css"    -> By.cssSelector(parts[1]);
            case "xpath"  -> By.xpath(parts[1]);
            case "link"   -> By.linkText(parts[1]);
            default -> throw new FrameworkException("Unsupported locator type: " + parts[0]);
        };
    }
}
