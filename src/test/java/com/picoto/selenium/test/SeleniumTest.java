package com.picoto.selenium.test;

import static com.picoto.selenium.test.KeyManagerFactoryProviders.usingPKCS12File;
import static com.picoto.selenium.test.KeyManagerFactoryProviders.usingPemKeyPair;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.time.Duration;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.interactions.Actions;

public class SeleniumTest {

	private static WebDriver driver;

	@BeforeClass
	public static void init() {
		// System.setProperty("webdriver.firefox.driver",
		// "/home/jmgodino/Selenium/drivers/geckodriver");

		File clientCertFile = new File("/home/jmgodino/Selenium/clientCert.p12");
		char[] clientCertPassword = "xxx".toCharArray();
		File serverCertFile = new File("src/test/java/server.crt");
		File serverKeyFile = new File("src/test/java/server.key");

		org.littleshoot.proxy.impl.DefaultHttpProxyServer.bootstrap().withName(clientCertFile.getName()).withPort(5555)
				.withAllowLocalOnly(true)
				.withManInTheMiddle(
						new MutualAuthenticationCapableMitmManager(usingPKCS12File(clientCertFile, clientCertPassword),
								usingPemKeyPair(serverCertFile, serverKeyFile)))
				.start();

		Proxy proxy = new Proxy();
		proxy.setSslProxy("127.0.0.1:5555");
		proxy.setNoProxy("<-loopback>"); // overwrite the default no-proxy for localhost, 127.0.0.1

		FirefoxOptions options = new FirefoxOptions();
		options.setProxy(proxy);

		driver = new FirefoxDriver(options);
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(20));
	}

	public void test1() {
		driver.get("https://sede.agenciatributaria.gob.es/");
		driver.manage().window().setSize(new Dimension(1319, 857));
		driver.findElement(By.id("input-buscador-menu")).click();
		driver.findElement(By.id("input-buscador-menu")).sendKeys("modelo 111");
		driver.findElement(By.id("btn-buscador-menu")).click();
		{
			WebElement element = driver.findElement(By.id("btn-buscador-menu"));
			Actions builder = new Actions(driver);
			builder.moveToElement(element).perform();
		}
		{
			WebElement element = driver.findElement(By.tagName("body"));
			Actions builder = new Actions(driver);
			builder.moveToElement(element, 0, 0).perform();
		}
		assertThat(driver.findElement(By.linkText("Modelo 111 - Presentación electrónica del modelo 111")).getText(),
				is("Modelo 111 - Presentación electrónica del modelo 111"));
	}

	@Test
	public void test2() {
		driver.get("https://prewww1.aeat.es/wlpl/OVPP-PAGO/AutoliquidacionesCuenta");
		driver.manage().window().setSize(new Dimension(1280, 768));
		{
			WebElement dropdown = driver.findElement(By.id("idmod"));
			dropdown.findElement(By.xpath("//option[. = '111 - IRPF. Retenciones e Ingresos a cuenta']")).click();
		}
		driver.findElement(By.cssSelector("option:nth-child(7)")).click();
		driver.findElement(By.id("idejf")).click();
		driver.findElement(By.id("idejf")).sendKeys("2023");
		driver.findElement(By.id("idper")).click();
		{
			WebElement dropdown = driver.findElement(By.id("idper"));
			dropdown.findElement(By.xpath("//option[. = '1T - Primer Trimestre']")).click();
		}
		driver.findElement(By.cssSelector("#idper > option:nth-child(13)")).click();
		driver.findElement(By.id("idnif")).click();
		driver.findElement(By.id("idnif")).sendKeys("89890001K");
		driver.findElement(By.id("idape")).sendKeys("TELEMATICAS UNO CERT");
		driver.findElement(By.id("idimp")).click();
		driver.findElement(By.id("idimp")).sendKeys(Keys.CONTROL + "a");
		driver.findElement(By.id("idimp")).sendKeys("3");
		driver.findElement(By.id("idiban")).click();
		driver.findElement(By.id("idiban")).sendKeys("ES7921000813610123456789");
		driver.findElement(By.id("CONF")).click();
		driver.findElement(By.cssSelector(".conborde:nth-child(18) li:nth-child(1)")).click();
		assertThat(
				driver.findElement(By.cssSelector(".conborde:nth-child(18) li:nth-child(1) > .notraducir")).getText(),
				is("111 - IRPF. Retenciones e Ingresos a cuenta"));
		driver.findElement(By.cssSelector(".conborde:nth-child(21) li:nth-child(1) > .notraducir")).click();
		driver.findElement(By.cssSelector(".conborde:nth-child(21) li:nth-child(1) > .notraducir")).click();
		{
			WebElement element = driver
					.findElement(By.cssSelector(".conborde:nth-child(21) li:nth-child(1) > .notraducir"));
			Actions builder = new Actions(driver);
			builder.doubleClick(element).perform();
		}
		assertThat(
				driver.findElement(By.cssSelector(".conborde:nth-child(21) li:nth-child(1) > .notraducir")).getText(),
				is("89890001K"));
		driver.findElement(By.id("check")).click();
		driver.findElement(By.id("CONF")).click();
		driver.findElement(By.cssSelector("#idUlAlertas > li")).click();
		assertThat(driver.findElement(By.cssSelector("#idUlAlertas > li")).getText(), startsWith(
				"La operación no se ha podido realizar. Respuesta de la Entidad Financiera: [99] Error técnico."));
		driver.findElement(By.cssSelector(".AEAT_bloque_errores:nth-child(6)")).click();
			assertThat(driver.findElement(By.cssSelector(".AEAT_bloque_errores:nth-child(6)")).getText(), is(
				"Por causas ajenas a la AEAT, las entidades [Banco con problemas] no están presentado sus servicios de pago telemático correctamente. Desde la AEAT se les han notificado la incidencia y estamos a la espera de que las entidades afectadas solucionen el problema. Lamentamos la molestias que este problema pueda ocasionar. Puede optar por realizar el pago con una cuenta o tarjeta de otra Entidad Colaboradora de la AEAT."));
	}

	@AfterClass
	public static void closeBrowser() {
		driver.quit();
	}

}
