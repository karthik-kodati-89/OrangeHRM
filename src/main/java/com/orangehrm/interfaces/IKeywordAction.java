package com.orangehrm.interfaces;

import org.openqa.selenium.WebDriver;

/**
 * Functional interface for the Keyword-Driven engine.
 * Each registered keyword is an implementation of execute().
 */
@FunctionalInterface
public interface IKeywordAction {
    void execute(WebDriver driver, String locator, String data);
}
