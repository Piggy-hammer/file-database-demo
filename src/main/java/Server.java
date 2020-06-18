import dao.TextDao;
import io.javalin.Javalin;
import io.swagger.v3.oas.models.info.Info;
import io.javalin.plugin.openapi.OpenApiOptions;
import io.javalin.plugin.openapi.OpenApiPlugin;
import io.javalin.plugin.openapi.ui.ReDocOptions;
import io.javalin.plugin.openapi.ui.SwaggerOptions;
import org.sql2o.Sql2o;
import service.TextService;

/**
 * This class is the server controller which can invoke different method in
 * textService with command from the client with Javalin frame and sent the
 * response string back
 *
 * @see TextService The analyer of the server system
 */
public class Server {
    public static void main(String[] args) throws ClassNotFoundException {
        //TODO:connect database


        TextDao dao = new TextDao();
        TextService service = new TextService(dao);

        Javalin app = Javalin.create(config -> {
            config.registerPlugin(getConfiguredOpenApiPlugin());
        }).start(7001);
        app.get("/", ctx -> ctx.result("Welcome to RESTful Corpus Platform"));
        // handle list
        app.get("/files/list",service::handleList);
        // handle exist
        app.get("/files/:md5/exists", service::handleExists);
        // handle upload
        app.post("/files/:md5", service::handleUpload);
        // handle compare
        app.get("/files/:md51/compare/:md52", service::handleCompare);
        // handle download
        app.get("/files/:md5", service::handleDownload);
        //handle delete
        app.delete("/files/:md5/delete", service::handleDelete);
    }


    private static OpenApiPlugin getConfiguredOpenApiPlugin() {
        Info info = new Info().version("1.0").description("RESTful Corpus Platform API");
        OpenApiOptions options = new OpenApiOptions(info)
                .activateAnnotationScanningFor("cn.edu.sustech.java2.RESTfulCorpusPlatform")
                .path("/swagger-docs") // endpoint for OpenAPI json
                .swagger(new SwaggerOptions("/swagger-ui")) // endpoint for swagger-ui
                .reDoc(new ReDocOptions("/redoc")); // endpoint for redoc
        return new OpenApiPlugin(options);
    }

}