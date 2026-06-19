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
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TiendaE2ETest {

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
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", element);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    @Test
    @DisplayName("CP-20: Listar tiendas correctamente")
    void listarTiendas() {
        driver.get("http://localhost:" + port + "/tiendas");
        wait.until(ExpectedConditions.urlContains("/tiendas"));

        assertTrue(driver.getCurrentUrl().contains("/tiendas"));
        assertTrue(driver.getPageSource().toLowerCase().contains("tienda"));
    }

    @Test
    @DisplayName("CP-21: Crear nueva tienda exitosamente")
    void crearTienda() {
        driver.get("http://localhost:" + port + "/tiendas/nuevo");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("nombre")));

        String nombreUnico = "Tienda Test " + System.currentTimeMillis();

        driver.findElement(By.id("nombre")).sendKeys(nombreUnico);
        driver.findElement(By.id("direccion")).sendKeys("Direccion Test 123");
        driver.findElement(By.id("telefono")).sendKeys("999888777");
        driver.findElement(By.id("estado")).sendKeys("Activa");
        driver.findElement(BOTON_GUARDAR).click();

        wait.until(ExpectedConditions.urlContains("/tiendas"));
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), nombreUnico));

        assertTrue(driver.getCurrentUrl().contains("/tiendas"));
        assertTrue(driver.getPageSource().contains(nombreUnico),
                "Se esperaba ver la nueva tienda en la lista");
    }

    @Test
    @DisplayName("CP-22: Editar una tienda existente")
    void editarTienda() {
        driver.get("http://localhost:" + port + "/tiendas/nuevo");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("nombre")));

        String nombreOriginal = "TiendaEditar " + System.currentTimeMillis();
        driver.findElement(By.id("nombre")).sendKeys(nombreOriginal);
        driver.findElement(By.id("direccion")).sendKeys("Direccion Original");
        driver.findElement(By.id("telefono")).sendKeys("111222333");
        driver.findElement(By.id("estado")).sendKeys("Activa");
        driver.findElement(BOTON_GUARDAR).click();

        wait.until(ExpectedConditions.urlContains("/tiendas"));
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), nombreOriginal));
        assertTrue(driver.getPageSource().contains(nombreOriginal));

        List<WebElement> editarLinks = driver.findElements(By.cssSelector("a[href*='/tiendas/editar/']"));
        assertTrue(editarLinks.size() > 0, "Debe existir al menos un enlace de editar");

        jsClick(editarLinks.get(editarLinks.size() - 1));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("nombre")));

        WebElement nombreInput = driver.findElement(By.id("nombre"));
        nombreInput.clear();
        String nombreEditado = "TiendaEditada " + System.currentTimeMillis();
        nombreInput.sendKeys(nombreEditado);

        driver.findElement(BOTON_GUARDAR).click();
        wait.until(ExpectedConditions.urlContains("/tiendas"));
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), nombreEditado));

        assertTrue(driver.getPageSource().contains(nombreEditado),
                "Se esperaba ver la tienda con el nombre editado");
    }

    @Test
    @DisplayName("CP-23: Eliminar una tienda existente")
    void eliminarTienda() {
        driver.get("http://localhost:" + port + "/tiendas/nuevo");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("nombre")));

        String nombreEliminar = "TiendaEliminar " + System.currentTimeMillis();
        driver.findElement(By.id("nombre")).sendKeys(nombreEliminar);
        driver.findElement(By.id("direccion")).sendKeys("Direccion Eliminar");
        driver.findElement(By.id("telefono")).sendKeys("444555666");
        driver.findElement(By.id("estado")).sendKeys("Activa");
        driver.findElement(BOTON_GUARDAR).click();

        wait.until(ExpectedConditions.urlContains("/tiendas"));
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), nombreEliminar));
        assertTrue(driver.getPageSource().contains(nombreEliminar));

        List<WebElement> eliminarLinks = driver.findElements(By.cssSelector("a[href*='/tiendas/eliminar/']"));
        assertTrue(eliminarLinks.size() > 0, "Debe existir al menos un enlace de eliminar");

        jsClick(eliminarLinks.get(eliminarLinks.size() - 1));

        wait.until(ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().accept();

        wait.until(ExpectedConditions.urlContains("/tiendas"));

        assertTrue(driver.getCurrentUrl().contains("/tiendas"));
        assertTrue(!driver.getPageSource().contains(nombreEliminar),
                "La tienda eliminada no deberia seguir en la lista");
    }
}
