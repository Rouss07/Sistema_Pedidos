package com.espigapedidos.espigapedidos.e2e;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PedidoE2ETest {

    @LocalServerPort
    private int port;

    private WebDriver driver;
    private WebDriverWait wait;

    private static final By BOTON_GUARDAR =
            By.cssSelector("form[action*='/guardar'] button[type='submit']");

    private static final By BOTON_LOGIN =
            By.cssSelector("form button[type='submit']");

    @BeforeEach
    void setUp() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--window-size=1280,800");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        login();
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    private void login() {
        driver.get("http://localhost:" + port + "/login");

        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));

        driver.findElement(By.name("username")).sendKeys("admin");
        driver.findElement(By.name("password")).sendKeys("1234");
        driver.findElement(BOTON_LOGIN).click();

        wait.until(ExpectedConditions.urlToBe("http://localhost:" + port + "/"));
    }

    private void jsClick(WebElement element) {
        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].scrollIntoView({block:'center'});", element);

        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].click();", element);
    }

    @Test
    @DisplayName("CP-40: Listar pedidos correctamente")
    void listarPedidos() {

        driver.get("http://localhost:" + port + "/pedidos");

        wait.until(ExpectedConditions.urlContains("/pedidos"));

        assertTrue(driver.getCurrentUrl().contains("/pedidos"));
        assertTrue(driver.getPageSource().contains("Lista de Pedidos"));
    }

    @Test
    @DisplayName("CP-41: Crear pedido exitosamente")
    void crearPedido() {

        driver.get("http://localhost:" + port + "/pedidos/nuevo");

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("estado")));

        driver.findElement(By.id("estado"))
                .sendKeys("Pendiente Selenium");

        Select tiendaSelect =
                new Select(driver.findElement(By.name("tienda")));

        tiendaSelect.selectByIndex(1);

        driver.findElement(BOTON_GUARDAR).click();

        wait.until(ExpectedConditions.urlContains("/pedidos"));

        assertTrue(driver.getCurrentUrl().contains("/pedidos"));
    }

    @Test
    @DisplayName("CP-42: Editar pedido existente")
    void editarPedido() {

        driver.get("http://localhost:" + port + "/pedidos");

        List<WebElement> editarLinks =
                driver.findElements(By.cssSelector("a[href*='/pedidos/editar/']"));

        assertTrue(editarLinks.size() > 0);

        jsClick(editarLinks.get(editarLinks.size() - 1));

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("estado")));

        WebElement estadoInput =
                driver.findElement(By.id("estado"));

        estadoInput.clear();

        String nuevoEstado =
                "Entregado Selenium";

        estadoInput.sendKeys(nuevoEstado);

        driver.findElement(BOTON_GUARDAR).click();

        wait.until(ExpectedConditions.urlContains("/pedidos"));

        assertTrue(driver.getCurrentUrl().contains("/pedidos"));
    }

    @Test
    @DisplayName("CP-43: Eliminar pedido existente")
    void eliminarPedido() {

        driver.get("http://localhost:" + port + "/pedidos");

        List<WebElement> eliminarLinks =
                driver.findElements(By.cssSelector("a[href*='/pedidos/eliminar/']"));

        assertTrue(eliminarLinks.size() > 0);

        jsClick(eliminarLinks.get(eliminarLinks.size() - 1));

        wait.until(ExpectedConditions.alertIsPresent());

        driver.switchTo().alert().accept();

        wait.until(ExpectedConditions.urlContains("/pedidos"));

        assertTrue(driver.getCurrentUrl().contains("/pedidos"));
    }
}
