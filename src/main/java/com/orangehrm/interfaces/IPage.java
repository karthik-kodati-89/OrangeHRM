package com.orangehrm.interfaces;

/**
 * Contract every page object must honour.
 * INTERFACE = pure abstraction. Pages 'implement' this and BasePage at once
 * (Java multiple-inheritance via interfaces).
 */
public interface IPage {
    String getPageTitle();
    String getPageUrl();
    boolean isAt();
}
