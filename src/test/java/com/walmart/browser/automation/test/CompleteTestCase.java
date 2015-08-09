package com.walmart.browser.automation.test;

import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

public class CompleteTestCase {
	private WebDriver driver;
	private String baseUrl;
	private StringBuffer verificationErrors = new StringBuffer();

	@Before
	public void setUp() throws Exception {
		Map<String, String> mobileEmulation = new HashMap<String, String>();
		mobileEmulation.put("deviceName", "Apple iPhone 6");

		Map<String, Object> chromeOptions = new HashMap<String, Object>();
		chromeOptions.put("mobileEmulation", mobileEmulation);
		DesiredCapabilities capabilities = DesiredCapabilities.chrome();
		capabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions);
		WebDriver driver = new ChromeDriver(capabilities);
		baseUrl = "http://mobile.walmart.com";
		driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
	}

	@Test
	public void testCompleteTestCase() throws Exception {
		driver.get(baseUrl);
		driver.findElement(By.name("query")).clear();
		driver.findElement(By.name("query")).sendKeys("Socks");
		driver.findElement(By.cssSelector("a.product-title > p")).click();
		driver.findElement(By.id("WMItemAddToCartBtn")).click();
		driver.findElement(By.id("PACViewCartBtn")).click();
		driver.findElement(By.linkText("Sign In")).click();
		driver.findElement(By.id("login-username")).clear();
		driver.findElement(By.id("login-username")).sendKeys(
				"reneartois07302015");
		driver.findElement(By.id("login-username")).clear();
		driver.findElement(By.id("login-username")).sendKeys(
				"reneartois07302015@gmail.com");
		driver.findElement(By.id("login-password")).clear();
		driver.findElement(By.id("login-password")).sendKeys("AlloAllo15");
		driver.findElement(By.xpath("(//button[@type='submit'])[3]")).click();
		driver.findElement(By.cssSelector("i.wmicon.wmicon-cart")).click();
		driver.findElement(By.xpath("(//button[@id='CartRemItemBtn'])[3]"))
				.click();
		driver.findElement(
				By.cssSelector("button.searchbar-submit.js-searchbar-submit"))
				.click();
	}

	@After
	public void tearDown() throws Exception {
		driver.quit();
		String verificationErrorString = verificationErrors.toString();
		if (!"".equals(verificationErrorString)) {
			fail(verificationErrorString);
		}
	}
}
