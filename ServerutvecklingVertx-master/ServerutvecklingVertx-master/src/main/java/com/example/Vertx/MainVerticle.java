  package com.example.Vertx;

  import com.example.objects.ImageUserDataSet;
  import io.vertx.core.AbstractVerticle;
  import io.vertx.core.MultiMap;
  import io.vertx.core.Promise;
  import io.vertx.core.Vertx;
  import io.vertx.core.http.HttpMethod;
  import io.vertx.core.http.HttpServerResponse;
  import io.vertx.core.json.Json;
  import io.vertx.core.json.JsonArray;
  import io.vertx.core.json.JsonObject;
  import io.vertx.ext.web.Route;
  import io.vertx.ext.web.Router;
  import io.vertx.ext.web.RoutingContext;
  import io.vertx.ext.web.client.WebClient;
  import io.vertx.ext.web.handler.BodyHandler;
  import io.vertx.ext.web.handler.CorsHandler;
  import io.vertx.ext.web.handler.StaticHandler;
  import io.vertx.mysqlclient.MySQLConnectOptions;
  import io.vertx.mysqlclient.MySQLPool;
  import io.vertx.sqlclient.PoolOptions;
  import io.vertx.sqlclient.Row;
  import io.vertx.sqlclient.RowSet;

  import java.util.ArrayList;
  import java.util.Arrays;
  import java.util.Collections;
  import java.util.List;

  public class MainVerticle extends AbstractVerticle {

    private String uuid;
    private MySQLPool client;
    private  PoolOptions poolOptions;
    private MySQLConnectOptions connectOptions;
    private JsonArray jsonArray;
    private Route requestedRoute;

    @Override
    public void start() throws Exception {
      // Create a Router
      super.start();
      Router router = Router.router(vertx);
      router.route().handler(CorsHandler.create("*")
        .allowedMethod(HttpMethod.GET)
        .allowedMethod(HttpMethod.POST)
        .allowedHeader("Access-Control-Request-Method")
        .allowedHeader("Access-Control-Allow-Credentials")
        .allowedHeader("Access-Control-Allow-Origin")
        .allowedHeader("Access-Control-Allow-Headers")
        .allowedHeader("Content-Type")
        .allowCredentials(true));
      Route messageRoute = router.get("/api/message");
      messageRoute.handler(rc -> rc.response().end("hello"));

      // Mount the handler for all incoming requests at every path and HTTP method
      Route uuidRoute = router.get("/api/test/:uuid");
      uuidRoute.handler(request -> {

        uuid = request.request().getParam("uuid");
        requestedRoute = uuidRoute;

        // This handler gets called for each request that arrives on the server
        HttpServerResponse response = request.response();
        response.putHeader("content-type", "text/plain");
        //dbCall(uuidRoute);

        // Create the client pool
        client = MySQLPool.pool(vertx, connectOptions, poolOptions);
        //JsonArray jsonArray = new JsonArray();
        // A simple query
        client
          //.query("SELECT * FROM canvas_image")
          .query("select creator, day_of_the_week, count(*) AS count from canvas_image where creator = '" + uuid + "' group by creator, day_of_the_week order by day_of_the_week;")
          .execute(ar -> {

            if (ar.succeeded()) {
              System.out.println("SUCCESS: " + uuid);
              RowSet<Row> result = ar.result();
              jsonArray.clear();
              ArrayList<ImageUserDataSet> list = new ArrayList<>();
              ArrayList<Integer> countList = new ArrayList<>();
              for(Row row: result) {
                ImageUserDataSet temp = new ImageUserDataSet();
                temp.setUuid(row.getString("creator"));
                temp.setCount(row.getInteger("count"));
                temp.setDay_of_the_week(row.getInteger("day_of_the_week"));
                System.out.println(row.getString("creator") + " " + row.getInteger("count") +" " + row.getInteger("day_of_the_week"));
                list.add(temp);
              }
              System.out.println("List size: " + list.size());
              list = CheckForMissingDay(list);
              for (ImageUserDataSet data: list){
                countList.add(data.getCount());
              }

              //Testa om någon veckodag saknas, lägg isf till ett 0 element
              //Lägg sedan in hela JsonArray i ett jsonObejct och skicka

              //for (ImageUserDataSet i : list) {
                JsonObject jsonObject = new JsonObject();
                //jsonObject.put("day_of_the_week", i.getDay_of_the_week());
                //jsonObject.put("count", i.getCount());
                jsonObject.put("count", countList);

             // }
              request.json(countList);
            } else {
              System.out.println("Failure: " + ar.cause().getMessage());
            }
            // Now close the pool
            client.close();
          });

      });
      //handle requests for static resources
      router.get().handler(StaticHandler.create());

      // Create the HTTP server
      vertx.createHttpServer()
        // Handle every request using the router
        .requestHandler(router)
        // Start listening
        .listen(8888)
        // Print the port
        .onSuccess(server -> {
            System.out.println(
              "HTTP server started on port " + server.actualPort()
            );
            connectOptions = new MySQLConnectOptions()
              .setPort(3333)
              .setHost("localhost")
              .setDatabase("serverut")
              .setUser("user")
              .setPassword("ThePassword");

  // Pool options
            poolOptions = new PoolOptions()
              .setMaxSize(5);

            jsonArray = new JsonArray();

          }
        );
    }

    /**
     * Checks for missing days
     * @param list the list of days and amount of pictures saved on corresponding day
     * @return
     */
    private ArrayList CheckForMissingDay(ArrayList<ImageUserDataSet> list){
      if(list.size()==0 || !(list.get(0).getDay_of_the_week() == 1) ){
        list.add(0,new ImageUserDataSet(uuid,0,1));
      }
      if(list.size()==1 || !(list.get(1).getDay_of_the_week() == 2)){
        list.add(1,new ImageUserDataSet(uuid,0,2));
      }
      if(list.size()==2 || !(list.get(2).getDay_of_the_week() == 3)){
        list.add(2,new ImageUserDataSet(uuid,0,3));
      }
      if(list.size()==3 || !(list.get(3).getDay_of_the_week() == 4)){
        list.add(3,new ImageUserDataSet(uuid,0,4));
      }
      if(list.size()==4 || !(list.get(4).getDay_of_the_week() == 5) ){
        list.add(4,new ImageUserDataSet(uuid,0,5));
      }
      if(list.size()==5 || !(list.get(5).getDay_of_the_week() == 6)){
        list.add(5,new ImageUserDataSet(uuid,0,6));
      }
      if(list.size()==6 || !(list.get(6).getDay_of_the_week() == 7)){
        list.add(6,new ImageUserDataSet(uuid,0,7));
      }
      return list;
    }

    private void dbCall(Route Route){
      System.out.println("Tjenis");

      // Create the client pool
      client = MySQLPool.pool(vertx, connectOptions, poolOptions);
      //JsonArray jsonArray = new JsonArray();
      // A simple query
      client
        //.query("SELECT * FROM canvas_image")
        .query("select creator, day_of_the_week, count(*) AS count from canvas_image where creator = '" + uuid + "' group by creator, day_of_the_week;")
        .execute(ar -> {

          if (ar.succeeded()) {
            System.out.println("SUCCESS");
            RowSet<Row> result = ar.result();
            jsonArray.clear();
            ArrayList<ImageUserDataSet> list = new ArrayList<>();
            for(Row row: result) {
              ImageUserDataSet temp = new ImageUserDataSet();
              temp.setUuid(row.getString("creator"));
              temp.setCount(row.getInteger("count"));
              temp.setDay_of_the_week(row.getInteger("day_of_the_week"));
              list.add(temp);
            }
            for (ImageUserDataSet i : list) {
              JsonObject jsonObject = new JsonObject();
              jsonObject.put("creator", i.getUuid());
              jsonObject.put("day_of_the_week", i.getDay_of_the_week());
              jsonObject.put("count", i.getCount());
              jsonArray.add(jsonObject);
              System.out.println(jsonArray.toString());
            }
          } else {
            System.out.println("Failure: " + ar.cause().getMessage());
          }
          // Now close the pool
          client.close();
        });
    }


    @Override
    public void stop() throws Exception {
        super.stop();
        vertx.close();
    }
  }

          /*
        // Get the address of the request
        String address = context.request().connection().remoteAddress().toString();
        // Get the query parameter "name"
        MultiMap queryParams = context.queryParams();
        String name = queryParams.contains("name") ? queryParams.get("name") : "unknown";
        // Write a json response
        context.json(
          new JsonObject()
            .put("name", name)
            .put("address", address)
            .put("message", "Hello " + name + " connected from " + address)
        );*/
