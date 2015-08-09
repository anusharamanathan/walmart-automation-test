package com.walmart.browser.automation.test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

/**
 * To simulate real world users , pauses of 15-30 seconds are used before
 * clicking on items. Also , it allows reasonable time for loading of javascript
 * files.
 * 
 * @author aramanathan
 * 
 */
public class AutomatedTransactionFlow {

	private static final Logger logger = Logger
			.getLogger(AutomatedTransactionFlow.class.getName());

	private WebDriver webDriver;

	private static final int TIMEOUT_SECONDS = 30;

	private static final String DEFAULT_URL = "http://www.walmart.com";

	private List<String> defaultSearchTerms;

	private static final String SEARCH_BAR_CSS = "div.js-searchbar-typeahead-input > span > input";

	private static final String SEARCH_RESULTS_ROOT_CSS = "#searchContent > div > div.search-results";

	private static final String SEARCH_RESULTS_ITEMS_LIST = "//div[@class='mobile-result-items']/div";

	private static final String ADD_TO_CART_BUTTON_ID = "WMItemAddToCartBtn";

	private static final String ADD_ITEM_TO_CART_ITEM_ID_XPATH = "//div[@class=\"js-product-questions\"]";

	private static final String ITEM_ADDED_TO_CART_ITEM_ID_XPATH = "//a[@id=\"CartItemInfo\"]";

	private static final String CHECKOUT_XPATH = "//a[contains(@class,'btn-pac-checkout')]";

	private static final int DEFAULT_ZIP_CODE = 94066;

	private static final String ZIP_CODE_KEY = "zipcode";

	private static final String SIGN_IN_BUTTON_KEY = "COAC0WelAccntSignInBtn";

	private static final String XPATH_SEARCH_DEPARTMENT_PREFIX = "//span[contains(.,\"See all\")]/../span[contains(.,\"";

	private static final String XPATH_SHIPPING = ".//li[contains(.,'shipping')]";

	private static final String ITEM_ID_KEY = "data-us-item-id";

	private static final String LOGIN_USERNAME_KEY = "login-username";

	private static final String LOGIN_PASSWORD_KEY = "login-password";

	private static final String LOGIN_USERNAME_VALUE = "anusharamanathan4@gmail.com";

	private static final String LOGIN_PASSWORD_VALUE = "automation";

	private static final String XPATH_SHIPPING_METHOD = "//h2[contains(.,' Choose shipping')]";

	private static final String XPATH_CART = "//i[contains(@class,'wmicon-cart')]";

	private static final String XPATH_CART_PRESENT="//h3[contains(.,\"Your cart\")]/span";
	
	public static void main(String[] args) throws IOException,
			InterruptedException {
		Random r = new Random();
		AutomatedTransactionFlow automatedTransactionFlow = new AutomatedTransactionFlow();
		automatedTransactionFlow.loadURL(new URL(DEFAULT_URL));

		// Select a random item from the list.
		String searchTerm = automatedTransactionFlow.getDefaultSearchTerms()
				.get(r.nextInt(automatedTransactionFlow.getDefaultSearchTerms()
						.size()));
		logger.log(Level.FINE, "Seach term is :" + searchTerm);

		WebElement searchResultsElement = automatedTransactionFlow
				.performSearch(searchTerm);

		String selectedElement = automatedTransactionFlow
				.selectItemAndAddToCart(searchResultsElement);
		automatedTransactionFlow.performCheckout();
		automatedTransactionFlow.verifyItemInCart(selectedElement);
	}

	/**
	 * Initialize ChromeDriver
	 */
	private void init() {
		logger.setLevel(Level.ALL);
		ConsoleHandler handler = new ConsoleHandler();
		handler.setFormatter(new SimpleFormatter());
		logger.addHandler(handler);
		
		System.setProperty("webdriver.chrome.driver",
				"D:/chrome_driver/bin/chromedriver.exe");
		Map<String, String> mobileEmulation = new HashMap<String, String>();
		mobileEmulation.put("deviceName", "Apple iPhone 6");

		Map<String, Object> chromeOptions = new HashMap<String, Object>();
		chromeOptions.put("mobileEmulation", mobileEmulation);
		DesiredCapabilities capabilities = DesiredCapabilities.chrome();
		capabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions);

