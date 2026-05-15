package com.orangehrm.base;

import com.orangehrm.exceptions.FrameworkException;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

/**
 * Abstract Base Page - demonstrates DATA ABSTRACTION via abstract class.
 * All page classes MUST extend this and provide their own isPageLoaded() impl.
 *
 * OOPS Concepts in action:
 *  - Abstract Class (Abstraction)
 *  - Constructor + 'this' keyword
 *  - Method Overloading (Static Polymorphism)
 *  - Instance vs Static vs Local variables (variable scopes)
 *  - Inheritance (subclasses use 'super')
 */
public abstract class BasePage {

    // INSTANCE variables (per-object scope) - protected for child access
    protected WebDriver driver;
    protected WebDriverWait wait;
    protected JavascriptExecutor js;

    // STATIC variable (class-level scope) - shared across all instances
    private static final int DEFAULT_TIMEOUT = 20;

    // Constructor - 'this' keyword resolves shadowing between
    // parameter and instance variable
    public BasePage(WebDriver driver) {
        if (driver == null) {
            throw new FrameworkException("WebDriver cannot be null in BasePage");
        }
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT));
        this.js = (JavascriptExecutor) driver;
    }

    // ==================== ABSTRACT METHOD ====================
    // Forces every subclass to define its own page-loaded check.
    public abstract boolean isPageLoaded();

    // ============= STATIC POLYMORPHISM (Method Overloading) =============
    // Same method name 'click' with different parameter lists.

    public void click(By locator) {
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
        element.click();
    }

    public void click(WebElement element) {
        wait.until(ExpectedConditions.elementToBeClickable(element)).click();
    }

    public void click(By locator, int customTimeoutSec) {
        // LOCAL variable - method scope only
        WebDriverWait localWait = new WebDriverWait(driver, Duration.ofSeconds(customTimeoutSec));
        localWait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    // Overloaded 'type' methods
    public void type(By locator, String text) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        el.clear();
        el.sendKeys(text);
    }

    public void type(WebElement element, String text) {
        wait.until(ExpectedConditions.visibilityOf(element));
        element.clear();
        element.sendKeys(text);
    }

    // ==================== COMMON UTILITIES ====================
    public String getText(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).getText();
    }

    public boolean isDisplayed(By locator) {
        try {
            return driver.findElement(locator).isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public List<WebElement> getElements(By locator) {
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(locator));
        return driver.findElements(locator);
    }

    public void selectByVisibleText(By locator, String visibleText) {
        WebElement dd = wait.until(ExpectedConditions.elementToBeClickable(locator));
        new Select(dd).selectByVisibleText(visibleText);
    }

    public void scrollIntoView(WebElement element) {
        js.executeScript("arguments[0].scrollIntoView({block:'center'});", element);
    }

    // ==================== SHADOW DOM SUPPORT ====================
    /**
     * Pierce one or more shadow roots.
     * Pass selectors top-down: outer host -> inner host -> ... -> target.
     */
    public WebElement findInShadow(String... cssSelectors) {
        if (cssSelectors == null || cssSelectors.length == 0) {
            throw new FrameworkException("At least one CSS selector required for shadow DOM");
        }
        WebElement current = driver.findElement(By.cssSelector(cssSelectors[0]));
        for (int i = 1; i < cssSelectors.length; i++) {
            SearchContext shadowRoot = current.getShadowRoot();
            current = shadowRoot.findElement(By.cssSelector(cssSelectors[i]));
        }
        return current;
    }

    // ==================== IFRAME SUPPORT ====================
    public void switchToFrame(By frameLocator) {
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(frameLocator));
    }

    public void switchToFrame(int index) {
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(index));
    }

    public void switchToDefaultContent() {
        driver.switchTo().defaultContent();
    }
}
