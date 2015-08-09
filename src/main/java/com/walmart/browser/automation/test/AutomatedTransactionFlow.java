package com.walmart.browser.automation.test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class AutomatedTransactionFlow {

	private static final Logger logger = Logger
			.getLogger(AutomatedTransactionFlow.class.getName());

	private WebDriver webDriver;

	private static final String DEFAULT_URL = "http://mobile.walmart.com";

	private List<String> defaultSearchTerms;

	private static final String SEARCH_BAR_CSS = "div.js-searchbar-typeahead-input > span > input";

	private static final String SEARCH_RESULTS_ROOT_CSS = "#searchContent > div > div.search-results";
	
	private static final String SEARCH_RESULTS_ITEMS_LIST_CSS = "#searchContent > div > div.search-results > div.mobile-result-items ";

	public static void main(String[] args) throws IOException {
		Random r = new Random();
		AutomatedTransactionFlow automatedTransactionFlow = new AutomatedTransactionFlow();
		automatedTransactionFlow.loadURL(new URL(DEFAULT_URL));
		String searchTerm = automatedTransactionFlow.getDefaultSearchTerms()
				.get(r.nextInt(automatedTransactionFlow.getDefaultSearchTerms()
						.size()));
		logger.log(Level.FINE, "Seach term is :" + searchTerm);
		WebElement searchResultsElement = automatedTransactionFlow
				.performSearch(searchTerm);
		
		automatedTransactionFlow.selectRandomElementFromSearchResults(searchResultsElement);

	}

	private void init() {
		logger.log(Level.INFO, "Initializing ChromeDriver");
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

	private void loadURL(URL url) {
		logger.log(Level.FINE, "Trying to load URL  '" + url.toString() + "'");
		webDriver.get(url.toString());
		WebElement myDynamicElement = (new WebDriverWait(webDriver, 10))
				.until(ExpectedConditions.presenceOfElementLocated(By
						.cssSelector(SEARCH_BAR_CSS)));
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
			searchBox.clear();
			searchBox.sendKeys(searchTerm);
			searchBox.submit();
			logger.log(Level.FINE, "Waiting for search results ... ");

			searchResultsElement = (new WebDriverWait(webDriver, 10))
					.until(ExpectedConditions.presenceOfElementLocated(By
							.cssSelector(SEARCH_RESULTS_ROOT_CSS)));

			logger.log(Level.FINE, "Search results loaded.");
		}
		return searchResultsElement;
	}

	private void selectRandomElementFromSearchResults(
			WebElement searchResultsRoot) {
		WebElement selection = null;
		if (searchResultsRoot != null) {
			List<WebElement> elements = webDriver
					.findElements(By
							.cssSelector(SEARCH_RESULTS_ITEMS_LIST_CSS));

			int resultsOnPage = elements.size();
			System.out.println(resultsOnPage);
			Random r = new Random();
			int randomSelection = r.nextInt(resultsOnPage);

			elements.get(randomSelection).click();

			WebElement addToCartButton = (new WebDriverWait(webDriver, 10))
					.until(ExpectedConditions.presenceOfElementLocated(By
							.xpath("//div[starts-with(@class,'js-product-add-to-cart-row')]/button[contains(text(),'Add to Cart')]")));

			if (addToCartButton != null) {
				addToCartButton.click();
			}

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