		webDriver = new ChromeDriver(capabilities);
	}

	/**
	 * loads a particular base URL into the browser
	 * 
	 * @param url
	 */
	private void loadURL(URL url) {

		logger.log(Level.FINE, "Trying to load URL  '" + url.toString() + "'");
		webDriver.get(url.toString());

		WebElement myDynamicElement = (new WebDriverWait(webDriver,
				TIMEOUT_SECONDS)).until(ExpectedConditions
				.presenceOfElementLocated(By.cssSelector(SEARCH_BAR_CSS)));
		if (myDynamicElement != null)
			logger.log(Level.FINE, "URL '" + url.toString()
					+ "' loaded successfully.");

	}

	/**
	 * 
	 * @param searchTerm
	 * @return WebElement (the table containing the search results grid)
	 */
	private WebElement performSearch(String searchTerm) {

		WebElement searchResultsElement = null;
		WebElement searchBox = webDriver.findElement(By
				.cssSelector(SEARCH_BAR_CSS));
		logger.log(Level.FINE, "Entering " + searchTerm
				+ " into the search bar");
		if (searchBox != null) {

			// Entery query string into search bar
			searchBox.clear();
			searchBox.sendKeys(searchTerm);
			searchBox.submit();
			logger.log(Level.FINE, "Waiting for search results ... ");

			webDriver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
			String searchStringDepartment = XPATH_SEARCH_DEPARTMENT_PREFIX
					+ searchTerm + "\")]";

			List<WebElement> seeAllResultsElement = webDriver.findElements(By
					.xpath(searchStringDepartment));

			if (seeAllResultsElement.size() != 0) {
				// search is for a department
				seeAllResultsElement.get(0).click();
			}

			searchResultsElement = (new WebDriverWait(webDriver,
					TIMEOUT_SECONDS)).until(ExpectedConditions
					.presenceOfElementLocated(By
							.cssSelector(SEARCH_RESULTS_ROOT_CSS)));

			logger.log(Level.FINE, "Search results loaded.");
		}
		return searchResultsElement;
	}

	/**
	 * Select a random item from the search results that is available for
	 * shipping and add it to the cart.
	 * 
	 * @param searchResultsRoot
	 * @return
	 * @throws InterruptedException
	 */
	private String selectItemAndAddToCart(WebElement searchResultsRoot)
			throws InterruptedException {
		// We need this to verify that the item we selected was the one added to
		// the cart.
		String selectedItemID = "";

		if (searchResultsRoot != null) {
			List<WebElement> elements = webDriver.findElements(By
					.xpath(SEARCH_RESULTS_ITEMS_LIST));
			logger.log(Level.FINE, "Elements size is " + elements.size());
			WebElement selectedElement = null;
			do {
				// Select a random item from the cart that is available to ship
				int randomNumber = (int) (Math.random() * elements.size());
				selectedElement = elements.get(randomNumber);

				// Checks if the item is available (and not out of stock)
				if (selectedElement.findElements(By.xpath(XPATH_SHIPPING))
						.size() == 0)
					selectedElement = null;
			} while (selectedElement == null);
			selectedElement.click();

			WebElement addToCartButton = null;
			webDriver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
			List<WebElement> zipCodeCloseButton = webDriver.findElements(By
					.name(ZIP_CODE_KEY));

			// for iPhones , an option to verify if the item is available in the
			// particular zip code
			if (zipCodeCloseButton.size() != 0) {

				WebElement zipCode = zipCodeCloseButton.get(0);
				zipCode.sendKeys(String.valueOf(DEFAULT_ZIP_CODE));
				zipCode.submit();

			} else {
				logger.log(Level.FINER, "Close button is not present.");
			}

			addToCartButton = (new WebDriverWait(webDriver, TIMEOUT_SECONDS))
					.until(ExpectedConditions.presenceOfElementLocated(By
							.id(ADD_TO_CART_BUTTON_ID)));
			WebElement ItemBox = webDriver.findElement(By
					.xpath(ADD_ITEM_TO_CART_ITEM_ID_XPATH));
		
			// this is the item that is added to the cart. verifyItemInCart(itemAddedToCart) verifies that the item added here is the item present in the cart
			selectedItemID = ItemBox.getAttribute(ITEM_ID_KEY);

			addToCartButton.sendKeys(Keys.ENTER);
		}
		return selectedItemID;
	}

	/**
	 * Perform the checkout prrocess
	 */
	private void performCheckout() {

		WebElement checkOutButton = (new WebDriverWait(webDriver,
				TIMEOUT_SECONDS)).until(ExpectedConditions
				.elementToBeClickable(By.xpath(CHECKOUT_XPATH)));

		checkOutButton.sendKeys(Keys.ENTER);

		WebElement usernameTextField = (new WebDriverWait(webDriver,
				TIMEOUT_SECONDS)).until(ExpectedConditions
				.presenceOfElementLocated(By.name(LOGIN_USERNAME_KEY)));

		WebElement passwordTextField = (new WebDriverWait(webDriver,
				TIMEOUT_SECONDS)).until(ExpectedConditions
				.presenceOfElementLocated(By.name(LOGIN_PASSWORD_KEY)));

		// Enter username and password to sign in
		usernameTextField.clear();
		usernameTextField.sendKeys(LOGIN_USERNAME_VALUE);

		passwordTextField.clear();
		passwordTextField.sendKeys(LOGIN_PASSWORD_VALUE);

		WebElement signInButton = (new WebDriverWait(webDriver, TIMEOUT_SECONDS))
				.until(ExpectedConditions.presenceOfElementLocated(By
						.id(SIGN_IN_BUTTON_KEY)));
		signInButton.click();
	}

	/**
	 * verify that the item added previously is the one(and only item) present in the cart
	 * @param itemAddedToCart
	 */
	private void verifyItemInCart(String itemAddedToCart) {
		webDriver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
		WebElement shippingElement = (new WebDriverWait(webDriver,
				TIMEOUT_SECONDS)).until(ExpectedConditions
				.presenceOfElementLocated(By.xpath(XPATH_SHIPPING_METHOD)));

		if (shippingElement != null) {
			WebElement cartButton = (new WebDriverWait(webDriver,
					TIMEOUT_SECONDS)).until(ExpectedConditions
					.elementToBeClickable(By.xpath(XPATH_CART)));
			cartButton.click();
			WebElement ItemBox = (new WebDriverWait(webDriver, TIMEOUT_SECONDS))
					.until(ExpectedConditions.elementToBeClickable(By
							.xpath(ITEM_ADDED_TO_CART_ITEM_ID_XPATH)));
			
			// verify that item in cart is the selected one
			String ItemInCart = ItemBox.getAttribute(ITEM_ID_KEY);
			if (ItemInCart.compareTo(itemAddedToCart) == 0)
				logger.log(Level.ALL,"Validated that the item added to the cart is the selected item");
			else
				logger.log(Level.SEVERE,"Item added to the cart was NOT the one selected!");

			// verify that the only item in the cart is the selected one
			WebElement itemQuantity = (new WebDriverWait(webDriver,
					TIMEOUT_SECONDS)).until(ExpectedConditions
					.elementToBeClickable(By
							.xpath(XPATH_CART_PRESENT)));
			if (itemQuantity.getText().compareTo("1 item") == 0)
				logger.log(Level.ALL,"Validated that the item added to the cart is the selected item and that it is the only item in the cart");
			else
				logger.log(Level.SEVERE,"Cart has multiple items!");
		}
	}

	public WebDriver getWebDriver() {
		return webDriver;
	}

	public void setWebDriver(WebDriver webDriver) {
		this.webDriver = webDriver;
	}

	public List<String> getDefaultSearchTerms() {
		return defaultSearchTerms;
	}

	public void setDefaultSearchTerms(List<String> defaultSearchTerms) {
		this.defaultSearchTerms = defaultSearchTerms;
	}

	public AutomatedTransactionFlow() throws IOException {
		init();
		defaultSearchTerms = new ArrayList<String>();
		File file = new File(getClass().getClassLoader().getResource("")
				.getPath());
		String pathToFile = file.getAbsolutePath()
				+ "/default_search_terms.txt";
		defaultSearchTerms = Files.readLines(new File(pathToFile),
				Charsets.UTF_8);
	}

}
