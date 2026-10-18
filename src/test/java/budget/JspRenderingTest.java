package budget;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.*;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
class JspRenderingTest {
    @TempDir static Path dataRoot;
    @DynamicPropertySource static void storage(DynamicPropertyRegistry registry) { registry.add("budget.data-dir",()->dataRoot.toString()); }
    @LocalServerPort int port;
    HttpClient client=HttpClient.newBuilder().cookieHandler(new CookieManager(null,CookiePolicy.ACCEPT_ALL)).followRedirects(HttpClient.Redirect.NORMAL).build();
    HttpResponse<String> get(String path) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+port+path)).GET().build(),HttpResponse.BodyHandlers.ofString());
    }
    HttpResponse<String> post(String path,Map<String,String> values) throws Exception {
        String body=values.entrySet().stream().map(e->URLEncoder.encode(e.getKey(),StandardCharsets.UTF_8)+"="+URLEncoder.encode(e.getValue(),StandardCharsets.UTF_8)).reduce((a,b)->a+"&"+b).orElse("");
        return client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+port+path)).header("Content-Type","application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build(),HttpResponse.BodyHandlers.ofString());
    }
    @Test void staticJspTextUsesUtf8() throws Exception {
        String html=get("/profiles").body();
        assertTrue(html.contains("&nbsp; \u00b7 &nbsp;"),"Footer separator must be decoded as UTF-8");
        assertFalse(html.contains("\u00c2\u00b7"),"Do not display mojibake");
    }
    @Test void realJspPagesRenderSavedUnicodeAndEscapeMarkup() throws Exception {
        var selection=get("/profiles");
        assertEquals(200,selection.statusCode());
        Matcher match=Pattern.compile("name=\"_csrf\" value=\"([^\"]+)\"").matcher(selection.body());
        assertTrue(match.find(),"JSP must render the session CSRF token");
        String token=match.group(1);
        assertEquals(200,post("/profiles",Map.of("_csrf",token,"displayName","Zoë Demo")).statusCode());
        assertEquals(200,post("/income",Map.of("_csrf",token,"month","2026-10","income","3000")).statusCode());
        assertEquals(200,post("/transactions",Map.of("_csrf",token,"month","2026-10","date","2026-10-17","description","<script>alert(1)</script>","amount","3.50","category","WANTS")).statusCode());
        var dashboard=get("/?month=2026-10");
        assertEquals(200,dashboard.statusCode());
        assertTrue(dashboard.body().contains("Zoë Demo"));
        assertTrue(dashboard.body().contains("&lt;script&gt;"));
        assertFalse(dashboard.body().contains("<script>alert(1)</script>"));
        assertTrue(dashboard.body().contains("1,500.00"));
        var help=get("/help"); assertEquals(200,help.statusCode());
        assertTrue(help.body().contains("50/30/20"));
    }
}
