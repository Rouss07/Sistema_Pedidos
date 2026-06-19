package com.espigapedidos.espigapedidos.e2e;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LoginE2ETest {

    @LocalServerPort
    private int port;

    private WebDriver driver;

    @BeforeEach
    void setUp() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--window-size=1280,800");
        driver = new ChromeDriver(options);
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @DisplayName("CP-01: Login exitoso con credenciales validas (admin)")
    void loginExitoso() {
        driver.get("http://localhost:" + port + "/login");

        driver.findElement(By.name("username")).sendKeys("admin");
        driver.findElement(By.name("password")).sendKeys("1234");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // Tras login correcto, redirige a la pagina principal (dashboard)
        String url = driver.getCurrentUrl();
        assertTrue(url.equals("http://localhost:" + port + "/"),
                "Se esperaba redireccion a la pagina principal, pero la URL fue: " + url);

        assertTrue(driver.getPageSource().contains("Dashboard"),
                "Se esperaba ver el Dashboard tras iniciar sesion");
    }

    @Test
    @DisplayName("CP-02: Login fallido con credenciales invalidas")
    void loginFallido() {
        driver.get("http://localhost:" + port + "/login");

        driver.findElement(By.name("username")).sendKeys("usuario_invalido");
        driver.findElement(By.name("password")).sendKeys("clave_mala");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // Tras login fallido, Spring Security redirige a /login?error
        assertTrue(driver.getCurrentUrl().contains("error"),
                "Se esperaba la URL con parametro 'error' tras login fallido");

        assertTrue(driver.getPageSource().contains("incorrectos"),
                "Se esperaba ver el mensaje de error de credenciales incorrectas");
    }

    @Test
    @DisplayName("CP-03: Acceso a pagina protegida sin login redirige a /login")
    void accesoSinLoginRedirigeALogin() {
        driver.get("http://localhost:" + port + "/productos");

        assertTrue(driver.getCurrentUrl().contains("/login"),
                "Se esperaba redireccion a /login al acceder sin autenticacion");
    }
}
